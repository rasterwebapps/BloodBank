# BloodBank — SRE Guide

**Last Updated**: 2026-04-24
**Milestone**: M13 — Post-Launch & Continuous Improvement
**Issues**: M13-029 to M13-033
**Owner**: Platform / SRE Team

---

## Table of Contents

1. [SLO Definitions](#1-slo-definitions)
2. [Error Budget Calculation & Tracking](#2-error-budget-calculation--tracking)
3. [Burn Rate Alerting](#3-burn-rate-alerting)
4. [Chaos Engineering Experiment Catalog](#4-chaos-engineering-experiment-catalog)
5. [Capacity Planning Model](#5-capacity-planning-model)
6. [Cost Optimisation Guide](#6-cost-optimisation-guide)

---

## 1. SLO Definitions

**(M13-029 — Define and track SLOs for all services)**

### 1.1 SLO Philosophy

BloodBank is a healthcare-critical system. Blood unit availability, crossmatch
turnaround, and emergency request processing are safety-critical paths. SLOs for these
paths are deliberately stricter than typical SaaS products.

**SLO Review cadence**: Monthly — presented at the Operations Review meeting.
**SLO breach escalation**: PagerDuty P1 + BRANCH_ADMIN email within 5 minutes of
sustained breach.

### 1.2 SLO Definitions per Service

All latency SLOs are measured at the 99th percentile (p99) of HTTP response times
for non-cached, authenticated requests, as scraped by Prometheus from the Istio sidecar
metrics or Spring Boot Actuator `/actuator/prometheus`.

All availability SLOs are calculated as:

```
Availability = (total_requests - error_requests) / total_requests × 100
```

where `error_requests` is any HTTP 5xx response or TCP connection timeout.

---

#### 1.2.1 api-gateway

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.95% | 30-day rolling |
| p99 latency (proxy overhead only) | < 50 ms | 30-day rolling |
| p50 latency | < 10 ms | 30-day rolling |

**Notes**: The gateway is a single point of entry; its availability sets the ceiling
for all services. Dual-AZ deployment with at least 2 replicas required at all times.

---

#### 1.2.2 donor-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| p99 latency — donor registration | < 500 ms | 30-day rolling |
| p99 latency — donor search | < 300 ms | 30-day rolling |
| p99 latency — eligibility check | < 200 ms | 30-day rolling |
| Camp collection sync error rate | < 0.1% | 7-day rolling |

---

#### 1.2.3 inventory-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.95% | 30-day rolling |
| p99 latency — stock level query | < 150 ms | 30-day rolling |
| p99 latency — unit reservation | < 300 ms | 30-day rolling |
| p99 latency — unit issue | < 500 ms | 30-day rolling |
| Cold chain alert delivery | < 2 min from sensor excursion | Event-based |

**Notes**: Stock queries are served from Redis cache; cache-miss p99 may reach 400 ms
during Redis restarts. Alert on sustained cache miss rate > 5%.

---

#### 1.2.4 lab-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| p99 latency — test order creation | < 400 ms | 30-day rolling |
| p99 latency — result retrieval | < 200 ms | 30-day rolling |
| Mandatory test panel completion rate | > 99.5% within 6 hours of collection | Daily |

**Notes**: The 6-hour mandatory panel SLO maps to regulatory requirement. Breach
triggers an automatic escalation to BRANCH_ADMIN.

---

#### 1.2.5 transfusion-service (crossmatch & issue)

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.95% | 30-day rolling |
| p99 latency — crossmatch request | < 600 ms | 30-day rolling |
| p99 latency — emergency blood issue | < 300 ms | 30-day rolling |
| Crossmatch SLA breach rate | < 0.5% (> 72 h without result) | Daily |

**Notes**: Emergency blood issue is the most safety-critical path in the system.
A p99 > 300 ms warrants immediate investigation regardless of overall error rate.

---

#### 1.2.6 hospital-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| p99 latency — blood request creation | < 400 ms | 30-day rolling |
| Request fulfilment acknowledgement | < 4 hours (operational SLO) | Daily |

---

#### 1.2.7 billing-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.5% | 30-day rolling |
| p99 latency — invoice generation | < 1 s | 30-day rolling |
| p99 latency — payment webhook | < 500 ms | 30-day rolling |
| Invoice generation failure rate | < 0.01% | 30-day rolling |

---

#### 1.2.8 notification-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.5% | 30-day rolling |
| Email delivery latency (p95) | < 60 s from event | 7-day rolling |
| SMS delivery latency (p95) | < 30 s from event | 7-day rolling |
| Push delivery latency (p95) | < 10 s from event | 7-day rolling |
| Notification delivery success rate | > 99% (per channel) | 7-day rolling |

**Notes**: Measured via `notifications.status` column transitions logged to
`notification_service_delivery_metrics` (custom Prometheus metric scraped from
Actuator). Delivery failures are retried up to 3 times before recording as `FAILED`.

---

#### 1.2.9 request-matching-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| p99 latency — match query | < 500 ms | 30-day rolling |
| Emergency match time (operational) | < 15 min from request | Event-based |

---

#### 1.2.10 reporting-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99% | 30-day rolling |
| p99 latency — dashboard data | < 2 s | 30-day rolling |
| Scheduled report delivery (p99) | < 10 min from scheduled time | Daily |

---

#### 1.2.11 branch-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| p99 latency — branch lookup (cached) | < 50 ms | 30-day rolling |

---

#### 1.2.12 document-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.5% | 30-day rolling |
| p99 latency — document download | < 3 s | 30-day rolling |
| p99 latency — document upload | < 5 s | 30-day rolling |

---

#### 1.2.13 compliance-service

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.5% | 30-day rolling |
| Audit log write latency (p99) | < 200 ms | 30-day rolling |

---

#### 1.2.14 config-server

| SLI | Target | Measurement Window |
|-----|--------|--------------------|
| Availability | 99.9% | 30-day rolling |
| Config refresh latency (p99) | < 5 s | 30-day rolling |

**Notes**: Config server is only called at service startup and on `/actuator/refresh`
events. Low request volume; high availability is critical during rolling deployments.

---

### 1.3 SLO Measurement Infrastructure

```
Services
  │  Prometheus metrics
  ▼
Prometheus (scrape interval: 15 s)
  │  PromQL queries
  ▼
Grafana — SLO Dashboard (bloodbank-slo-dashboard.json)
  │
  ├── Availability gauge (30-day rolling window)
  ├── Latency heatmap (p50 / p95 / p99)
  ├── Error rate time-series
  └── Error budget burn rate panel
```

**Key Prometheus queries:**

```promql
# Availability — 30-day rolling (example for donor-service)
1 - (
  sum(rate(http_server_requests_seconds_count{
    service="donor-service", status=~"5.."
  }[30d]))
  /
  sum(rate(http_server_requests_seconds_count{
    service="donor-service"
  }[30d]))
)

# p99 latency — donor registration
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket{
    service="donor-service",
    uri="/api/v1/donors",
    method="POST"
  }[5m])) by (le)
)
```

---

## 2. Error Budget Calculation & Tracking

**(M13-030 — Implement error budgets and burn rate alerting)**

### 2.1 Concept

An **error budget** is the maximum allowed downtime / error volume within the SLO
measurement window before the SLO is breached. It provides a quantitative signal for
when to slow down releases and focus on reliability.

```
Error Budget (minutes) = (1 - SLO Target) × Window Duration
```

**Example — api-gateway (99.95% / 30 days):**

```
Error Budget = (1 - 0.9995) × 30 × 24 × 60
             = 0.0005 × 43,200
             = 21.6 minutes per 30-day window
```

### 2.2 Error Budgets per Service (30-day window)

| Service | SLO Target | Error Budget (min) | Error Budget (requests @ 100 RPS) |
|---------|------------|-------------------|-----------------------------------|
| api-gateway | 99.95% | 21.6 | 25,920 |
| inventory-service | 99.95% | 21.6 | 25,920 |
| transfusion-service | 99.95% | 21.6 | 25,920 |
| donor-service | 99.9% | 43.2 | 51,840 |
| lab-service | 99.9% | 43.2 | 51,840 |
| request-matching-service | 99.9% | 43.2 | 51,840 |
| branch-service | 99.9% | 43.2 | 51,840 |
| hospital-service | 99.9% | 43.2 | 51,840 |
| billing-service | 99.5% | 216 | 259,200 |
| notification-service | 99.5% | 216 | 259,200 |
| document-service | 99.5% | 216 | 259,200 |
| compliance-service | 99.5% | 216 | 259,200 |
| reporting-service | 99% | 432 | 518,400 |
| config-server | 99.9% | 43.2 | 51,840 |

### 2.3 Error Budget Policy

| Budget Remaining | Action |
|-----------------|--------|
| > 50% | Normal release cadence; changes can proceed |
| 25% – 50% | Increase deployment risk review; no experimental changes |
| 10% – 25% | Freeze non-critical deployments; reliability work prioritised in sprint |
| < 10% | Full deployment freeze; on-call team focused on reliability improvements |
| 0% (SLO breached) | Incident declared; post-mortem required within 72 hours |

### 2.4 Error Budget Tracking Dashboard

A dedicated Grafana dashboard (`bloodbank-error-budget.json`) shows for each service:

- Remaining error budget as a percentage (30-day rolling)
- Cumulative error minutes consumed this window
- Projected end-of-window status (linear burn rate extrapolation)
- Historical burn rate chart (last 6 windows)

```promql
# Error budget remaining % — donor-service
(
  1 - (
    sum(increase(http_server_requests_seconds_count{
      service="donor-service", status=~"5.."
    }[30d]))
    /
    sum(increase(http_server_requests_seconds_count{
      service="donor-service"
    }[30d]))
  )
) / (1 - 0.999) * 100
```

---

## 3. Burn Rate Alerting

**(M13-030 — continued)**

### 3.1 Burn Rate Concept

**Burn rate** measures how quickly the error budget is being consumed relative to
the "safe" rate. A burn rate of 1.0 means the budget is being consumed at exactly
the rate that would exhaust it at the end of the window. A burn rate of 10.0 means
it is being consumed 10× faster — the budget will be exhausted in 1/10th of the window.

```
Burn Rate = (current error rate) / (1 - SLO target)
```

**Example**: If donor-service SLO is 99.9% and the current 1-hour error rate is 2%:
```
Burn Rate = 0.02 / 0.001 = 20
```
At burn rate 20, the monthly error budget is exhausted in `30 days / 20 = 1.5 days`.

### 3.2 Multi-Window Burn Rate Alert Strategy

Google's SRE Workbook recommends a multi-window approach to reduce both false positives
(short spikes) and false negatives (slow burns). BloodBank adopts a 3-tier alert model:

| Alert Tier | Short Window | Long Window | Burn Rate | Action |
|------------|-------------|-------------|-----------|--------|
| Page (P1) | 5 min | 1 hour | > 14.4× | Wake on-call immediately |
| Ticket (P2) | 30 min | 6 hours | > 6× | Create ticket; investigate within 2 hours |
| Warning (P3) | 2 hours | 24 hours | > 3× | Engineer review within business hours |

**Rationale**: At 14.4× burn rate, the 30-day error budget is exhausted in 2 days.
At 6×, it is exhausted in 5 days. At 3×, it is exhausted in 10 days.

### 3.3 Prometheus Alert Rules

```yaml
# alerting/slo-burn-rate.yaml
groups:
  - name: bloodbank.slo.donor-service
    rules:
      # P1 — Fast burn (>14.4× over 5 min AND 1 h)
      - alert: DonorServiceHighBurnRate
        expr: |
          (
            sum(rate(http_server_requests_seconds_count{
              service="donor-service", status=~"5.."
            }[5m]))
            /
            sum(rate(http_server_requests_seconds_count{
              service="donor-service"
            }[5m]))
          ) > (14.4 * 0.001)
          and
          (
            sum(rate(http_server_requests_seconds_count{
              service="donor-service", status=~"5.."
            }[1h]))
            /
            sum(rate(http_server_requests_seconds_count{
              service="donor-service"
            }[1h]))
          ) > (14.4 * 0.001)
        for: 2m
        labels:
          severity: critical
          service: donor-service
        annotations:
          summary: "Donor Service SLO fast burn rate"
          description: >
            donor-service is burning its error budget at >14.4× the safe rate.
            Current 5m error rate: {{ $value | humanizePercentage }}.
            At this rate, the 30-day error budget will be exhausted in <2 days.
          runbook_url: "https://wiki.bloodbank.internal/runbooks/service-high-error-rate"

      # P2 — Slow burn (>6× over 30 min AND 6 h)
      - alert: DonorServiceMediumBurnRate
        expr: |
          (
            sum(rate(http_server_requests_seconds_count{
              service="donor-service", status=~"5.."
            }[30m]))
            /
            sum(rate(http_server_requests_seconds_count{
              service="donor-service"
            }[30m]))
          ) > (6 * 0.001)
          and
          (
            sum(rate(http_server_requests_seconds_count{
              service="donor-service", status=~"5.."
            }[6h]))
            /
            sum(rate(http_server_requests_seconds_count{
              service="donor-service"
            }[6h]))
          ) > (6 * 0.001)
        for: 5m
        labels:
          severity: warning
          service: donor-service
        annotations:
          summary: "Donor Service SLO medium burn rate"
          description: >
            donor-service is burning its error budget at >6× the safe rate.
            At this rate, the 30-day error budget will be exhausted in <5 days.
```

> **Note**: Replicate the above alert group for each service, substituting the service
> name and the SLO target in the burn rate threshold expressions.
>
> For 99.95% SLO services (api-gateway, inventory-service, transfusion-service),
> replace `0.001` with `0.0005` in all expressions.

### 3.4 Alert Routing (Alertmanager)

```yaml
# alertmanager/config.yaml (excerpt)
route:
  group_by: [service, severity]
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: default
  routes:
    - match:
        severity: critical
      receiver: pagerduty-critical
      continue: true
    - match:
        severity: warning
      receiver: slack-sre-channel

receivers:
  - name: pagerduty-critical
    pagerduty_configs:
      - service_key: ${PAGERDUTY_SERVICE_KEY}
        description: '{{ .GroupLabels.service }} — {{ .Annotations.summary }}'
        client_url: '{{ .Annotations.runbook_url }}'

  - name: slack-sre-channel
    slack_configs:
      - api_url: ${SLACK_WEBHOOK_URL}
        channel: '#sre-alerts'
        title: '{{ .GroupLabels.severity | toUpper }} — {{ .GroupLabels.service }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

---

## 4. Chaos Engineering Experiment Catalog

**(M13-031 — Chaos engineering experiments)**

### 4.1 Principles

BloodBank follows the [Principles of Chaos Engineering](https://principlesofchaos.org/):

1. **Build a hypothesis around steady-state behaviour** — define what "normal" looks
   like using SLI metrics before each experiment.
2. **Vary real-world events** — simulate realistic failures (pod crashes, network
   latency, disk full) not synthetic ones.
3. **Run experiments in production (with safeguards)** — or a production-equivalent
   staging environment.
4. **Minimise blast radius** — start with a single pod, single branch, limited traffic.
5. **Automate experiments** — use LitmusChaos or Chaos Mesh on a schedule.

### 4.2 Prerequisites

- [ ] All SLOs and alert rules are active (Section 1 and 3).
- [ ] On-call engineer is aware and available during the experiment window.
- [ ] A documented rollback plan exists for each experiment.
- [ ] Experiments are never run during: business-critical windows (peak donation hours
      08:00–18:00 local), holidays, or within 24 hours of a production deployment.
- [ ] Emergency stop procedure: `kubectl delete chaosexperiment <name> -n chaos` or
      delete the LitmusChaos ChaosEngine resource.

### 4.3 Experiment Catalog

---

#### EXP-001 — Random Pod Kill (Pod Failure)

**Tool**: LitmusChaos `pod-delete`
**Hypothesis**: If one replica of donor-service is killed, Kubernetes reschedules it
within 60 seconds and availability remains > 99.9%.

```yaml
apiVersion: litmuschaos.io/v1alpha1
kind: ChaosEngine
metadata:
  name: donor-pod-delete
  namespace: bloodbank
spec:
  appinfo:
    appns: bloodbank
    applabel: app=donor-service
    appkind: deployment
  chaosServiceAccount: litmus-admin
  experiments:
    - name: pod-delete
      spec:
        components:
          env:
            - name: TOTAL_CHAOS_DURATION
              value: "60"   # seconds
            - name: CHAOS_INTERVAL
              value: "10"   # kill one pod every 10 s
            - name: FORCE
              value: "false"
            - name: PODS_AFFECTED_PERC
              value: "33"   # kill 1 of 3 replicas
```

**Steady-state checks**:
- HTTP 200 rate on `POST /api/v1/donors` > 99%
- p99 latency < 1 s (double normal SLO to account for restart delay)

**Expected result**: Load balancer routes traffic to remaining replicas during the
kill; new pod is healthy within 60 seconds; no 5xx errors visible to end users
beyond a brief spike (< 5 s).

**Rollback**: Delete the ChaosEngine resource. The experiment auto-terminates after
`TOTAL_CHAOS_DURATION`.

---

#### EXP-002 — Pod Kill — inventory-service (Stock Query Path)

**Hypothesis**: If inventory-service restarts, Redis cache serves stock level queries
with no user-visible error for the first 30 seconds, after which the rebuilt pod
serves fresh DB queries.

Same configuration as EXP-001 with `applabel: app=inventory-service`.

**Additional check**: Redis `KEYS bloodbank:stock:*` count does not drop to zero
during restart (TTL-based cache survival).

---

#### EXP-003 — Network Latency Injection

**Tool**: LitmusChaos `pod-network-latency`
**Hypothesis**: With 200 ms added latency on donor-service → PostgreSQL connection,
the p99 API latency degrades gracefully (< 1 s) and no timeout-induced 5xx errors occur.

```yaml
experiments:
  - name: pod-network-latency
    spec:
      components:
        env:
          - name: NETWORK_INTERFACE
            value: "eth0"
          - name: NETWORK_LATENCY
            value: "200"     # ms
          - name: JITTER
            value: "50"      # ms
          - name: TOTAL_CHAOS_DURATION
            value: "120"
          - name: DESTINATION_IPS
            value: "${POSTGRES_CLUSTER_IP}"
```

**Expected result**: p99 latency on donor registration rises to ~700 ms but stays
below the 1 s timeout defined in HikariCP (`connection-timeout: 30000` — no breaches).
Error rate < 0.1%.

**If hypothesis fails**: Connection pool exhaustion is suspected. Review HikariCP
`maximumPoolSize` and `connectionTimeout` settings; consider circuit breaker via
Resilience4j.

---

#### EXP-004 — Network Partition (Split-Brain)

**Tool**: LitmusChaos `pod-network-partition`
**Hypothesis**: If notification-service is partitioned from RabbitMQ for 5 minutes,
messages accumulate in the DLQ and are processed within 10 minutes of partition
resolution, with no duplicate processing.

```yaml
experiments:
  - name: pod-network-partition
    spec:
      components:
        env:
          - name: TOTAL_CHAOS_DURATION
            value: "300"   # 5 minutes
          - name: DESTINATION_IPS
            value: "${RABBITMQ_SERVICE_IP}"
          - name: POLICY
            value: "DROP"
```

**Checks after partition resolves**:
- `bloodbank.dlq` depth returns to 0 within 10 minutes.
- `notifications` table shows no duplicate entries (idempotency key check).
- No `ConcurrentConsumerException` in notification-service logs.

---

#### EXP-005 — Disk I/O Stress (PostgreSQL Node)

**Tool**: LitmusChaos `node-io-stress` (requires node-level chaos agent)
**Hypothesis**: With 80% disk I/O utilisation on the PostgreSQL primary node, query
latency degrades but connection pool does not exhaust and write operations succeed.

```yaml
experiments:
  - name: node-io-stress
    spec:
      components:
        env:
          - name: TOTAL_CHAOS_DURATION
            value: "120"
          - name: FILESYSTEM_UTILIZATION_PERCENTAGE
            value: "80"
          - name: NUMBER_OF_WORKERS
            value: "4"
          - name: NODE_LABEL
            value: "role=postgres-primary"
```

**Expected result**: Write query p99 rises to < 5 s; no `statement_timeout` errors
(timeout is set to 30 s in PostgreSQL); Pgbouncer connection queue < 50.

**Rollback**: Auto-terminates. If PostgreSQL enters recovery mode, trigger a manual
failover to the standby using `pg_ctl promote`.

---

#### EXP-006 — Redis Failure (Cache Eviction)

**Tool**: `kubectl exec` + `redis-cli FLUSHALL` (controlled)
**Hypothesis**: Flushing the Redis cache causes a 5–10× increase in database load
for 60 seconds while the cache warms, but no 5xx errors occur and the database does
not reach 90% CPU.

**Steps**:
1. Record baseline Prometheus metrics: Redis hit rate, PostgreSQL CPU.
2. Execute `kubectl exec -n bloodbank redis-0 -- redis-cli FLUSHALL`.
3. Monitor for 5 minutes: error rate, database CPU, Redis hit rate recovery.
4. Confirm Redis hit rate returns to > 90% within 5 minutes.

**Expected result**: Cache miss spike causes < 30% increase in PostgreSQL CPU.
No 5xx errors. Redis hit rate recovers to > 90% within 5 minutes as the warm-up
query pattern re-populates common keys.

---

#### EXP-007 — Keycloak Unavailability

**Tool**: Scale Keycloak deployment to 0 replicas
**Hypothesis**: If Keycloak is unreachable for 2 minutes, existing valid JWT tokens
continue to work (services cache the public key and validate locally) and no 401
errors are seen for users with unexpired tokens.

```bash
# Start experiment
kubectl scale deployment keycloak -n auth --replicas=0

# Run for 2 minutes, then restore
sleep 120
kubectl scale deployment keycloak -n auth --replicas=2
```

**Checks during outage**:
- Requests with valid, non-expired JWTs: HTTP 200 (expected — JWT validation is local).
- Requests with expired JWTs or no JWT: HTTP 401 (expected).
- New login attempts: HTTP 503 (expected — Keycloak UI unreachable).

**Recovery check**: Within 60 seconds of Keycloak restoration, new logins succeed.

---

#### EXP-008 — RabbitMQ Broker Restart

**Tool**: `kubectl rollout restart statefulset/rabbitmq -n bloodbank`
**Hypothesis**: During a RabbitMQ broker restart (< 60 s), publishers retry with
exponential back-off and no events are lost (durable queues, publisher confirms).

**Checks**:
- `bloodbank.events` exchange and all queues survive restart (durable=true).
- `notification-service` logs show retry attempts, not dropped events.
- `bloodbank.dlq` depth does not increase (no max-delivery reached during brief outage).
- All events published before restart are consumed after restart (no data loss).

---

### 4.4 Experiment Scheduling

| Experiment | Frequency | Scheduled Window | Owner |
|-----------|-----------|-----------------|-------|
| EXP-001 (Pod Kill — donor) | Weekly | Friday 14:00–15:00 UTC | SRE |
| EXP-002 (Pod Kill — inventory) | Weekly | Friday 14:00–15:00 UTC | SRE |
| EXP-003 (Network Latency) | Monthly | Last Thursday 10:00–11:00 UTC | SRE |
| EXP-004 (Network Partition) | Monthly | Last Thursday 11:00–12:00 UTC | SRE |
| EXP-005 (Disk I/O Stress) | Quarterly | Announced 48 h in advance | SRE + DBA |
| EXP-006 (Redis Flush) | Monthly | Tuesday 09:00–09:30 UTC | SRE |
| EXP-007 (Keycloak Down) | Quarterly | Announced 48 h in advance | SRE + IAM |
| EXP-008 (RabbitMQ Restart) | Monthly | Wednesday 09:00–09:30 UTC | SRE |

### 4.5 Post-Experiment Review

After each experiment, the running engineer files a brief report in the
`#chaos-engineering` Slack channel:

```
Experiment: EXP-001
Date: 2026-05-01 14:00 UTC
Duration: 60 s
Hypothesis confirmed: YES / NO
Steady-state metric (error rate): X.XX% (target < 0.1%)
Unexpected behaviour: <description or "None">
Action items: <tickets created or "None">
```

If the hypothesis was **not confirmed**, a follow-up ticket is created with `sre`
and `reliability` labels before the next sprint.

---

## 5. Capacity Planning Model

**(M13-032 — Capacity planning based on growth projections)**

### 5.1 Traffic Model

**Input variables**:

| Variable | Symbol | Initial (Launch) | Year 1 | Year 2 | Year 3 |
|----------|--------|-----------------|--------|--------|--------|
| Active branches | B | 10 | 50 | 150 | 300 |
| Donors per branch / day | D | 20 | 30 | 40 | 50 |
| Staff concurrent users per branch | S | 5 | 8 | 10 | 12 |
| Hospital requests per branch / day | H | 10 | 20 | 30 | 40 |
| Peak-to-average ratio | PAR | 3× | 3× | 3× | 3× |

**Peak RPS estimate**:

```
Total daily transactions ≈ B × (D × 15 API calls + S × 100 API calls + H × 5 API calls)

Year 1 (50 branches):
  = 50 × (30 × 15 + 8 × 100 + 20 × 5)
  = 50 × (450 + 800 + 100)
  = 50 × 1,350
  = 67,500 transactions/day
  = 0.78 TPS average
  Peak RPS = 0.78 × 3 × (1/3600 × 3600) = 2.35 RPS (trivial)

Year 3 (300 branches, 50 donors/day, 12 staff):
  = 300 × (50 × 15 + 12 × 100 + 40 × 5)
  = 300 × (750 + 1200 + 200)
  = 300 × 2,150
  = 645,000 transactions/day
  = 7.47 TPS average
  Peak RPS = 7.47 × 3 = 22.4 RPS
```

These are modest request rates. The system is database-write-heavy (blood unit
tracking, audit logs) rather than compute-heavy.

### 5.2 Kubernetes Resource Requirements

Resource requirements per service replica (measured via Kubernetes metrics-server
at 20% load, then scaled):

| Service | CPU Request | CPU Limit | Mem Request | Mem Limit | Min Replicas | Max Replicas (HPA) |
|---------|------------|-----------|-------------|-----------|-------------|---------------------|
| api-gateway | 250m | 1000m | 256Mi | 512Mi | 2 | 8 |
| donor-service | 250m | 500m | 256Mi | 512Mi | 2 | 6 |
| inventory-service | 500m | 1000m | 512Mi | 1Gi | 2 | 6 |
| lab-service | 250m | 500m | 256Mi | 512Mi | 2 | 4 |
| transfusion-service | 250m | 500m | 256Mi | 512Mi | 2 | 4 |
| hospital-service | 250m | 500m | 256Mi | 512Mi | 2 | 4 |
| billing-service | 250m | 500m | 256Mi | 512Mi | 1 | 4 |
| request-matching-service | 500m | 1000m | 512Mi | 1Gi | 2 | 6 |
| notification-service | 250m | 500m | 256Mi | 512Mi | 2 | 4 |
| reporting-service | 500m | 2000m | 512Mi | 2Gi | 1 | 4 |
| branch-service | 250m | 500m | 256Mi | 512Mi | 2 | 4 |
| document-service | 250m | 500m | 256Mi | 512Mi | 1 | 4 |
| compliance-service | 250m | 500m | 256Mi | 512Mi | 1 | 4 |
| config-server | 250m | 500m | 256Mi | 512Mi | 2 | 2 |

**HPA scaling trigger**: 70% CPU utilisation or 80% memory utilisation, whichever
is reached first.

### 5.3 Stateful Component Sizing

#### PostgreSQL 17

| Parameter | Launch | Year 1 | Year 2 | Year 3 |
|-----------|--------|--------|--------|--------|
| Instance type (AWS RDS) | db.t3.medium | db.t3.large | db.r6g.large | db.r6g.xlarge |
| vCPU | 2 | 2 | 2 | 4 |
| RAM | 4 GB | 8 GB | 16 GB | 32 GB |
| Storage | 100 GB gp3 | 500 GB gp3 | 1 TB io1 | 2 TB io1 |
| IOPS | 3,000 | 6,000 | 10,000 | 20,000 |
| Read replicas | 0 | 1 (reporting) | 2 | 2 |

**Estimated DB row growth** (conservative):
- `blood_units`: 50 branches × 50 units/day × 365 = ~913,000 rows/year
- `audit_logs`: ~10 audit events per transaction × total transactions/year ≈ 6.5M rows/year
- `notifications`: ~3 notifications per donor/day × 50D × B × 365 ≈ high volume → partition by month

#### Redis 7

| Parameter | Launch | Year 3 |
|-----------|--------|--------|
| Instance | cache.t3.micro | cache.r6g.large |
| Memory | 0.5 GB | 13 GB |
| Cluster mode | Disabled (single shard) | Enabled (3 shards) |
| Max memory policy | `allkeys-lru` | `allkeys-lru` |

Primary cached key families:
- `bloodbank:stock:{branchId}:{bloodGroup}` — current unit counts (TTL: 60 s)
- `bloodbank:branch:{branchId}` — branch metadata (TTL: 3600 s)
- `bloodbank:user:session:{userId}` — session data (TTL: 1800 s)

#### RabbitMQ 3.13+

| Parameter | Launch | Year 3 |
|-----------|--------|--------|
| Nodes | 3 (quorum queues) | 3 (scale vertically) |
| Instance | t3.medium | r6g.large |
| Message rate (peak) | ~10 msg/s | ~100 msg/s |
| Queue depth (normal) | < 100 | < 1,000 |

### 5.4 Scaling Triggers & Runbook References

| Trigger | Threshold | Action | Runbook |
|---------|-----------|--------|---------|
| PostgreSQL CPU > 80% sustained 10 min | — | Scale up instance or read replica | `runbook-database-issues.md` |
| Redis memory > 80% | — | Increase instance size | `runbook-service-down.md` |
| HPA at max replicas + CPU > 90% | — | Increase `maxReplicas` in HPA manifest | SRE escalation |
| Disk > 80% on any PV | — | Expand PVC (EBS resize, no downtime) | SRE escalation |
| RabbitMQ queue depth > 10,000 sustained | — | Investigate consumer lag; scale notification-service | `runbook-high-error-rate.md` |

---

## 6. Cost Optimisation Guide

**(M13-033 — Cost optimisation: right-size K8s, reserved instances, spot instances)**

### 6.1 Overview

BloodBank is deployed on AWS (primary) with EKS for Kubernetes workloads, RDS for
PostgreSQL, ElastiCache for Redis, and AmazonMQ (or self-managed RabbitMQ on EC2)
for messaging. The following recommendations apply at Year 1 scale
(approximately 50 branches, 22 RPS peak).

### 6.2 Right-Sizing Kubernetes Workloads

**Step 1 — Measure actual usage**

```bash
# View current CPU/memory usage vs. requests/limits
kubectl top pods -n bloodbank --sort-by=cpu
kubectl describe nodes | grep -A 5 "Allocated resources"
```

Use the Kubernetes VPA (Vertical Pod Autoscaler) in **recommendation mode** (not
auto-apply) for 2 weeks after launch to gather actual usage data:

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: donor-service-vpa
  namespace: bloodbank
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: donor-service
  updatePolicy:
    updateMode: "Off"   # Recommendation only; do not auto-apply
```

Review VPA recommendations weekly in the Grafana VPA dashboard.

**Step 2 — Identify over-provisioned services**

A service is over-provisioned if its actual CPU usage is consistently < 30% of its
request. Common candidates at launch:

| Service | Likely over-provisioned? | Recommended action |
|---------|------------------------|---------------------|
| reporting-service | Yes (low traffic) | Reduce CPU request from 500m → 250m; reduce replicas to 1 |
| billing-service | Yes | Reduce min replicas to 1 |
| compliance-service | Yes | Reduce min replicas to 1 |
| document-service | Yes | Reduce min replicas to 1 |
| config-server | No — keep 2 for HA | No change |

**Step 3 — Use Namespace Resource Quotas**

Prevent individual service over-consumption during incidents (e.g., runaway retry loop):

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: bloodbank-quota
  namespace: bloodbank
spec:
  hard:
    requests.cpu: "20"
    requests.memory: "20Gi"
    limits.cpu: "40"
    limits.memory: "40Gi"
    pods: "100"
```

### 6.3 Reserved Instances (AWS)

Reserved Instances (RIs) provide up to 72% cost savings over On-Demand pricing for
predictable, always-on workloads.

**Strategy**: Purchase 1-year Standard RIs for baseline capacity; use On-Demand for
burst above the baseline.

| Resource | On-Demand Cost/mo (est.) | 1-yr RI Savings | Recommended RI |
|----------|------------------------|-----------------|----------------|
| RDS db.t3.large (PostgreSQL) | ~$120 | ~40% | 1× 1-yr Standard RI |
| RDS db.t3.medium (read replica) | ~$60 | ~40% | 1× 1-yr Standard RI |
| ElastiCache cache.t3.medium | ~$50 | ~35% | 1× 1-yr Standard RI |
| EKS worker node m5.large (×3) | ~$315 | ~40% | 3× 1-yr Convertible RI |
| EKS worker node c5.xlarge (×2, for reporting/gateway) | ~$280 | ~40% | 2× 1-yr Convertible RI |

**Use Convertible RIs for EC2** to retain flexibility to exchange for a different
instance type as capacity needs change.

**RI Purchase timing**: Purchase after 4 weeks of production data. Use AWS Cost
Explorer's RI recommendations feature to identify the optimal RI configuration
based on actual usage patterns.

### 6.4 Spot Instances

Spot Instances are appropriate for **stateless, interruptible workloads** that can
tolerate a 2-minute termination notice.

#### Suitable for Spot

| Workload | Why suitable |
|---------|-------------|
| reporting-service | Batch report jobs; interruption delays delivery by minutes, not seconds |
| Chaos experiment pods (LitmusChaos) | Intentionally ephemeral |
| CI/CD build agents (Jenkins agents) | Interrupt-resilient; retry logic built in |
| Load testing pods (k6 agents) | Non-production; restartable |

#### NOT suitable for Spot

| Workload | Why not suitable |
|---------|-----------------|
| api-gateway | Always-on; interruption causes service outage |
| transfusion-service | Patient safety; interruption during crossmatch unacceptable |
| PostgreSQL | Stateful; use RDS reserved instead |
| RabbitMQ | Stateful quorum queues; interruption causes split-brain risk |
| Keycloak | Authentication; interruption logs out all users |

#### Spot Configuration for reporting-service

```yaml
# k8s/reporting-service/deployment.yaml (spot node pool addition)
spec:
  template:
    spec:
      nodeSelector:
        eks.amazonaws.com/capacityType: SPOT
      tolerations:
        - key: "spot"
          operator: "Exists"
          effect: "NoSchedule"
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: DoNotSchedule
          labelSelector:
            matchLabels:
              app: reporting-service
```

### 6.5 Storage Cost Optimisation

| Resource | Current | Optimisation | Estimated Saving |
|----------|---------|-------------|-----------------|
| EBS gp3 for PVCs | Provisioned at 100 GB each | Use EBS lifecycle manager to snapshot + reduce underutilised PVCs | ~20% |
| S3 (MinIO backups, documents) | Standard storage | Apply S3 Intelligent-Tiering for objects > 90 days old | ~40% on old objects |
| CloudWatch Logs | Default retention (never expire) | Set 30-day retention on non-audit logs; 7-year on audit logs (HIPAA) | ~60% on non-audit logs |
| RDS automated backups | 7-day retention | Move to 7-day automated + monthly manual snapshots to S3 Glacier | ~30% backup cost |

### 6.6 Cost Monitoring

**AWS Cost Explorer alerts**: Set a monthly budget alert at 120% of the previous
month's spend to catch unexpected cost spikes.

**Kubecost** (or OpenCost): Deploy alongside the BloodBank cluster to allocate
Kubernetes costs by namespace, service, and team:

```bash
helm install kubecost kubecost/cost-analyzer \
  --namespace kubecost \
  --create-namespace \
  --set kubecostToken="${KUBECOST_TOKEN}"
```

Kubecost dashboard shows per-service cost, idle resource cost, and savings recommendations.
Review weekly in the SRE team meeting.

**Monthly cost review checklist**:

- [ ] Review AWS Cost Explorer: any service with > 20% MoM cost increase?
- [ ] Check VPA recommendations: any service consistently using < 30% of CPU request?
- [ ] Review Spot instance interruption rate: > 5% in a week → evaluate managed node groups.
- [ ] Review S3 storage growth: is document-service storage growing linearly with usage?
- [ ] Check RDS storage auto-scaling: is automatic storage scaling triggered?
- [ ] Review RI utilisation: are reserved instances > 90% utilised? If not, consider Convertible RI exchange.

### 6.7 Cost Optimisation Roadmap

| Quarter | Initiative | Estimated Annual Saving |
|---------|-----------|------------------------|
| Q3 2026 | Purchase 1-yr RIs for RDS + ElastiCache | ~$750 |
| Q3 2026 | Enable Spot for reporting-service and CI agents | ~$400 |
| Q4 2026 | Enable S3 Intelligent-Tiering for documents | ~$200 |
| Q4 2026 | Set CloudWatch log retention policies | ~$300 |
| Q1 2027 | Right-size over-provisioned services (VPA recommendations) | ~$500 |
| Q1 2027 | Purchase 1-yr Convertible RIs for EKS worker nodes | ~$1,500 |
| Q2 2027 | Evaluate Graviton (ARM) instances for compute-heavy services | ~$600 |

**Estimated total Year 1 savings: ~$4,250 / year** at 50-branch scale.
Savings scale proportionally with growth.

---

## Appendix A — Grafana Dashboard Inventory

| Dashboard | Purpose | File |
|-----------|---------|------|
| `bloodbank-slo-dashboard` | SLO availability + latency per service | `monitoring/grafana/dashboards/slo.json` |
| `bloodbank-error-budget` | Error budget remaining per service | `monitoring/grafana/dashboards/error-budget.json` |
| `bloodbank-vpa-recommendations` | VPA CPU/memory recommendations | `monitoring/grafana/dashboards/vpa.json` |
| `bloodbank-cost` | Kubecost per-service cost breakdown | `monitoring/grafana/dashboards/cost.json` |
| `bloodbank-chaos` | Post-experiment metric replay | `monitoring/grafana/dashboards/chaos.json` |

---

## Appendix B — On-Call Escalation Path

See `docs/operations/on-call-guide.md` for the full escalation matrix.

| Severity | First Responder | Escalation (30 min no response) | Executive (2 hr no resolution) |
|----------|----------------|--------------------------------|-------------------------------|
| P1 — SLO breach | On-call SRE (PagerDuty) | SRE lead | CTO |
| P2 — Burn rate > 6× | On-call SRE (Slack) | SRE lead | — |
| P3 — Burn rate > 3× | SRE (next business hour) | — | — |

---

## Appendix C — Glossary

| Term | Definition |
|------|-----------|
| **SLI** | Service Level Indicator — a quantitative measure of service behaviour (e.g., error rate) |
| **SLO** | Service Level Objective — the target value for an SLI (e.g., error rate < 0.1%) |
| **SLA** | Service Level Agreement — a contractual commitment to customers based on SLOs |
| **Error budget** | The allowed amount of unreliability within an SLO measurement window |
| **Burn rate** | The rate at which the error budget is being consumed relative to the safe rate |
| **Chaos engineering** | Intentional injection of failures to verify system resilience |
| **VPA** | Vertical Pod Autoscaler — automatically adjusts CPU/memory requests |
| **HPA** | Horizontal Pod Autoscaler — automatically adjusts replica count |
| **RI** | Reserved Instance — discounted AWS capacity commitment |
| **Spot** | AWS Spot Instance — spare capacity at up to 90% discount, with 2-min termination notice |
