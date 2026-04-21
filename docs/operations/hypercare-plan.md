# Hypercare Plan

**Last Updated**: 2026-04-21
**Milestone**: M10 — Pilot Deployment (1 Branch)
**Issues**: M10-016 through M10-022
**Status**: 🔴 NOT STARTED

---

## Overview

Hypercare is the intensive support period immediately following go-live. For the BloodBank pilot, hypercare runs for **14 days** after go-live, with three distinct phases:

| Phase | Duration | Support Level |
|---|---|---|
| Phase 1: Intensive | Days 1–3 | 24/7 monitoring and on-call support |
| Phase 2: Active | Days 4–7 | Business-hours support + daily review |
| Phase 3: Stabilization | Days 8–14 | Scheduled reviews + on-call for critical only |

---

## 1. 24/7 Monitoring Plan (Days 1–3)

### Monitoring Stack

| Layer | Tool | Dashboard URL |
|---|---|---|
| Infrastructure | Prometheus + Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| Application logs | Loki + Grafana | `https://monitoring.bloodbank.example.com/logs` |
| Distributed tracing | Jaeger / OpenTelemetry | `https://monitoring.bloodbank.example.com/tracing` |
| Uptime | Uptime Robot (external) | `https://uptimerobot.com/dashboard` |
| Kubernetes | K8s Dashboard | `https://k8s.bloodbank.example.com` |

### On-Call Rota (Days 1–3)

The on-call engineer monitors Grafana dashboards every 30 minutes and is the first responder for all alerts.

| Shift | Hours (UTC) | Engineer | Backup |
|---|---|---|---|
| Day shift | 06:00–18:00 | _TBD_ | _TBD_ |
| Night shift | 18:00–06:00 | _TBD_ | _TBD_ |

### Key Metrics to Monitor

| Metric | Alert Threshold | Critical Threshold |
|---|---|---|
| API p95 response time | > 2 s | > 5 s |
| API error rate (5xx) | > 0.5% | > 2% |
| Database connection pool usage | > 70% | > 90% |
| Redis cache hit rate | < 80% | < 60% |
| RabbitMQ queue depth | > 100 messages | > 1000 messages |
| JVM heap usage (any service) | > 80% | > 95% |
| Kubernetes pod restarts | > 1 in 10 min | > 3 in 10 min |
| Disk usage (prod nodes) | > 75% | > 90% |

### Automated Alerts

All alerts are routed to:
- Slack: `#bloodbank-ops-alerts`
- PagerDuty: on-call engineer (for Critical threshold breaches)
- Email: `ops-team@bloodbank.example.com`

### Alert Severity and Response Time

| Severity | Definition | Response SLA | Resolution SLA |
|---|---|---|---|
| P1 — Critical | System down or data corruption | 15 minutes | 4 hours |
| P2 — High | Core workflow broken (collections, issuing) | 30 minutes | 8 hours |
| P3 — Medium | Non-critical feature broken | 2 hours | 24 hours |
| P4 — Low | Cosmetic or minor issue | Next business day | 72 hours |

---

## 2. Support Channel Setup

### Channels

| Channel | Purpose | Access |
|---|---|---|
| Slack `#bloodbank-ops` | Internal DevOps team communication | DevOps team only |
| Slack `#bloodbank-ops-alerts` | Automated system alerts | DevOps team + on-call |
| Slack `#bloodbank-pilot-support` | Pilot branch staff questions | All pilot staff + support team |
| Email: `support@bloodbank.example.com` | External support requests | Pilot branch staff |
| WhatsApp group: "BloodBank Pilot" | Quick communication during hypercare | Pilot branch staff + Project Manager |
| Phone hotline | Emergency voice support | On-call engineer's mobile number |

### Response Time Commitments (Support Channels)

| Channel | Business Hours | Outside Business Hours (Phase 1 only) |
|---|---|---|
| Slack `#bloodbank-pilot-support` | 30 minutes | 1 hour |
| Email | 2 hours | Next morning |
| WhatsApp | 1 hour | 30 minutes (Phase 1) |
| Phone hotline | Immediate | Immediate (Phase 1) |

### First Contact for Pilot Branch Staff

Post this in the branch common area:

```
BloodBank Support
─────────────────────────────────
📞 On-call phone:  [TBD]
💬 WhatsApp group: BloodBank Pilot
📧 Email:         support@bloodbank.example.com
🌐 Help centre:   https://bloodbank.example.com/help
─────────────────────────────────
Critical issue? Call the on-call engineer directly.
```

---

## 3. Escalation Matrix

```
Level 1 — Application Support Engineer
  Handles: Login issues, UI questions, data entry errors
  Response: 30 min
  Contacts: support@bloodbank.example.com / Slack / WhatsApp
       │
       │ If unresolved in 2 hours ↓
       │
Level 2 — Backend / DevOps Lead
  Handles: Service errors, performance degradation, DB issues
  Response: 1 hour
  Contacts: Direct mobile (on-call rota)
       │
       │ If unresolved in 4 hours OR P1/P2 immediately ↓
       │
Level 3 — Project Manager + Technical Lead
  Handles: Major incidents, rollback decisions, stakeholder comms
  Response: 30 min (P1), 2 hours (P2)
  Contacts: PM mobile + Tech Lead mobile
       │
       │ If unresolved in 8 hours OR data integrity risk ↓
       │
Level 4 — Executive Sponsor
  Handles: Business decisions, rollback approval, external communications
  Response: 1 hour
```

### Escalation Contact Register

