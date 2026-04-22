# Scaling Validation Procedures

**Last Updated**: 2026-04-22
**Milestone**: M11 — Regional Rollout
**Issues**: M11-031, M11-032, M11-033, M11-034
**Status**: 🔴 NOT STARTED

---

## Overview

Scaling validation is performed **during and after Batch 4 go-live**, once multi-branch traffic is flowing across the full regional deployment. Unlike the per-batch `scaling-check.sh` runs (which check baseline health), this validation confirms that the platform holds up under realistic combined load from all branches simultaneously, and that Kubernetes auto-scaling, database performance, and caching layers all meet their targets.

> **When to run**: Begin monitoring from Batch 3 go-live onwards. Conduct formal validation at the end of Week 4, after all batches are live. Repeat after each major alert threshold adjustment.

---

## 1. HPA Scaling Verification (M11-031)

### 1.1 Purpose

Verify that Kubernetes Horizontal Pod Autoscalers (HPAs) for each microservice respond correctly to increased load from multi-branch traffic — scaling out within the defined latency window and scaling back in during low-traffic periods.

### 1.2 HPA Configuration Reference

All HPA resources are defined in `k8s/production/hpa/`. Expected configuration per service:

| Service | Min Replicas | Max Replicas | Scale-Out Trigger | Scale-In Stabilization |
|---|---|---|---|---|
| api-gateway | 2 | 10 | CPU > 60% or RPS > 500 | 5 min |
| donor-service | 2 | 8 | CPU > 70% | 5 min |
| inventory-service | 2 | 8 | CPU > 70% | 5 min |
| lab-service | 2 | 6 | CPU > 70% | 5 min |
| transfusion-service | 2 | 6 | CPU > 70% | 5 min |
| notification-service | 1 | 4 | Queue depth > 100 | 3 min |
| branch-service | 2 | 4 | CPU > 70% | 5 min |
| reporting-service | 1 | 4 | CPU > 80% | 10 min |

### 1.3 Verification Steps

#### Step 1: Confirm HPA Resources Exist

```bash
kubectl get hpa -n bloodbank-prod
```

Expected: All 14 services have an HPA entry. Verify `MINPODS`, `MAXPODS`, and `TARGETS` columns match the configuration reference above.

#### Step 2: Check Current Replica Counts

```bash
kubectl get deployments -n bloodbank-prod -o wide
```

During low-traffic periods (before business hours), most services should be at minimum replicas.

#### Step 3: Simulate Load — Morning Peak

At 08:45 local time (15 minutes before peak), run the load simulation:

```bash
# Run from the load-test node (not from a branch network)
k6 run k8s/load-tests/regional-morning-peak.js \
  --env BASE_URL=https://api.bloodbank.<YOUR_DOMAIN> \
  --env BRANCHES=4 \
  --env VIRTUAL_USERS=200 \
  --env DURATION=15m
```

The `regional-morning-peak.js` script simulates 50 concurrent users per branch performing typical morning workflows (donor check-in, lab test submission, inventory queries).

#### Step 4: Observe HPA Scale-Out

```bash
# Watch HPAs in real time (run in a separate terminal)
watch -n 10 kubectl get hpa -n bloodbank-prod
```

Expected observations within 3 minutes of load onset:

| Service | Expected Replica Count at Peak |
|---|---|
| api-gateway | 4–6 |
| donor-service | 3–5 |
| inventory-service | 3–4 |
| lab-service | 2–4 |

Record actual replica counts during peak.

#### Step 5: Verify API Response Times During Scale-Out

Query Prometheus for p95 latency during the load test:

```promql
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket{namespace="bloodbank-prod"}[5m]))
  by (le, service)
)
```

**Target**: p95 < 500 ms for all services during scale-out.

#### Step 6: Observe Scale-In After Load Drops

After the load test completes, wait 10 minutes and check replica counts return to minimum.

```bash
kubectl get deployments -n bloodbank-prod | grep -E 'NAME|READY'
```

#### Step 7: Check HPA Events for Errors

```bash
kubectl describe hpa -n bloodbank-prod | grep -A 5 Events
```

