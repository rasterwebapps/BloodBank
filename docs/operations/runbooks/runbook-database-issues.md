# Runbook: Database Issues

**Last Updated**: 2026-04-22
**Severity**: P1 (connection pool exhausted, DB down) / P2 (slow queries, replication lag)
**Response Owner**: On-call Engineer (L2) + DBA
**Escalation**: See `docs/operations/on-call-guide.md`

---

## Trigger

This runbook is activated when:
- Grafana alert: `BloodBankDBConnectionPoolExhausted` (> 90% pool usage)
- Grafana alert: `BloodBankDBSlowQueries` (p95 query time > 5 s)
- Grafana alert: `BloodBankDBReplicationLag` (lag > 30 s)
- Service health endpoints return `DB: DOWN`
- Services log `HikariPool-1 - Connection is not available, request timed out`

---

## Impact Assessment

| Scenario | Severity | Impact |
|---|---|---|
| Primary DB fully down | P1 | All services unable to read/write — full outage |
| Connection pool exhausted | P1 | Services queue up, timeout errors cascade |
| Replication lag > 5 min | P2 | Read replicas serving stale data |
| Slow queries (blocking) | P2 | Degraded response times; risk of pool exhaustion |
| Disk full on DB node | P1 | Writes blocked; DB may crash |

---

## Step 1 — Identify the Problem (≤ 5 minutes)

```bash
# 1. Check PostgreSQL pod status
kubectl get pods -n bloodbank-prod -l app=postgresql

# 2. Check connection pool metrics via Actuator
curl -s https://api.bloodbank.example.com/donor-service/actuator/metrics/hikaricp.connections.active | jq .

# 3. Connect to PostgreSQL to diagnose
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT count(*) as total_connections,
           state,
           wait_event_type,
           wait_event
    FROM pg_stat_activity
    GROUP BY state, wait_event_type, wait_event
    ORDER BY total_connections DESC;"
```

---

## Scenario A — Connection Pool Exhausted

### Symptoms
- HikariCP `active` connections at or near `maximumPoolSize`
- Services returning HTTP 503 or 500 with `connection timeout`
- Grafana: pool usage > 90%

### Diagnosis

```bash
# Count connections per application
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT application_name, state, COUNT(*) as connections
    FROM pg_stat_activity
    WHERE datname = 'bloodbank_db'
    GROUP BY application_name, state
    ORDER BY connections DESC;"

# Find long-running queries (> 60 seconds)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT pid, now() - pg_stat_activity.query_start AS duration, query, state
    FROM pg_stat_activity
    WHERE (now() - pg_stat_activity.query_start) > interval '60 seconds'
      AND state != 'idle'
    ORDER BY duration DESC;"
```

### Resolution

```bash
# 1. Terminate idle connections (safe)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT pg_terminate_backend(pid)
    FROM pg_stat_activity
    WHERE datname = 'bloodbank_db'
      AND state = 'idle'
      AND query_start < now() - interval '10 minutes';"

# 2. If long-running queries are blocking (get pid from diagnosis query above)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "SELECT pg_cancel_backend(<PID>);"

# 3. Temporarily increase connection limit if needed (emergency only)
# Edit HikariCP config in application.yml via ConfigMap:
kubectl edit configmap donor-service-config -n bloodbank-prod
# Change: spring.datasource.hikari.maximum-pool-size from 20 to 30
# Then restart the service:
kubectl rollout restart deployment/donor-service -n bloodbank-prod

# 4. Consider enabling PgBouncer connection pooler if not already active
kubectl get deployment pgbouncer -n bloodbank-prod
```

---

## Scenario B — Slow Queries

### Diagnosis

```bash
# Find top slow queries using pg_stat_statements
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT query,
           calls,
           round(total_exec_time::numeric, 2) AS total_ms,
           round(mean_exec_time::numeric, 2) AS avg_ms,
           round(stddev_exec_time::numeric, 2) AS stddev_ms,
           rows
    FROM pg_stat_statements
    ORDER BY mean_exec_time DESC
    LIMIT 20;"

# Check for missing indexes (sequential scans on large tables)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT relname, seq_scan, seq_tup_read, idx_scan, idx_tup_fetch
    FROM pg_stat_user_tables
    WHERE seq_scan > 100
    ORDER BY seq_scan DESC
    LIMIT 20;"

# Check for table bloat / need for VACUUM
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT relname, n_dead_tup, n_live_tup, last_vacuum, last_autovacuum
    FROM pg_stat_user_tables
    WHERE n_dead_tup > 10000
    ORDER BY n_dead_tup DESC;"
```

### Resolution

