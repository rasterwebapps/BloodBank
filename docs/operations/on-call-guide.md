# On-Call Guide

**Last Updated**: 2026-04-22
**Milestone**: M12 — Worldwide Launch
**Issue**: M12-014
**Status**: 🔴 NOT STARTED (Activate before go-live)

---

## Overview

This guide defines the on-call procedures for BloodBank production operations worldwide. The on-call engineer is the first responder for all production incidents and is responsible for initial triage, escalation, and incident coordination.

---

## 1. On-Call Rotation Schedule

### 1.1 Rotation Template

The on-call schedule is managed in PagerDuty. The template below shows the weekly rotation structure:

| Week | Primary On-Call | Secondary (Backup) | Escalation Lead (L3) |
|---|---|---|---|
| Week 1 | _Engineer A_ | _Engineer B_ | _Tech Lead A_ |
| Week 2 | _Engineer B_ | _Engineer C_ | _Tech Lead B_ |
| Week 3 | _Engineer C_ | _Engineer D_ | _Tech Lead A_ |
| Week 4 | _Engineer D_ | _Engineer A_ | _Tech Lead B_ |

**Rotation rhythm**: Weekly, Monday 09:00 UTC handoff
**PagerDuty schedule URL**: `https://bloodbank.pagerduty.com/schedules`

### 1.2 On-Call Responsibilities

The primary on-call engineer is responsible for:
- Acknowledging PagerDuty pages within 15 minutes (P1/P2) or 30 minutes (P3)
- Initial triage using the appropriate runbook
- Escalating to L3 when resolution is not possible within the SLA
- Updating the incident Slack channel every 15 minutes during active P1 incidents
- Completing the incident log entry within 2 hours of resolution

The secondary on-call is backup if the primary is unresponsive within 10 minutes.

### 1.3 On-Call Shift Handoff Checklist

At the start of each on-call week, the incoming engineer must:

```
On-Call Handoff — Week of [DATE]
Incoming engineer: [NAME]
Outgoing engineer: [NAME]

Handoff items:
[ ] Review open incidents and tickets from previous week
[ ] Check Grafana for any unresolved or acknowledged-but-not-resolved alerts
[ ] Confirm PagerDuty schedule shows correct assignments
[ ] Verify production dashboards are loading correctly
[ ] Check Kubernetes cluster health (all nodes Ready)
[ ] Review any pending deployments or maintenance windows
[ ] Read last 7 days of #bloodbank-ops-alerts Slack channel
[ ] Confirm all runbooks are up to date (check last-updated dates)
[ ] Verify emergency contact list is current
[ ] Confirm access to production systems (K8s, Grafana, DB read access)

Sign-off:
Incoming: [NAME] [DATE TIME]
Outgoing: [NAME] [DATE TIME]
```

---

## 2. Incident Severity Definitions

### P1 — Critical (System Down or Data Risk)

**Definition**: A P1 incident affects all users or puts patient data at risk.

| Criteria | Examples |
|---|---|
| Full system outage | api-gateway down; no users can log in |
| Core clinical workflow broken | Cannot issue blood units; cannot perform crossmatch |
| Data corruption detected | Wrong blood group, duplicate units, audit trail gaps |
| Active security breach | Unauthorized access to PHI |
| Database primary down | Write operations failing system-wide |

**Response SLA**: Page acknowledged within **15 minutes**
**Resolution SLA**: Resolved or escalated to L4 within **4 hours**
**Communication**: Status update every 15 minutes in `#bloodbank-ops`

### P2 — High (Major Feature Impaired)

**Definition**: A P2 incident significantly impairs an important workflow but the system is partially usable.

| Criteria | Examples |
|---|---|
| Non-critical service down | reporting-service, billing-service |
| Core workflow degraded (not fully broken) | Collections failing for one region only |
| Elevated error rate (5–10%) | Many requests failing but some succeeding |
| Performance severely degraded | p95 response time > 10 s |
| Replication lag > 5 minutes | Replicas serving very stale data |

**Response SLA**: Page acknowledged within **30 minutes**
**Resolution SLA**: Resolved or escalated to L3 within **8 hours**
**Communication**: Status update every 30 minutes in `#bloodbank-ops`