Look for: `SuccessfulRescale` events. Flag any `FailedScale` or `DesiredReplicas` stuck events.

### 1.4 HPA Scaling Results Log

| Metric | Target | Actual | Pass/Fail |
|---|---|---|---|
| Scale-out triggered within 3 min of load onset | ≤ 3 min | — | — |
| api-gateway replica count at peak | ≥ 4 | — | — |
| p95 API latency during scale-out | < 500 ms | — | — |
| Scale-in within 10 min after load drops | ≤ 10 min | — | — |
| Zero `FailedScale` events | 0 | — | — |
| Zero OOMKilled pods during test | 0 | — | — |

### 1.5 Pass Criteria

All six metrics in the results log must meet their targets.

---

## 2. Database Performance Verification (M11-032)

### 2.1 Purpose

Verify that PostgreSQL 17 maintains acceptable query performance under multi-branch data volume — specifically that queries do not perform full-table scans on branch-scoped tables, that connection pool utilization stays within limits, and that long-running queries do not accumulate.

### 2.2 Expected Data Volume at Full Regional Rollout

| Table | Estimated Rows (All Branches) | Key Indexes |
|---|---|---|
| `donors` | 5,000–50,000 | `(branch_id, blood_group)`, `(email)` |
| `blood_units` | 2,000–20,000 | `(branch_id, status, blood_group)` |
| `test_results` | 10,000–100,000 | `(branch_id, test_order_id)` |
| `audit_logs` | 100,000–1,000,000 | `(branch_id, entity_type, created_at)` |
| `transfusions` | 1,000–10,000 | `(branch_id, patient_id)` |

### 2.3 Query Performance Checks

Run these checks against the production database (read-only replica if available):

#### Check 1: Branch-Scoped Query Uses Index

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, first_name, last_name, blood_group
FROM donors
WHERE branch_id = '<branch-uuid>'
  AND status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 20;
```

**Expected**: `Index Scan` using `idx_donors_branch_status` (or equivalent composite index). **No** `Seq Scan` on the full donors table.

#### Check 2: Cross-Branch Aggregate (Regional Dashboard)

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT branch_id, blood_group, COUNT(*) AS available_units
FROM blood_units
WHERE status = 'AVAILABLE'
  AND branch_id IN (SELECT id FROM branches WHERE region_id = '<region-uuid>')
GROUP BY branch_id, blood_group;
```

**Expected**: `Index Scan` on `idx_blood_units_branch_status_group`. Query time < 200 ms.

#### Check 3: Audit Log Query Performance

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT entity_type, action, performed_by, created_at
FROM audit_logs
WHERE branch_id = '<branch-uuid>'
  AND created_at >= NOW() - INTERVAL '7 days'
ORDER BY created_at DESC
LIMIT 100;
```

**Expected**: `Index Scan` using `idx_audit_logs_branch_created`. Query time < 500 ms.

#### Check 4: Connection Pool Utilization

```sql
SELECT count(*) AS active_connections,
       max_conn AS max_connections,
       round(100.0 * count(*) / max_conn, 1) AS utilization_pct
FROM pg_stat_activity, (SELECT setting::int AS max_conn FROM pg_settings WHERE name = 'max_connections') s
WHERE state = 'active'
GROUP BY max_conn;
```

**Target**: Active connections < 70% of `max_connections`.

#### Check 5: Long-Running Queries

```sql
SELECT pid, now() - query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active'
  AND now() - query_start > INTERVAL '5 seconds'
