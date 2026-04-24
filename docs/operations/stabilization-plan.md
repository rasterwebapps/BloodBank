# Post-Launch Stabilization Plan

**Last Updated**: 2026-04-22
**Milestone Issues**: M13-001, M13-002, M13-003, M13-004
**Duration**: Weeks 1–2 post worldwide launch
**Status**: 🔴 NOT STARTED

---

## Overview

The stabilization plan covers the first 14 days after the worldwide launch (M12). During this period, the engineering and SRE teams operate in heightened readiness mode to detect and resolve production issues rapidly before normal operational cadence begins.

| Phase | Days | Posture |
|---|---|---|
| Intensive | 1–3 | 24/7 on-call, 30-minute dashboard checks, zero tolerance for P1 |
| Active | 4–7 | Business-hours primary + 24/7 on-call for P1/P2 |
| Stabilization | 8–14 | Daily review, on-call for critical only, alert tuning |

---

## M13-001: 24/7 Monitoring Plan

### Monitoring Stack

| Layer | Tool | URL |
|---|---|---|
| Infrastructure metrics | Prometheus + Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| Application logs | Loki + Grafana | `https://monitoring.bloodbank.example.com/logs` |
| Distributed tracing | Jaeger / OpenTelemetry | `https://monitoring.bloodbank.example.com/tracing` |
| External uptime | Uptime Robot | `https://uptimerobot.com/dashboard` |
| Kubernetes cluster | K8s Dashboard | `https://k8s.bloodbank.example.com` |
| RabbitMQ queues | RabbitMQ Management | `https://rabbitmq.bloodbank.example.com` |
| Error tracking | Sentry / Grafana alerts | `https://monitoring.bloodbank.example.com/alerts` |

### On-Call Rota (Days 1–14)

Two engineers are on-call at all times during Days 1–3, with one primary and one backup for Days 4–14.

| Shift | Hours (UTC) | Primary | Backup |
|---|---|---|---|
| Day | 06:00–18:00 | _TBD_ | _TBD_ |
| Night | 18:00–06:00 | _TBD_ | _TBD_ |

The on-call engineer **checks all Grafana dashboards every 30 minutes** during Days 1–3 and every 60 minutes during Days 4–14. All checks are logged in the `#bloodbank-ops` Slack channel.

### Critical Metrics and Thresholds

| Metric | Warning | Critical | Data Source |
|---|---|---|---|
| API p95 response time | > 2 s | > 5 s | Grafana / Prometheus |
| API error rate (5xx) | > 0.5% | > 2% | Grafana / Prometheus |
| Database connection pool usage | > 70% | > 90% | HikariCP metrics |
| Redis cache hit rate | < 80% | < 60% | Redis INFO stats |
| RabbitMQ queue depth (`bloodbank.events`) | > 100 messages | > 1,000 messages | RabbitMQ Management API |
| Dead letter queue (DLQ) depth | > 0 | > 10 messages | RabbitMQ Management API |
| JVM heap usage (any service) | > 80% | > 95% | JVM metrics |
| Pod restart count | > 1 in 10 min | > 3 in 10 min | `kubectl` / Grafana |
| Disk usage (prod nodes) | > 75% | > 90% | Node exporter |
| Keycloak authentication failure rate | > 5% | > 15% | Keycloak metrics |
| Blood unit inventory discrepancy | Any | Any | Application alert |

### Alert Routing

All alerts are routed simultaneously to:

- **Slack** `#bloodbank-ops-alerts` — all warning and critical alerts
- **PagerDuty** — critical alerts only (on-call engineer paged)
- **Email** `ops-team@bloodbank.example.com` — critical alert digest (hourly)

### Alert Severity and Response SLA

| Severity | Definition | Acknowledgement SLA | Resolution SLA |
|---|---|---|---|
| P1 — Critical | System down, data corruption, blood safety risk | 15 minutes | 4 hours |
| P2 — High | Core workflow broken (collections, issuing, crossmatch) | 30 minutes | 8 hours |
| P3 — Medium | Non-critical feature broken, performance degraded | 2 hours | 24 hours |
| P4 — Low | Cosmetic issue, minor UX bug | Next business day | 72 hours |

### Key Grafana Dashboards to Monitor