### P3 — Medium (Non-Critical Issue)

**Definition**: A feature or service is impaired but a workaround exists.

| Criteria | Examples |
|---|---|
| Non-critical feature unavailable | Notifications delayed, reports slow |
| Minor performance degradation | p95 slightly above SLO |
| Single user or single branch affected | Isolated access issue |

**Response SLA**: Acknowledged within **2 hours**
**Resolution SLA**: Resolved within **24 hours**
**Communication**: Update in ticket/Slack within 4 hours

### P4 — Low (Cosmetic / Minor)

**Definition**: A minor issue with no user impact.

| Criteria | Examples |
|---|---|
| Cosmetic UI issue | Wrong label, alignment problem |
| Low-priority edge case | Rarely used feature slightly broken |

**Response SLA**: Next business day
**Resolution SLA**: Within **72 hours**

---

## 3. Response Time SLAs Summary

| Severity | Alert Acknowledgement | Resolution Target | Escalation Trigger |
|---|---|---|---|
| P1 — Critical | 15 minutes | 4 hours | After 30 min unresolved → L3; after 2 h → L4 |
| P2 — High | 30 minutes | 8 hours | After 2 h unresolved → L3 |
| P3 — Medium | 2 hours | 24 hours | After 8 h unresolved → L3 |
| P4 — Low | Next business day | 72 hours | Via ticketing system |

**Clock starts**: When the PagerDuty page is sent (not when acknowledged).

---

## 4. Escalation Procedures

### 4.1 Escalation Matrix

```
L1 — On-Call Engineer (Primary)
  Handles: Initial triage, runbook execution, minor incidents
  Response: 15 min (P1), 30 min (P2), 2 h (P3)
  Contact: PagerDuty primary on-call
       │
       │ Not resolved in SLA / P1 immediately → ↓
       │
L2 — On-Call Engineer (Secondary / Backup)
  Handles: Assists L1, takes over if L1 unavailable
  Response: 10 min after L1 unresponsive
  Contact: PagerDuty secondary on-call
       │
       │ Not resolved in SLA / complex issue → ↓
       │
L3 — Technical Lead + DevOps Lead
  Handles: Architectural decisions, DB issues, complex debugging, rollback decisions
  Response: 30 min (P1), 2 h (P2)
  Contact: Direct mobile (see contact register)
       │
       │ P1 > 2 h unresolved / data breach / clinical impact → ↓
       │
L4 — Project Manager + Executive Sponsor
  Handles: Business decisions, external communication, regulatory notification
  Response: 1 h (P1)
  Contact: Direct mobile (see contact register)
       │
       │ Regulatory breach, media involvement → ↓
       │
L5 — Legal Counsel + Clinical Lead (for healthcare incidents)
  Handles: Regulatory notifications, patient safety, media response
  Response: As directed by L4
```

### 4.2 Emergency Contact Register

| Level | Name | Role | Phone | Email | PagerDuty |
|---|---|---|---|---|---|
| L1 Primary | _TBD_ | On-call Engineer | _TBD_ | _TBD_ | Scheduled primary |
| L1 Backup | _TBD_ | On-call Engineer | _TBD_ | _TBD_ | Scheduled secondary |
| L3 | _TBD_ | Technical Lead | _TBD_ | _TBD_ | _TBD_ |
| L3 | _TBD_ | DevOps Lead | _TBD_ | _TBD_ | _TBD_ |
| L3 | _TBD_ | DBA | _TBD_ | _TBD_ | _TBD_ |
| L3 | _TBD_ | Security Lead | _TBD_ | _TBD_ | _TBD_ |
| L4 | _TBD_ | Project Manager | _TBD_ | _TBD_ | _TBD_ |
| L4 | _TBD_ | Executive Sponsor | _TBD_ | _TBD_ | _TBD_ |
| L5 | _TBD_ | Clinical Lead | _TBD_ | _TBD_ | _TBD_ |
| L5 | _TBD_ | Legal Counsel | _TBD_ | _TBD_ | _TBD_ |

> ⚠️ This register contains sensitive contact information. Do not commit real phone numbers to the repository. Store in a password manager (e.g., 1Password, HashiCorp Vault) and share securely.