ORDER BY duration DESC;
```

**Expected**: Zero queries running longer than 5 seconds during normal operation.

### 2.4 pgBouncer / Connection Pool Metrics

If pgBouncer is deployed (recommended for production):

```bash
# Connect to pgBouncer admin console
psql -h localhost -p 6432 -U pgbouncer pgbouncer -c "SHOW STATS;"
```

Review `avg_query_time` (target < 50 ms), `max_wait` (target < 100 ms), and `cl_active` (target < 80% of pool size).

### 2.5 Database Performance Results Log

| Check | Metric | Target | Actual | Pass/Fail |
|---|---|---|---|---|
| Donor query | Execution plan | Index Scan only | — | — |
| Donor query | Query time | < 50 ms | — | — |
| Regional aggregate | Query time | < 200 ms | — | — |
| Audit log query | Query time | < 500 ms | — | — |
| Connection pool | Utilization | < 70% | — | — |
| Long-running queries | Count during peak | 0 | — | — |
| Replication lag (if replica) | Lag | < 1 s | — | — |

### 2.6 Pass Criteria

All checks must show `Index Scan` plans (no `Seq Scan` on large tables). All timing targets met. Connection pool utilization below 70%.

---

## 3. Redis Cache Hit Rate Verification (M11-033)

### 3.1 Purpose

Verify that Redis caching achieves a **>90% hit rate** across all branches once the system has been operating for at least 24 hours post go-live (the cache warm-up period).

### 3.2 Cache Hit Rate Target

| Cache Layer | Target Hit Rate | Measurement Window |
|---|---|---|
| Overall Redis hit rate | **> 90%** | 24 h rolling window |
| Branch master data (branches, regions) | > 98% | 24 h rolling window |
| Blood group / component type lookups | > 99% | 24 h rolling window |
| Stock level summaries | > 85% | 1 h rolling window |
| User session tokens | > 95% | 24 h rolling window |

> **Note**: Stock level summaries have a lower target (85%) because they are invalidated frequently during active donation and issuing periods.

### 3.3 Measurement via Redis CLI

#### Method 1: Redis INFO stats

```bash
kubectl exec -n bloodbank-prod deployment/redis -- redis-cli INFO stats | grep -E "keyspace_hits|keyspace_misses"
```

Calculate hit rate:

```
hit_rate = keyspace_hits / (keyspace_hits + keyspace_misses) × 100
```

#### Method 2: scaling-check.sh

The automated script reports cache hit rates:

```bash
./k8s/scripts/scaling-check.sh --namespace bloodbank-prod --output json | jq '.cache'
```

Expected output:

```json
{
  "hit_rate_pct": 92.4,
  "status": "OK",
  "threshold": 90.0,
  "hits": 184320,
  "misses": 15280
}
```

#### Method 3: Prometheus / Grafana Dashboard

Open the **BloodBank Operations** Grafana dashboard and navigate to the **Cache Performance** panel. Review:

- **Redis Hit Rate (%)** — should show a stable line above 90% after the 24 h warm-up window.
- **Cache Evictions / sec** — should be < 10/sec during normal operation.
- **Memory Used / Max Memory** — should be < 80%.

### 3.4 Per-Branch Cache Key Namespace Verification

Each branch should have isolated cache keys (prefixed with `branch:{branchId}:`). Verify there is no key collision between branches:

```bash
kubectl exec -n bloodbank-prod deployment/redis -- redis-cli --scan --pattern "branch:*" | head -20
```

Spot-check that keys from Branch A cannot be read by Branch B's service instances by reviewing the key prefix configuration in `application.yml` for each service:

```yaml
spring:
  cache:
    redis:
      key-prefix: "branch:${BRANCH_ID}:"