| Dashboard | What to Check |
|---|---|
| **Service Health Overview** | All 14 microservices `UP`, error rate, latency |
| **Database Performance** | Connection pool, slow queries, active sessions |
| **RabbitMQ Overview** | Queue depth, consumer count, DLQ messages |
| **Kubernetes Pods** | Pod status, restart counts, resource usage |
| **Business Metrics** | Collections/hour, units issued, crossmatch TAT |
| **Security Events** | Failed logins, role changes, DLQ auth events |

### Useful Operational Commands

```bash
# Check all service health endpoints
for svc in donor-service inventory-service lab-service branch-service \
           transfusion-service hospital-service billing-service \
           request-matching-service notification-service reporting-service \
           document-service compliance-service api-gateway config-server; do
  echo -n "$svc: "
  kubectl exec -n bloodbank deploy/$svc -- \
    curl -sf http://localhost:8080/actuator/health | jq -r '.status'
done

# Pod restart counts
kubectl get pods -n bloodbank-prod \
  --sort-by='.status.containerStatuses[0].restartCount' | tail -20

# RabbitMQ DLQ depth
curl -su guest:guest \
  https://rabbitmq.bloodbank.example.com/api/queues/%2F/bloodbank.dlq \
  | jq '.messages'

# Top slow queries (PostgreSQL 17)
kubectl exec -n bloodbank deploy/postgres -- psql -U bloodbank_user bloodbank_db -c "
  SELECT query, calls, total_exec_time/calls AS avg_ms, rows
  FROM pg_stat_statements
  ORDER BY avg_ms DESC
  LIMIT 10;"
```

---

## M13-002: Daily Triage Process

### Daily Triage Schedule

| Time (UTC) | Activity | Owner | Audience |
|---|---|---|---|
| 08:00 | Run morning triage checklist | On-call engineer | SRE team |
| 09:00 | Post daily status summary to Slack | DevOps Lead | Project Slack + Branch Management |
| 17:00 | End-of-day review | On-call engineer | SRE team |
| 23:00 | Overnight readiness check | Night on-call | Internal only |

### Morning Triage Checklist

Run every morning at 08:00 UTC. Record results in the daily ops log.

#### 1. Infrastructure Health

- [ ] All 14 microservices show `UP` on `/actuator/health`
- [ ] No pod crash-loops in the last 24 h (`kubectl get pods -n bloodbank-prod`)
- [ ] Database connection pool utilization < 70%
- [ ] Redis cache hit rate ≥ 80%
- [ ] RabbitMQ queues draining normally — no accumulation trend
- [ ] DLQ depth = 0 (investigate any messages immediately)
- [ ] No disk usage warnings on any prod node (< 75%)
- [ ] External uptime check ≥ 99.5% for last 24 h (Uptime Robot)

#### 2. Application Health

- [ ] API error rate (5xx) < 0.5% in last 24 h
- [ ] p95 response time < 2 s across all services
- [ ] No P1 or P2 alerts active (if any, link to incident ticket)
- [ ] Grafana alert rules all enabled and targeting correct channels
- [ ] No unacknowledged PagerDuty alerts

#### 3. Data Integrity

- [ ] No audit log gaps (verify `audit_logs` entries exist for all completed collections)
- [ ] Blood unit count consistent with previous day's closing count ± expected activity
- [ ] No orphaned crossmatch or transfusion records

```sql
-- Verify yesterday's collection audit coverage
SELECT COUNT(*) AS collections_yesterday,
       COUNT(al.id) AS audit_entries
FROM collections c
LEFT JOIN audit_logs al
  ON al.entity_id = c.id::text AND al.action = 'CREATE'
WHERE c.created_at >= NOW() - INTERVAL '24 hours';

-- Check for DLQ messages (via RabbitMQ Management API)
-- GET https://rabbitmq.bloodbank.example.com/api/queues/%2F/bloodbank.dlq

-- Blood unit reconciliation
SELECT status, COUNT(*) AS units
FROM blood_units
GROUP BY status
ORDER BY status;
```

#### 4. Security Events

- [ ] No anomalous login failure spikes (> 5% failure rate)
- [ ] No unexpected role assignments or privilege escalations in Keycloak audit
- [ ] No bulk data export events in `audit_logs`

#### 5. Support Ticket Status

- [ ] All open P1/P2 tickets have assigned owners and next action within 2 h
- [ ] No ticket unacknowledged for > 2 h
- [ ] Escalation register up to date

### Daily Status Report Template

Post to `#bloodbank-ops` and email to Branch Management every morning at 09:00:

```
🩸 BloodBank Daily Status — [DATE]

Overall Status: 🟢 HEALTHY / 🟡 DEGRADED / 🔴 OUTAGE

Infrastructure:
  Services UP: 14/14
  API Error Rate (24h): 0.x%
  p95 Response Time: x ms
  Database Pool: x%
  Redis Hit Rate: x%
  RabbitMQ DLQ: 0 messages

Incidents (last 24h):
  P1: 0 | P2: 0 | P3: x | P4: x

Open Tickets: x
  - [TICKET-NNN] Brief description (P2, Owner: Name)

Actions Today:
  1. [Action description] — Owner
  2. [Action description] — Owner

Next report: tomorrow 09:00 UTC
```

### Triage Decision Tree

```
Alert fires
    │
    ├── Is blood safety at risk? (data corruption, wrong crossmatch, wrong issue)
    │       └── YES → P1 immediately, escalate to L3 + L4, consider halt
    │
    ├── Is a core workflow completely broken? (collections, issuing, crossmatch)
    │       └── YES → P1/P2, escalate to L2, begin mitigation
    │
    ├── Is a non-critical feature broken?
    │       └── YES → P3, assign to next sprint, document workaround
    │
    └── Is it cosmetic or minor?
            └── YES → P4, backlog, document in daily report
```

---

## M13-003: Critical Bug SLA (4-Hour Response)

All P1 incidents (critical bugs) follow a strict 4-hour resolution SLA from the moment of detection.

### P1 Definition

A P1 (Critical) incident is ANY of the following:

- System completely unavailable (all users locked out)
- Data corruption or data loss detected
- Blood safety risk: incorrect crossmatch result, wrong blood unit issued, wrong test result returned
- Security breach: unauthorized PHI access, compromised credentials, privilege escalation
- Complete loss of audit trail
- All blood units showing incorrect status

### P1 Response Timeline

| Time | Milestone | Owner |
|---|---|---|
| T+0 | Incident detected (alert or user report) | Monitoring / On-call |
| T+5 min | Acknowledged in PagerDuty, joins `#bloodbank-ops` | L1 On-call |
| T+10 min | Severity confirmed as P1, L2 notified | L1 On-call |
| T+15 min | Incident channel `#incident-NNN` created, commander assigned | L2 DevOps Lead |
| T+30 min | Root cause hypothesis formed, containment action started | L2 + L1 |
| T+60 min | First external status update (status page + email to branch managers) | L3 Project Manager |
| T+2 h | If not resolved: escalate to L4, evaluate rollback | L3 + L4 |
| T+4 h | Resolution deadline — fix deployed or rollback completed | L2 + Commander |
| T+24 h | Post-incident review (PIR) scheduled | L3 |
| T+72 h | PIR document published | Commander |

### P1 Response Checklist

```
□ 1. Acknowledge PagerDuty alert
□ 2. Join #bloodbank-ops Slack channel
□ 3. Post: "Investigating [symptom] since [time]. Severity assessment in progress."
□ 4. Open incident ticket in tracking system
□ 5. Determine blast radius (which services, branches, users affected)
□ 6. Classify P1 if criteria met — notify L2 immediately
□ 7. Create #incident-NNN Slack channel, invite L2, L3
□ 8. Identify root cause using:
     - Grafana dashboards
     - kubectl logs / kubectl describe
     - Database slow query log
     - Distributed traces (Jaeger)
□ 9. Apply containment (restart pod, scale down, circuit breaker, rollback)
□ 10. Verify containment effective — error rate dropping
□ 11. Deploy fix or complete rollback
□ 12. Verify full recovery — all SLOs green
□ 13. Post all-clear to #bloodbank-ops and status page
□ 14. Schedule PIR within 24 h
□ 15. Document in post-incident review template
```

### Post-Incident Review (PIR) Template

```markdown
## Post-Incident Review — P1-[NNN]

**Incident**: [Brief title]
**Date/Time**: [Start] — [End] UTC
**Duration**: [X hours Y minutes]
**Severity**: P1
**Affected**: [Services / branches / users]
**Commander**: [Name]

### Timeline
| Time (UTC) | Event |
|---|---|
| | |

### Root Cause
[What caused the incident?]

### Impact
- Users affected: [N]
- Blood operations impacted: [describe]
- Data integrity: [clean / issue / resolved]
- Compliance impact: [none / HIPAA notification required / etc.]

### Containment
[What actions stopped the bleeding?]

### Resolution
[What fixed the root cause?]

### What Went Well
- ...

### What Went Poorly
- ...

### Action Items
| # | Action | Owner | Due |
|---|---|---|---|
| 1 | | | |

### Sign-off
| Role | Name | Date |
|---|---|---|
| Incident Commander | | |
| DevOps Lead | | |
| Project Manager | | |
```

