# Incident Response Playbook

**Last Updated**: 2026-04-22
**Milestone**: M12 — Worldwide Launch
**Issue**: M12-016
**Status**: 🔴 NOT STARTED (Activate before go-live)

---

## Overview

This document is the authoritative incident response playbook for BloodBank production. It defines the process from initial detection through resolution and post-incident review. It also provides communication templates for both internal and external stakeholders.

For specific technical remediation steps, refer to the runbooks in `docs/operations/runbooks/`.

---

## 1. Incident Response Lifecycle

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   DETECTION  │───▶│    TRIAGE    │───▶│ CONTAINMENT  │───▶│  RESOLUTION  │───▶│  FOLLOW-UP   │
│              │    │              │    │              │    │              │    │              │
│ Alert fires  │    │ Classify P1  │    │ Stop spread  │    │ Root cause   │    │ PIR within   │
│ User report  │    │ to P4        │    │ Mitigate     │    │ Fix applied  │    │ 24–72 h      │
│ Monitor sees │    │ Assign owner │    │ Communicate  │    │ Verify       │    │ Action items │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
```

---

## 2. Incident Response Process

### Phase 1 — Detection (T+0 to T+5 min)

| Step | Who | Action |
|---|---|---|
| 1 | PagerDuty | Fires alert to on-call engineer |
| 2 | On-call (L1) | Acknowledges page in PagerDuty within SLA |
| 3 | On-call (L1) | Joins `#bloodbank-ops` Slack channel |
| 4 | On-call (L1) | Posts incident opened message (template below) |
| 5 | On-call (L1) | Opens incident in PagerDuty / ticketing system |

### Phase 2 — Triage (T+5 to T+15 min)

| Step | Who | Action |
|---|---|---|
| 1 | On-call (L1) | Assess severity: P1 / P2 / P3 |
| 2 | On-call (L1) | Identify affected service(s) using Grafana and `kubectl` |
| 3 | On-call (L1) | Select the appropriate runbook |
| 4 | On-call (L1) | If P1: escalate to L3 immediately and begin runbook |
| 5 | On-call (L1) | If data or security risk: escalate before taking action |
| 6 | On-call (L1) | Post first status update in incident channel |

### Phase 3 — Containment and Mitigation (T+15 to T+SLA)

| Step | Who | Action |
|---|---|---|
| 1 | On-call + L3 (if escalated) | Execute runbook steps |
| 2 | On-call | Post status updates every 15 min (P1) or 30 min (P2) |
| 3 | L3 (if escalated) | Approve rollback, DB changes, or isolation actions |
| 4 | L3 | Notify L4 if P1 persists > 2 hours or has regulatory implications |
| 5 | L4 (if notified) | Approve external communications |

### Phase 4 — Resolution (T+SLA to Close)

| Step | Who | Action |
|---|---|---|
| 1 | On-call | Verify fix is effective (error rate back to normal, health checks green) |
| 2 | On-call | Monitor for 30 min after resolution |
| 3 | On-call | Resolve PagerDuty incident |
| 4 | On-call | Post resolution message in Slack |
| 5 | On-call | Update status page |
| 6 | On-call | Complete incident log entry (within 2 hours) |

### Phase 5 — Post-Incident Review

| Step | Who | When |
|---|---|---|
| Schedule PIR | Incident commander (L1 or L3) | Within 1 hour of resolution |
| Hold PIR meeting | All responders + stakeholders | Within 24 h (P1) / 72 h (P2) |
| Document findings | Incident commander | Within 48 h of PIR meeting |
| Track action items | Engineering team | Sprints following the incident |

---

## 3. Incident Log Template

Create a new incident log entry for every P1 and P2 incident. Use this template in Confluence, Notion, or a GitHub Issue.