```

### 3.5 Cache Hit Rate Results Log

| Metric | Target | Actual (24 h post all-branches live) | Pass/Fail |
|---|---|---|---|
| Overall hit rate | > 90% | — | — |
| Branch master data hit rate | > 98% | — | — |
| Stock level summary hit rate | > 85% | — | — |
| Cache evictions / sec | < 10 | — | — |
| Redis memory utilization | < 80% | — | — |
| Cross-branch key isolation | No collisions | — | — |

### 3.6 Troubleshooting Low Cache Hit Rates

| Symptom | Likely Cause | Remediation |
|---|---|---|
| Hit rate < 70% immediately after go-live | Cache cold (normal) | Wait 24 h; re-measure |
| Hit rate < 80% after 48 h | TTL too short or cache key mismatch | Review TTL settings in `application.yml` |
| Hit rate drops suddenly | Redis eviction (maxmemory-policy) | Increase Redis memory allocation or eviction policy |
| Cross-branch key collisions | Missing `branch_id` in cache key prefix | Fix `key-prefix` configuration and flush affected keys |
| Stock level hit rate < 70% | Invalidation too aggressive (event storm) | Increase stock summary TTL from 30 s to 60 s |

### 3.7 Pass Criteria

Overall Redis hit rate must exceed **90%** in the 24-hour window following all-branches-live status. All metrics in the results log must meet their targets.

---

## 4. Alert Threshold Tuning Guide (M11-034)

### 4.1 Purpose

Calibrate Prometheus alerting thresholds based on **real usage patterns** observed during the regional rollout. Default thresholds were set conservatively at deployment; this guide defines how to review and adjust them to reduce alert fatigue while maintaining early warning capability.

### 4.2 Alert Inventory

All alerting rules are in `k8s/monitoring/alerts/bloodbank-alerts.yaml`. Key alerts:

| Alert Name | Metric | Default Threshold | Severity |
|---|---|---|---|
| `HighAPIErrorRate` | `rate(http_requests_total{status=~"5.."}[5m])` | > 1% | Warning |
| `CriticalAPIErrorRate` | Same | > 5% | Critical |
| `SlowAPIResponse` | `p95 http_server_requests_seconds` | > 500 ms | Warning |
| `CriticalSlowAPIResponse` | Same | > 2 s | Critical |
| `LowCacheHitRate` | Derived from Redis INFO | < 90% | Warning |
| `HighDBConnections` | `pg_stat_activity count` | > 70% of max | Warning |
| `RabbitMQDLQMessages` | `rabbitmq_queue_messages_total{queue="bloodbank.dlq"}` | > 0 | Critical |
| `HPAMaxReplicas` | `kube_horizontalpodautoscaler_status_current_replicas` | = maxReplicas | Warning |
| `PodCrashLooping` | `rate(kube_pod_container_status_restarts_total[15m])` | > 3 in 15 min | Critical |
| `BloodStockCritical` | Custom: units below minimum | Per blood group | Critical |
| `DiskUsageHigh` | `node_filesystem_avail_bytes` | < 20% free | Warning |
| `DiskUsageCritical` | Same | < 10% free | Critical |

### 4.3 Tuning Process

#### Phase 1: Collect Baseline Metrics (Week 4, Day 1–4)

For each alert, record the observed metric range during normal operations across all branches:

| Alert | Observed Min | Observed Max | Typical Value | Trigger Count (Week 4) |
|---|---|---|---|---|
| `HighAPIErrorRate` | — | — | — | — |
| `SlowAPIResponse` | — | — | — | — |
| `LowCacheHitRate` | — | — | — | — |
| `HighDBConnections` | — | — | — | — |
| `HPAMaxReplicas` | — | — | — | — |

#### Phase 2: Identify Alert Fatigue

An alert is causing **fatigue** if it fires:
- More than **5 times per day** on average without any action required, OR
- During **known non-incident periods** (e.g., nightly batch jobs, scheduled reports).

An alert is **under-sensitive** if a real incident occurred that it failed to catch before manual detection.

#### Phase 3: Adjust Thresholds

Edit `k8s/monitoring/alerts/bloodbank-alerts.yaml` following these guidelines:

**Warning threshold adjustment** — move threshold to the **95th percentile** of normal observed values plus a 20% buffer:

```yaml
# Example: SlowAPIResponse — observed p95 during normal ops = 180 ms
# New warning threshold = 180 ms × 1.20 = 216 ms → round to 250 ms
- alert: SlowAPIResponse
  expr: histogram_quantile(0.95, ...) > 0.250   # was 0.500
  for: 5m
  labels:
    severity: warning
```

**Critical threshold adjustment** — set at the point where patient safety or data integrity is at risk:

```yaml
- alert: CriticalSlowAPIResponse
  expr: histogram_quantile(0.95, ...) > 2.0    # 2 s is always unacceptable — keep
  for: 2m
  labels:
    severity: critical