| Level | Name | Role | Phone | Email |
|---|---|---|---|---|
| L1 | _TBD_ | App Support Engineer | _TBD_ | _TBD_ |
| L2 | _TBD_ | DevOps Lead | _TBD_ | _TBD_ |
| L2 | _TBD_ | Backend Lead | _TBD_ | _TBD_ |
| L3 | _TBD_ | Project Manager | _TBD_ | _TBD_ |
| L3 | _TBD_ | Technical Lead | _TBD_ | _TBD_ |
| L4 | _TBD_ | Executive Sponsor | _TBD_ | _TBD_ |

### Rollback Decision Authority

| Scenario | Decision Authority |
|---|---|
| Single service degraded | L2 DevOps Lead |
| Multiple services degraded | L3 Project Manager + Technical Lead |
| Data integrity issue detected | L3 + L4 (joint decision) |
| Full system outage > 4 h | L4 Executive Sponsor |

---

## 4. Daily Review Checklist

Run this checklist every morning at 09:00 during hypercare. The DevOps Lead owns this and sends the summary to the project Slack channel and to Branch Management.

### Infrastructure Health

- [ ] All 14 microservices showing `UP` on `/actuator/health`
- [ ] No pod crash-loops in the last 24 h (`kubectl get pods -n bloodbank-prod`)
- [ ] Database connections within normal range (< 70% pool utilization)
- [ ] Redis cache hit rate ≥ 80%
- [ ] RabbitMQ queues draining normally (no depth accumulation)
- [ ] No disk usage warnings on prod nodes

### Application Health

- [ ] API error rate (5xx) < 0.5% in last 24 h
- [ ] No P1 or P2 alerts fired in last 24 h (if any: document and track to resolution)
- [ ] All Grafana alert rules enabled and firing to correct channels

### Data Integrity

- [ ] No audit trail gaps detected (query: audit_logs entries created for all completed collections)
- [ ] Blood unit count consistent with yesterday's closing count ± expected activity
- [ ] No orphaned crossmatch or transfusion records

### User Activity

- [ ] All pilot staff accounts able to log in (test 1 account per role group)
- [ ] No support tickets unacknowledged for > 2 h
- [ ] Feedback from branch staff reviewed and logged

### Daily Review SQL Queries

```sql
-- 24-hour collection activity
SELECT COUNT(*) AS collections_today
FROM collections
WHERE branch_id = '<PILOT_BRANCH_UUID>'
  AND created_at >= NOW() - INTERVAL '24 hours';

-- Error events in audit log
SELECT action, COUNT(*) AS cnt
FROM audit_logs
WHERE branch_id = '<PILOT_BRANCH_UUID>'
  AND created_at >= NOW() - INTERVAL '24 hours'
  AND action LIKE '%ERROR%'
GROUP BY action;

-- RabbitMQ dead letter queue depth (run via RabbitMQ Management API)
-- GET https://rabbitmq.bloodbank.example.com/api/queues/%2F/bloodbank.dlq
```

---

## 5. Weekly SLO Review Template

Conduct this review every Friday during hypercare. Share the report with Executive Sponsors and Branch Management.

### Report Header

```
BloodBank Pilot — Week N SLO Review
Branch: [PILOT BRANCH NAME]
Period: [START DATE] to [END DATE]
Prepared by: [NAME]
Date: [DATE]
```

### SLO Scorecard

| SLO | Target | Actual (Week N) | Status |
|---|---|---|---|
| API availability | ≥ 99.5% | __%_ | 🟢 / 🟡 / 🔴 |
| API p95 response time | < 2 s | __ms_ | 🟢 / 🟡 / 🔴 |
| API error rate (5xx) | < 0.5% | __%_ | 🟢 / 🟡 / 🔴 |
| Collection workflow end-to-end time | < 30 min | __min_ | 🟢 / 🟡 / 🔴 |
| Crossmatch TAT (routine) | < 2 h | __h_ | 🟢 / 🟡 / 🔴 |
| Notification delivery rate | ≥ 99% | __%_ | 🟢 / 🟡 / 🔴 |
| Report generation time | < 30 s | __s_ | 🟢 / 🟡 / 🔴 |

### Incident Summary

| Incident | Severity | Date | Duration | Root Cause | Status |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

### Support Ticket Summary

| Category | Count | Avg Resolution Time | Notes |
|---|---|---|---|
| Login / access | | | |
| UI bug | | | |
| Data entry issue | | | |
| Performance | | | |
| Feature request | | | |
| **Total** | | | |

### User Feedback Summary

- Overall satisfaction score: __ / 5
- Top positive themes:
  1. _
  2. _
- Top improvement requests:
  1. _
  2. _

### Actions for Next Week

| # | Action | Owner | Due |
|---|---|---|---|
| 1 | | | |
| 2 | | | |

### Pilot Status

**Current pilot status**: 🟢 ON TRACK / 🟡 AT RISK / 🔴 NEEDS INTERVENTION

**Recommendation**: Continue hypercare / Extend hypercare / Escalate to Executive Sponsor

---

## 6. Hypercare Exit Criteria

Hypercare (Phase 1 and 2) may conclude early if all criteria are met for 3 consecutive days:

| Criterion | Target |
|---|---|
| API availability | ≥ 99.9% |
| Zero P1 incidents | 3 consecutive days with no P1 |
| Zero P2 incidents | 3 consecutive days with no P2 |
| Support ticket volume | < 5 tickets/day |
| User satisfaction score | ≥ 4.0 / 5.0 |
| SLO scorecard | All SLOs green |

Hypercare exit is approved jointly by Project Manager and Branch Manager.