```markdown
# Incident Log: INC-<YYYYMMDD-NNN>

**Severity**: P1 / P2 / P3
**Status**: Open / Mitigated / Resolved / PIR Done
**Incident Commander**: [Name]
**Date/Time Opened**: [YYYY-MM-DD HH:MM UTC]
**Date/Time Resolved**: [YYYY-MM-DD HH:MM UTC]
**Total Duration**: [X hours Y minutes]

## Summary
[One-paragraph description of what happened and what the impact was]

## Timeline

| Time (UTC) | Event | Who |
|---|---|---|
| HH:MM | Alert fired: [alert name] | PagerDuty |
| HH:MM | On-call acknowledged | [Name] |
| HH:MM | [Action taken] | [Name] |
| HH:MM | [Root cause identified] | [Name] |
| HH:MM | [Fix applied] | [Name] |
| HH:MM | Service restored | [Name] |
| HH:MM | Incident resolved | [Name] |

## Root Cause
[Technical description of what caused the incident]

## Contributing Factors
- [Factor 1]
- [Factor 2]

## Impact
- **Users affected**: [X users / all users / specific branches]
- **Duration**: [X hours Y minutes]
- **Data affected**: [Yes/No — describe if yes]
- **SLA breached**: [Yes/No — which SLO]
- **Clinical impact**: [Yes/No — describe if yes]

## Actions Taken
1. [Action + who + when]
2. [Action + who + when]

## Resolution
[Description of how the incident was resolved]

## Action Items (Post-Incident)
| # | Action | Owner | Due Date | Status |
|---|---|---|---|---|
| 1 | | | | |
| 2 | | | | |

## Lessons Learned
- What went well:
  - [Item]
- What could be improved:
  - [Item]
- What was missing (tools, access, docs):
  - [Item]
```

---

## 4. Communication Templates

### 4.1 Internal Slack — Incident Opened

```
🔴 [INCIDENT OPENED — P{SEVERITY}]
━━━━━━━━━━━━━━━━━━━━━━━━
Incident ID: INC-{YYYYMMDD-NNN}
Severity:    P{SEVERITY} — {Critical / High}
Affected:    {Service or workflow}
Impact:      {Who/what is affected and how many users}
Started:     {HH:MM} UTC
Commander:   @{on-call-engineer}
Runbook:     {docs/operations/runbooks/runbook-xxx.md}
━━━━━━━━━━━━━━━━━━━━━━━━
Status updates every {15/30} minutes in this thread.
```

### 4.2 Internal Slack — Status Update

```
📍 [P{SEVERITY} UPDATE — T+{X}min]
━━━━━━━━━━━━━━━━━━━━━━━━
Status:       {Investigating / Mitigating / Monitoring}
Current state: {What is currently happening}
Actions taken: {Brief list of what was tried}
Next step:    {What is being done next}
ETA:          {If known, otherwise "Unknown — update in 15 min"}
```

### 4.3 Internal Slack — Resolved

```
✅ [P{SEVERITY} RESOLVED — INC-{YYYYMMDD-NNN}]
━━━━━━━━━━━━━━━━━━━━━━━━
Duration:     {X hours Y minutes}
Root cause:   {One sentence description}
Fix applied:  {What was done}
All services: 🟢 Healthy
Monitoring:   Continuing for 30 min
PIR:          Scheduled {DATE TIME} — all responders please attend
━━━━━━━━━━━━━━━━━━━━━━━━
Incident log: {Link to INC log}
```

### 4.4 External Status Page Update (Public)

**During incident:**
```
[Investigating] We are investigating reports of {issue description}.
Our team has been alerted and is actively working to resolve this.
We will provide an update within {15/30} minutes.

Posted: {TIME} UTC
```

**Mitigated but monitoring:**
```
[Monitoring] We have identified and applied a fix for {issue description}.
We are monitoring the system to confirm full recovery.
We will confirm resolution within {X} minutes.

Posted: {TIME} UTC
```

**Resolved:**
```
[Resolved] The issue affecting {feature/service} has been resolved.
All systems are operating normally.

Duration: {X hours Y minutes}
We apologise for any inconvenience caused.
A full incident report will be published within {24/72} hours.

Posted: {TIME} UTC
```

### 4.5 External Email — User-Facing Incident Notification (P1 only)

Subject: `[BloodBank] Service Disruption — {Date} — {Region}`

```
Dear BloodBank User,

We are writing to inform you of a service disruption that affected BloodBank
between {START TIME} and {END TIME} UTC on {DATE}.

WHAT HAPPENED
{Brief non-technical description of the incident}

WHO WAS AFFECTED
{Description of affected users, branches, or regions}

WHAT WE DID
Our operations team identified and resolved the issue at {TIME} UTC.
{Brief description of the fix}

IMPACT ON YOUR DATA
{Either: "No data was lost or affected during this incident." OR describe what data was affected and next steps}

WHAT WE ARE DOING TO PREVENT RECURRENCE
{Brief description of preventive action items}

We apologise for the disruption and thank you for your patience.
If you have questions, please contact support@bloodbank.example.com.

Regards,
BloodBank Operations Team
```

### 4.6 Internal Executive Briefing (P1, for L4)