```

**Time-window adjustments** — extend `for:` duration to suppress transient spikes:

| Alert | Default `for:` | Recommended `for:` after tuning |
|---|---|---|
| `HighAPIErrorRate` | 1 m | 3 m (transient spikes common at branch morning peak) |
| `SlowAPIResponse` | 5 m | 5 m (keep — 5 min of slow responses is real) |
| `LowCacheHitRate` | 5 m | 15 m (cache naturally dips at branch go-live and morning) |
| `HPAMaxReplicas` | 2 m | 5 m (brief max-replica events normal during peak) |

#### Phase 4: Suppression Rules for Known Maintenance Windows

Add Alertmanager inhibit rules for the nightly Flyway migration job (if applicable) and the training environment reset:

```yaml
# alertmanager.yaml — inhibit rules
inhibit_rules:
  - source_match:
      alertname: MaintenanceWindow
    target_match_re:
      alertname: '(SlowAPIResponse|HighDBConnections)'
    equal: ['namespace']
```

#### Phase 5: Validate and Deploy

After editing alert rules:

```bash
# Validate YAML syntax
promtool check rules k8s/monitoring/alerts/bloodbank-alerts.yaml

# Apply to cluster
kubectl apply -f k8s/monitoring/alerts/bloodbank-alerts.yaml -n monitoring

# Confirm rules loaded
kubectl exec -n monitoring deployment/prometheus -- \
  promtool query rules http://localhost:9090 | grep bloodbank
```

### 4.4 Alert Threshold Decision Log

Document every threshold change for auditability:

| Alert Name | Old Threshold | New Threshold | Reason | Changed By | Date |
|---|---|---|---|---|---|
| `SlowAPIResponse` | 500 ms | — | — | — | — |
| `LowCacheHitRate` | 90% | — | — | — | — |
| `HighAPIErrorRate` | 1% | — | — | — | — |

### 4.5 Non-Negotiable Thresholds

The following thresholds must **never be raised** regardless of observed patterns, as they are tied directly to patient safety and data integrity:

| Alert | Threshold | Rationale |
|---|---|---|
| `RabbitMQDLQMessages` | > 0 messages | Every DLQ message is a lost event — must be investigated immediately |
| `PodCrashLooping` | > 3 restarts in 15 min | Service instability affects patient-critical workflows |
| `BloodStockCritical` | Per minimum stock policy | Blood stock shortages are life-threatening |
| `CriticalAPIErrorRate` | > 5% | >5% error rate means significant workflow failures |

### 4.6 Pass Criteria

- All alerts reviewed against observed Week 4 data.
- Zero alert fatigue alerts (> 5 fires/day with no action) remaining.
- Threshold decision log completed for all changes.
- `promtool check rules` passes with zero errors.
- Non-negotiable thresholds unchanged.
- DevOps Lead and Project Manager have approved the final alert configuration.

---

## 5. Scaling Validation Summary Checklist

| # | Area | Issue | Status | Signed Off By | Date |
|---|---|---|---|---|---|
| 1 | HPA Scaling Verification | M11-031 | — | — | — |
| 2 | Database Performance | M11-032 | — | — | — |
| 3 | Redis Cache Hit Rate | M11-033 | — | — | — |
| 4 | Alert Threshold Tuning | M11-034 | — | — | — |

**Overall Scaling Validation Result**: ☐ PASS ☐ FAIL (with accepted exceptions)

**DevOps Lead signature**: _________________________ **Date**: _____________

**Project Manager signature**: _________________________ **Date**: _____________

---

## References

- [`docs/operations/rollout-schedule.md`](./rollout-schedule.md) — Rollout timeline and scripts reference
- [`docs/operations/cross-branch-validation.md`](./cross-branch-validation.md) — Cross-branch functional validation (M11-026–029)
- [`docs/operations/regional-signoff-template.md`](./regional-signoff-template.md) — Regional Management sign-off (M11-030)
- [`docs/milestones/M11-regional-rollout.md`](../milestones/M11-regional-rollout.md) — Issue tracker
- `k8s/monitoring/alerts/bloodbank-alerts.yaml` — Prometheus alert rules
- `k8s/scripts/scaling-check.sh` — Automated scaling health check script