```bash
# 1. EXPLAIN ANALYZE the slow query (add ANALYZE to see actual rows)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "EXPLAIN (ANALYZE, BUFFERS) <SLOW_QUERY_HERE>;"

# 2. Force autovacuum on a bloated table
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "VACUUM ANALYZE <TABLE_NAME>;"

# 3. Create a missing index (coordinate with DBA team — test in staging first)
# Example: missing index on blood_units (branch_id, status)
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    CREATE INDEX CONCURRENTLY idx_blood_units_branch_status
    ON blood_units(branch_id, status)
    WHERE status != 'DISPOSED';"

# 4. Reset pg_stat_statements after investigation
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "SELECT pg_stat_statements_reset();"
```

---

## Scenario C — Replication Lag

### Diagnosis

```bash
# Check replication lag on primary
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT client_addr, state, sent_lsn, write_lsn, flush_lsn, replay_lsn,
           (sent_lsn - replay_lsn) AS replication_lag_bytes
    FROM pg_stat_replication;"

# Check lag in seconds on replica
kubectl exec -it -n bloodbank-prod postgresql-replica-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT now() - pg_last_xact_replay_timestamp() AS replication_delay;"
```

### Resolution

```bash
# 1. If lag is growing, check replica disk I/O
kubectl top pods -n bloodbank-prod postgresql-replica-0

# 2. If replica is falling far behind (> 5 min), redirect read traffic to primary
# Edit service to point reads to primary endpoint:
kubectl patch service postgresql-read -n bloodbank-prod \
  --patch '{"spec":{"selector":{"role":"primary"}}}'

# 3. If replica cannot catch up, rebuild it from a backup (coordinate with DBA)
# This is a DBA-level action — escalate to L3 if needed.
```

---

## Scenario D — Database Failover

### Trigger
Primary PostgreSQL pod is down, OOMKilled, or node failure detected.

```bash
# 1. Check primary status
kubectl get pod -n bloodbank-prod postgresql-0

# 2. Check Patroni or pg_auto_failover cluster status (if using HA operator)
kubectl exec -it -n bloodbank-prod postgresql-0 -- patronictl list

# 3. If automatic failover has not occurred within 30 seconds, trigger manual
kubectl exec -it -n bloodbank-prod postgresql-0 -- patronictl failover bloodbank_cluster

# 4. Update application connection strings if DNS hasn't auto-switched
# (With proper service DNS setup, this should be automatic)
kubectl get service postgresql-primary -n bloodbank-prod -o jsonpath='{.spec.selector}'

# 5. Verify applications reconnect
for SVC in donor-service inventory-service lab-service; do
  curl -s https://api.bloodbank.example.com/${SVC}/actuator/health | jq '.components.db.status'
done
```

**RTO target**: < 5 minutes  
**RPO target**: < 1 minute (with streaming replication)

---

## Scenario E — Disk Full on DB Node

```bash
# Check disk usage on DB node
kubectl exec -it -n bloodbank-prod postgresql-0 -- df -h /var/lib/postgresql/data

# Check table sizes
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT tablename, pg_size_pretty(pg_total_relation_size(tablename::regclass)) AS size
    FROM pg_tables
    WHERE schemaname = 'public'
    ORDER BY pg_total_relation_size(tablename::regclass) DESC
    LIMIT 15;"

# Emergency space recovery — delete old WAL segments (DBA action only)
# First, check what's consuming space
kubectl exec -it -n bloodbank-prod postgresql-0 -- du -sh /var/lib/postgresql/data/pg_wal/

# Option 1: Expand PVC (if storage class supports expansion)
kubectl patch pvc postgresql-data-postgresql-0 -n bloodbank-prod \
  --patch '{"spec":{"resources":{"requests":{"storage":"500Gi"}}}}'

# Option 2: Purge old audit_logs (check retention policy first)
# NEVER delete without written approval from L3
```

---

## Escalation

| Condition | Action |
|---|---|
| Pool exhausted and not recovering after terminating idle connections | Escalate to L3 immediately |
| Primary DB down > 5 minutes | Escalate to L3 + notify Executive Sponsor |
| Data corruption detected | Immediately follow `runbook-data-corruption.md` |
| Disk full on DB node | Escalate to L3 immediately; do not attempt recovery without DBA approval |
| Failover not completing after 5 minutes | Escalate to L3 + cloud provider support |

---

## Post-Incident

After resolution, run these queries to verify data integrity:

```bash
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    -- Check for transaction inconsistencies
    SELECT schemaname, relname, n_dead_tup, last_vacuum
    FROM pg_stat_user_tables
    WHERE n_dead_tup > 0
    ORDER BY n_dead_tup DESC
    LIMIT 10;

    -- Check for replication consistency
    SELECT now() - pg_last_xact_replay_timestamp() AS replication_delay;"
```

Schedule a Post-Incident Review: `docs/operations/incident-response.md`

---

## Reference

| Resource | URL |
|---|---|
| Grafana DB dashboard | `https://monitoring.bloodbank.example.com/grafana/d/postgres` |
| PostgreSQL admin | `https://pgadmin.bloodbank.example.com` (VPN only) |
| On-call guide | `docs/operations/on-call-guide.md` |
| Incident response | `docs/operations/incident-response.md` |