### Rollback Decision Authority

| Scenario | Decision Authority |
|---|---|
| Single service degraded | L2 DevOps Lead |
| Multiple services degraded | L3 Project Manager + Technical Lead |
| Data integrity issue | L3 + L4 joint decision |
| Full system outage > 2 h | L4 Executive Sponsor |

Rollback procedure: see `docs/operations/runbooks/runbook-rollback.md`

---

## M13-004: Alert Tuning Schedule

Alert rules configured at launch are based on load testing estimates. Real production traffic patterns will differ. A structured tuning schedule ensures alerts remain actionable and noise is minimized.

### Alert Tuning Cadence

| Week | Activity | Owner |
|---|---|---|
| Week 1, Day 3 | First alert audit — count false positives and missed incidents | SRE Lead |
| Week 1, Day 7 | Adjust thresholds based on observed baselines | SRE Lead |
| Week 2, Day 10 | Second alert audit | SRE Lead |
| Week 2, Day 14 | Final stabilization-period alert review and sign-off | DevOps Lead |
| Month 2 onward | Monthly alert review (first Wednesday each month) | SRE Lead |

### Alert Audit Process

For each configured alert rule:

1. **Review** — Check firing history for the past 7 days
2. **Classify** — True positive, false positive, or missed (no alert when issue occurred)
3. **Tune** — Adjust threshold, evaluation window, or routing
4. **Document** — Record change in alert changelog

### Alert Audit Worksheet

| Alert Name | Fires (7d) | True Positive | False Positive | Missed | Action |
|---|---|---|---|---|---|
| `api_error_rate_high` | | | | | Adjust threshold |
| `api_latency_p95_high` | | | | | — |
| `db_pool_exhaustion` | | | | | — |
| `redis_hit_rate_low` | | | | | — |
| `rabbitmq_queue_depth` | | | | | — |
| `jvm_heap_critical` | | | | | — |
| `pod_crash_loop` | | | | | — |
| `disk_usage_high` | | | | | — |
| `keycloak_auth_failures` | | | | | — |
| `dlq_messages_present` | | | | | — |

**False positive target**: < 5% of total alert firings
**Missed incident target**: 0

### Baseline Establishment

By end of Week 2, document the following baselines for use in ongoing operations:

| Metric | P5 | P50 (Median) | P95 | P99 | Peak (observed) |
|---|---|---|---|---|---|
| API response time (ms) | | | | | |
| API error rate (%) | | | | | |
| DB connection pool (%) | | | | | |
| Redis hit rate (%) | | | | | |
| RabbitMQ throughput (msg/min) | | | | | |
| Collections/hour | | | | | |
| Active concurrent users | | | | | |

These baselines replace the pre-launch estimates used in initial alert configuration.

### Alert Changelog

Maintain a chronological log of all alert rule changes:

| Date | Alert Rule | Change | Reason | Author |
|---|---|---|---|---|
| | | | | |

---

## Stabilization Exit Criteria

The stabilization period concludes at end of Week 2 when all of the following are met:

| Criterion | Target | Verified |
|---|---|---|
| API availability | ≥ 99.5% over 14 days | ☐ |
| Zero P1 incidents | 3 consecutive days with no P1 | ☐ |
| P2 incidents resolved | All P2s resolved, no recurring pattern | ☐ |
| Alert baseline documented | All thresholds updated from real traffic | ☐ |
| PIRs complete | PIR published for every P1 and P2 | ☐ |
| Runbooks updated | Gaps found during stabilization are documented | ☐ |
| Team confidence | SRE Lead sign-off on handoff to ongoing-ops | ☐ |

**Sign-off**: Stabilization exit approved by DevOps Lead + Project Manager.

Transition to: `docs/operations/ongoing-operations.md`

---

*Related documents:*
- *`docs/operations/hypercare-plan.md` — Pilot-phase hypercare (M10)*
- *`docs/operations/incident-response.md` — Full incident response playbook*
- *`docs/operations/runbooks/runbook-rollback.md` — Rollback procedures*
- *`docs/operations/ongoing-operations.md` — Post-stabilization operational cadence*
