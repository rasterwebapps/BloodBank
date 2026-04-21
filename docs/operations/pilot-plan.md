# Pilot Deployment Plan

**Last Updated**: 2026-04-21
**Milestone**: M10 — Pilot Deployment (1 Branch)
**Issues**: M10-001, M10-007
**Status**: 🔴 NOT STARTED

---

## 1. Pilot Branch Selection Criteria

### Mandatory Requirements

| Criterion | Requirement | Rationale |
|---|---|---|
| Blood volume | 50–150 units/month | Manageable scale for first validation |
| Staff count | 10–30 staff members | All role groups representable |
| IT readiness | Stable broadband internet (≥50 Mbps) | Required for cloud-hosted system |
| Management buy-in | Branch Manager formally signed off | Ensures cooperation during hypercare |
| Data quality | ≥80% donor records in electronic format | Reduces data migration effort |
| Location | Regional hub with easy DevOps team access | Support SLA of 4 hours on-site if needed |

### Scoring Matrix

Rate each candidate branch 1–5 on each criterion, weight by importance:

| Criterion | Weight | Candidate A | Candidate B | Candidate C |
|---|---|---|---|---|
| Blood volume fit | 25% | — | — | — |
| Staff readiness | 20% | — | — | — |
| IT infrastructure | 20% | — | — | — |
| Data quality | 20% | — | — | — |
| Management buy-in | 15% | — | — | — |
| **Weighted total** | 100% | — | — | — |

Minimum passing score: **3.5 / 5.0**

### Disqualifying Conditions

- Branch currently under regulatory audit
- Pending IT infrastructure upgrade in the next 60 days
- Key roles (Doctor, Lab Technician) not filled
- Branch involved in recent data breach or security incident

---

## 2. Data Migration Checklist

> Full technical steps: [`docs/operations/data-migration-guide.md`](./data-migration-guide.md)

### Pre-Migration Checklist

- [ ] Signed data-sharing agreement from branch management
- [ ] Legacy system export completed and files transferred securely
- [ ] Data validation queries run against legacy export — no critical errors
- [ ] Staging environment migration dry-run completed successfully
- [ ] Rollback procedure tested in staging
- [ ] Migration window communicated to branch (minimum 48 h notice)
- [ ] Maintenance page configured at `bloodbank.example.com/maintenance`
- [ ] On-call DevOps engineer confirmed for migration window
- [ ] Database backup taken immediately before migration start
- [ ] Keycloak user accounts pre-provisioned for all pilot staff

### Migration Window

| Activity | Duration | Owner |
|---|---|---|
| Enable maintenance mode | 5 min | DevOps |
| Final legacy backup | 30 min | DevOps |
| Donor data import | 2–4 h | DevOps |
| Inventory data import | 1–2 h | DevOps |
| Data validation queries | 1 h | QA Lead |
| Smoke tests (10 scenarios) | 1 h | QA Lead |
| Go / no-go decision | 15 min | Steering committee |
| Disable maintenance mode | 5 min | DevOps |
| **Total window** | **6–9 h** | |

### Post-Migration Verification

- [ ] Donor count matches legacy system (±1%)
- [ ] Blood unit inventory count matches (exact match required)
- [ ] No duplicate donor records detected
- [ ] Audit trail entries created for all imported records
- [ ] At least 3 staff members successfully log in with new credentials

---

## 3. Staff Training Schedule

Training is delivered role-group by role-group over two days on-site. Each session is 3 hours: 1.5 h hands-on demo + 1.5 h supervised practice in the training environment.

### Day 1

| Time | Role Group | Participants | Trainer | Reference Guide |
|---|---|---|---|---|
| 09:00–12:00 | Reception & Phlebotomy | Receptionists, Phlebotomists | Application Trainer | [quick-ref-reception.md](./user-guides/quick-ref-reception.md) |
| 13:00–16:00 | Lab | Lab Technicians | Application Trainer | [quick-ref-lab.md](./user-guides/quick-ref-lab.md) |

### Day 2

| Time | Role Group | Participants | Trainer | Reference Guide |
|---|---|---|---|---|
| 09:00–12:00 | Clinical | Doctors, Nurses | Application Trainer | [quick-ref-clinical.md](./user-guides/quick-ref-clinical.md) |
| 13:00–15:00 | Inventory | Inventory Managers | Application Trainer | [quick-ref-inventory.md](./user-guides/quick-ref-inventory.md) |
| 15:00–17:00 | Administration | Branch Admin, Branch Manager | Application Trainer | [quick-ref-admin.md](./user-guides/quick-ref-admin.md) |