### 4.3 Escalation Protocol

1. **Before escalating**, document in the incident Slack channel:
   - What you tried
   - Current status
   - Why escalation is needed
2. **Contact via phone first** for P1 — Slack may not be seen immediately
3. **State severity and impact clearly**: "This is a P1 — api-gateway is down, all users locked out, 10 minutes so far"
4. **Transfer incident ownership explicitly**: "I'm handing this to [NAME] — please confirm you have context"

---

## 5. On-Call Communication Templates

### 5.1 Incident Opened (Slack)

```
🔴 [P1 INCIDENT OPENED]
Incident: <Short description>
Affected: <Service(s) or workflow>
Started: <Time UTC>
On-call: @<engineer>
Impact: <Who/what is affected>
Runbook: <Link or file>
Updates: Every 15 min in this thread
```

### 5.2 Status Update (Slack — every 15 min for P1)

```
📍 [P1 UPDATE — +Xmin]
Status: Investigating / Mitigating / Monitoring
Actions taken: <What you did>
Current state: <Current situation>
Next step: <What you're doing next>
ETA: <If known>
```

### 5.3 Incident Resolved (Slack)

```
✅ [P1 RESOLVED]
Duration: X hours Y minutes
Root cause: <Brief description>
Fix applied: <What was done>
Post-incident review: Scheduled for <DATE/TIME>
```

### 5.4 Escalation Message (Phone/WhatsApp)

```
"Hi [NAME], this is [YOUR NAME] on-call. We have a [P1/P2] incident.
[SERVICE_NAME] is [down/degraded]. Impact: [WHO IS AFFECTED].
Duration: [X] minutes. I need your help with [SPECIFIC NEED].
Can you join the incident Slack channel #inc-[DATE]?"
```

---

## 6. Production Access Requirements

Every on-call engineer must have pre-configured access to:

| System | Access Level | How to Verify |
|---|---|---|
| Grafana | Viewer + Alert management | `https://monitoring.bloodbank.example.com/grafana` |
| Kubernetes | `kubectl get/describe/logs -n bloodbank-prod` | `kubectl auth can-i get pods -n bloodbank-prod` |
| PagerDuty | Responder | `https://bloodbank.pagerduty.com` |
| Slack | Member of `#bloodbank-ops`, `#bloodbank-ops-alerts` | Check channel membership |
| Loki logs | Viewer | `https://monitoring.bloodbank.example.com/logs` |
| Jaeger tracing | Viewer | `https://monitoring.bloodbank.example.com/tracing` |
| PostgreSQL | Read-only via `kubectl exec` | `kubectl exec -it postgresql-0 -- psql -U bloodbank_readonly` |
| AWS/Cloud console | Read-only for infra viewing | Per cloud provider policy |

> ⚠️ On-call engineers do **NOT** have write access to the database by default. DBA access is escalated to L3.

---

## 7. Maintenance Windows

Planned maintenance should be scheduled:
- **Routine maintenance**: Tuesdays 02:00–06:00 UTC (lowest worldwide traffic)
- **Emergency maintenance**: Any time with prior notification in `#bloodbank-ops` and status page update
- **Deployment windows**: Monday/Wednesday/Friday 10:00 UTC (during business hours for L3 availability)

Notify affected regions at least 48 hours before planned maintenance.

---

## 8. On-Call Wellbeing

- The on-call engineer should not be on-call for more than **7 consecutive days**
- If woken up between midnight and 06:00 UTC for a P1 incident, you are entitled to **4 hours compensatory rest** (coordinate with your manager)
- If incidents are frequent (> 3 P1/P2 per week), this must be raised in the next SLO review as a systemic issue requiring investment

Post a note in `#bloodbank-ops` if you need to swap on-call duties due to illness or emergency.

---

## Reference

| Resource | URL |
|---|---|
| PagerDuty | `https://bloodbank.pagerduty.com` |
| Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| Status page | `https://status.bloodbank.example.com` |
| Incident response | `docs/operations/incident-response.md` |
| All runbooks | `docs/operations/runbooks/` |
| Launch checklist | `docs/operations/launch-checklist.md` |