```
EXECUTIVE INCIDENT BRIEFING

Incident ID: INC-{YYYYMMDD-NNN}
Date/Time:   {DATE} {TIME} UTC
Prepared by: {Name}

SITUATION
{One paragraph: what happened, when, and what the current status is}

IMPACT
- Users affected: {count or description}
- Clinical impact: {Yes/No and details}
- SLA: {Breached / Not breached}
- Regulatory: {Any HIPAA/GDPR notification triggers?}

CURRENT STATUS
{Resolved / Investigating / Mitigating — and what action is underway}

ACTIONS REQUIRED FROM EXECUTIVE SPONSOR
{List any decisions or approvals needed — or "None at this time"}

NEXT UPDATE
{TIME} UTC — or immediately if situation changes
```

---

## 5. Post-Incident Review (PIR) Template

Hold the PIR as a blameless meeting. The goal is to understand what happened and improve, not to assign blame.

```markdown
# Post-Incident Review: INC-{YYYYMMDD-NNN}

**Date of Incident**: {DATE}
**PIR Date**: {DATE}
**Facilitator**: {Name}
**Attendees**: {Names and roles}
**Incident severity**: P{N}
**Duration**: {X hours Y minutes}

---

## 1. Incident Summary
{Brief description — same as incident log}

## 2. Timeline Reconstruction
{Detailed minute-by-minute timeline from first symptom to resolution}

| Time (UTC) | Event | Notes |
|---|---|---|
| | | |

## 3. Root Cause Analysis (5 Whys)

**Problem**: {State the problem}

| Why | Answer |
|---|---|
| Why 1 | {First "why" answer} |
| Why 2 | {Second "why" answer} |
| Why 3 | {Third "why" answer} |
| Why 4 | {Fourth "why" answer} |
| Why 5 (Root Cause) | {Root cause} |

## 4. Contributing Factors

- Technical: {e.g., missing circuit breaker, no load testing for this scenario}
- Process: {e.g., deployment review skipped, no staging test for this path}
- People: {e.g., runbook not up to date, on-call didn't have DB access}
- External: {e.g., cloud provider failure, spike in traffic}

## 5. What Went Well

- {Item}
- {Item}

## 6. What Could Be Improved

- {Item}
- {Item}

## 7. Action Items

| # | Action | Type | Owner | Due Date | Status |
|---|---|---|---|---|---|
| 1 | {Action} | Prevent/Detect/Mitigate | {Name} | {Date} | Open |
| 2 | | | | | |

**Action types**:
- **Prevent**: Stop this from happening again
- **Detect**: Improve monitoring so we catch it faster
- **Mitigate**: Reduce the impact if it does happen

## 8. SLA / SLO Impact

| SLO | Target | Actual During Incident | Breached? |
|---|---|---|---|
| API availability | ≥ 99.9% | {X}% | Yes / No |
| p95 response time | < 500 ms | {X} ms | Yes / No |
| Error rate | < 0.1% | {X}% | Yes / No |

## 9. Regulatory / Compliance Review

- HIPAA breach notification required? {Yes/No — if yes, who is coordinating?}
- GDPR notification required? {Yes/No}
- FDA 21 CFR Part 11 audit trail complete? {Yes/No}
- Clinical incident report required? {Yes/No}
```

---

## 6. Escalation Quick Reference

| Situation | Immediate Action |
|---|---|
| P1 alert fires | Acknowledge within 15 min; post in `#bloodbank-ops`; open incident log |
| P1 > 30 min unresolved | Call L3 Technical Lead |
| P1 > 2 h unresolved | Call L4 Executive Sponsor |
| Any data loss or corruption suspected | Call L3 + L4 simultaneously; freeze writes |
| Security breach suspected | Call Security Lead immediately; don't investigate alone |
| Regulatory notification may be required | Call Legal Counsel + L4 before communicating externally |

---

## Reference

| Resource | URL |
|---|---|
| All runbooks | `docs/operations/runbooks/` |
| On-call guide | `docs/operations/on-call-guide.md` |
| Launch checklist | `docs/operations/launch-checklist.md` |
| HIPAA compliance | `docs/compliance/hipaa-validation.md` |
| GDPR compliance | `docs/compliance/gdpr-validation.md` |
| Grafana | `https://monitoring.bloodbank.example.com/grafana` |
| Status page | `https://status.bloodbank.example.com` |
| PagerDuty | `https://bloodbank.pagerduty.com` |