### Post-Training Activities (Day 3+)

| Activity | Timing | Owner |
|---|---|---|
| Competency assessment (10 scenarios) | Within 24 h after training | QA Lead |
| Feedback survey sent to all trainees | After assessment | Project Manager |
| Remedial session for ≥2 failed scenarios | Within 48 h | Application Trainer |
| Hospital Users (remote) | Webinar after go-live + 1 week | Application Trainer |
| Donor portal (self-service) | Help center article + onboarding email | Project Manager |

### Training Environment

- URL: `https://training.bloodbank.example.com`
- Pre-loaded with 500 synthetic donor records and 200 synthetic blood units
- Resets nightly at 02:00 UTC

---

## 4. Go / No-Go Decision Criteria

The go/no-go meeting is held after the migration window smoke tests complete, attended by: Branch Manager, Project Manager, DevOps Lead, QA Lead.

### Go Criteria (ALL must be met)

| # | Criterion | Target | Measurement |
|---|---|---|---|
| G1 | Data migration success | 100% records imported, 0 validation errors | Data validation queries |
| G2 | Service health | All 14 microservices healthy in K8s | `/actuator/health` endpoints |
| G3 | Authentication | All pilot staff log in successfully | Login smoke test |
| G4 | Critical workflows | Donor registration → collection → test → issue workflow completes end-to-end | QA scenario checklist |
| G5 | Performance | API p95 response time < 2 s for key endpoints | Prometheus / Grafana |
| G6 | Training completion | ≥90% of staff passed competency assessment | Training records |
| G7 | Rollback tested | Rollback procedure verified in staging | DevOps sign-off |
| G8 | On-call roster | 24/7 hypercare roster confirmed and communicated | Hypercare plan |

### No-Go Triggers (ANY causes delay)

| # | Trigger | Hold duration |
|---|---|---|
| NG1 | Any service not healthy at go-live | Until resolved + re-tested |
| NG2 | Data validation errors > 0.1% | Until errors corrected and re-validated |
| NG3 | Critical workflow failure | Until root cause fixed and re-tested |
| NG4 | p95 API latency > 5 s | Until performance remediated |
| NG5 | < 80% staff training completion | Until additional training delivered |
| NG6 | Security vulnerability (Critical/High) | Until patched and Trivy scan clean |

---

## 5. Communication Plan

### Stakeholder Map

| Audience | Role | Communication Channel | Frequency |
|---|---|---|---|
| Branch Management | Decision makers | Email + in-person meeting | Weekly |
| Pilot Branch Staff | End users | Email + WhatsApp group | As needed |
| IT / DevOps Team | Operators | Slack `#bloodbank-ops` | Daily during hypercare |
| Regional Admin | Oversight | Email report | Weekly |
| Executive Sponsors | Funding / escalation | Email summary | Bi-weekly |

### Communication Timeline

| Date Relative to Go-Live | Activity | Owner | Audience |
|---|---|---|---|
| T − 4 weeks | Kick-off announcement | Project Manager | All stakeholders |
| T − 2 weeks | Training schedule confirmed | Project Manager | Branch staff |
| T − 1 week | Migration window and maintenance notice | DevOps Lead | Branch Management |
| T − 1 day | Final go/no-go readiness check email | Project Manager | Steering committee |
| T + 0 (Go-live) | Go-live confirmation email | Project Manager | All stakeholders |
| T + 1 day | Day-1 status summary | DevOps Lead | Branch Management + Exec sponsors |
| T + 3 days | End of 24/7 hypercare status report | DevOps Lead | Regional Admin + Branch Management |
| T + 7 days | Week-1 SLO review report | QA Lead | Exec sponsors |
| T + 14 days | Pilot sign-off meeting | Project Manager | Branch Management + Exec sponsors |

### Escalation Path

```
Staff issue → Support channel (Slack / phone)
              ↓  (if unresolved in 30 min)
           Application Support Engineer
              ↓  (if unresolved in 2 h)
           DevOps Lead / Backend Lead
              ↓  (if unresolved in 4 h)
           Project Manager + Executive Sponsor
```

### Support Contact Sheet (distribute to all pilot staff)

| Contact | Name | Phone | Email | Available |
|---|---|---|---|---|
| Primary support | _TBD_ | _TBD_ | support@bloodbank.example.com | Business hours |
| On-call engineer | _TBD_ | _TBD_ | oncall@bloodbank.example.com | 24/7 first 3 days |
| Project Manager | _TBD_ | _TBD_ | pm@bloodbank.example.com | Business hours |
| Emergency escalation | _TBD_ | _TBD_ | — | 24/7 |
