# Regional Rollout Schedule

**Last Updated**: 2026-04-21
**Milestone**: M11 — Regional Rollout
**Issues**: M11-001 through M11-005
**Status**: 🔴 NOT STARTED

---

## Overview

This document defines the BloodBank regional rollout strategy: how branches are grouped into batches, the week-by-week schedule, per-branch migration checklist, and the staff training template used for each batch.

Rollout follows M10 (Pilot) completion. The pilot branch's learnings are incorporated into every batch execution.

---

## 1. Batch Grouping Strategy

### 1.1 Grouping Criteria

Branches are grouped by **region first**, then scored by **risk level** within each region. Batches are kept to **2–4 branches per week** to ensure the operations team can provide adequate hypercare without becoming overwhelmed.

| Factor | Weight | Description |
|---|---|---|
| Geographic proximity | 30% | Branches in the same city/region share support staff |
| Data volume (donor records) | 25% | High-volume branches carry more migration risk |
| IT readiness score | 25% | Measured by the M10 scoring matrix (minimum 3.5/5.0) |
| Staff training completion | 20% | % staff who completed pre-go-live training |

### 1.2 Risk Tiers

| Tier | Criteria | Deployment week |
|---|---|---|
| **Low Risk** | <500 donor records, IT score ≥4.5, <20 staff | Weeks 1–2 |
| **Medium Risk** | 500–2000 donor records, IT score 3.5–4.4, 20–50 staff | Weeks 2–3 |
| **High Risk** | >2000 donor records, IT score <3.5, or >50 staff | Week 4 (with extra support) |

### 1.3 Batch Composition Rules

- **Maximum 4 branches per batch** — ensures hypercare resources are not stretched
- **No two high-risk branches in the same batch**
- **Each batch must have at least 1 low-risk branch** (reduces batch-level failure probability)
- **All branches in one batch must be in the same region** (shared on-call timezone)

### 1.4 Sample Batch Config File

The `batch-rollout.sh` script (M11-002) reads a JSON config file. Template:

```json
{
  "batch": {
    "id":          "batch-1",
    "week":        1,
    "description": "North Region — Week 1 (Low-Risk Branches)",
    "contact":     "ops-team@bloodbank.org"
  },
  "branches": [
    {
      "name":        "Central City",
      "region":      "north-america",
      "admin_email": "admin@centralcity.bloodbank.org",
      "priority":    1,
      "risk_tier":   "LOW"
    },
    {
      "name":        "East District",
      "region":      "north-america",
      "admin_email": "admin@eastdistrict.bloodbank.org",
      "priority":    2,
      "risk_tier":   "LOW"
    }
  ]
}
```

---

## 2. Rollout Schedule — 4-Week Timeline

> **Note**: Adjust actual dates to match your project go-live window. All times are local to the region. Migration windows should fall on Tuesdays–Wednesdays (midweek) to allow Monday stabilisation and leave Thursday–Friday for issue resolution before the weekend.

### Week 1 (M11-006 to M11-011): Batch 1

| Day | Activity | Owner | Duration |
|---|---|---|---|
| Monday (T−7) | Batch 1 pre-migration dry run on staging | DevOps | 4 h |
| Monday (T−7) | Batch 1 branch Keycloak config + admin user creation | DevOps | 2 h |
| Tuesday (T−6) | Staff training — Day 1 (Batch 1 branches) | Application Trainer | 8 h |
| Wednesday (T−5) | Staff training — Day 2 (Batch 1 branches) | Application Trainer | 8 h |
| Wednesday (T−5) | Competency assessments (post-training) | QA Lead | 4 h |
| Thursday (T−4) | Migration window — Batch 1 data import | DevOps | 6–9 h |
| Thursday (T−4) | Post-migration data validation | QA Lead | 2 h |
| Thursday (T−4) | Go/no-go decision meeting | Steering committee | 30 min |
| Friday (T−3) | Batch 1 go-live | DevOps | 1 h |
| Friday → Sunday | Batch 1 hypercare 24/7 (3 days) | DevOps + Support | 72 h |
| Sunday (T+0) | Batch 1 sign-off meeting | Project Manager | 1 h |

**Batch 1 size**: 2–3 low-risk branches (< 500 donor records each)

**Key contacts**:
- DevOps Lead: _TBD_
- Application Trainer: _TBD_
- On-call engineer: _TBD_

---

### Week 2 (M11-012 to M11-017): Batch 2

| Day | Activity | Owner | Duration |
|---|---|---|---|
| Monday | Review Batch 1 lessons learned | Project Manager | 1 h |
| Monday | Batch 2 pre-migration dry run on staging | DevOps | 4 h |
| Monday | Batch 2 Keycloak configuration | DevOps | 2 h |
| Tuesday | Staff training — Day 1 (Batch 2) | Application Trainer | 8 h |
| Wednesday | Staff training — Day 2 (Batch 2) | Application Trainer | 8 h |
| Wednesday | Competency assessments | QA Lead | 4 h |
| Thursday | Migration window — Batch 2 | DevOps | 6–9 h |
| Thursday | Post-migration validation | QA Lead | 2 h |
| Thursday | Go/no-go decision | Steering committee | 30 min |
| Friday | Batch 2 go-live | DevOps | 1 h |
| Friday → Sunday | Batch 2 hypercare (3 days) | DevOps + Support | 72 h |
| Sunday | Batch 2 sign-off | Project Manager | 1 h |

**Batch 2 size**: 2–4 mixed-risk branches (low + medium)

---

### Week 3 (M11-018 to M11-021): Batch 3

| Day | Activity | Owner | Duration |
|---|---|---|---|
| Monday | Review Batch 2 lessons learned | Project Manager | 1 h |
| Monday | Batch 3 staging dry run | DevOps | 4 h |
| Tuesday | Staff training — Day 1 | Application Trainer | 8 h |
| Wednesday | Staff training — Day 2 | Application Trainer | 8 h |
| Wednesday | Competency assessments | QA Lead | 4 h |
| Thursday | Migration window — Batch 3 | DevOps | 6–9 h |
| Thursday | Post-migration validation + go/no-go | QA Lead + steering | 3 h |
| Friday | Batch 3 go-live | DevOps | 1 h |
| Friday → Sunday | Batch 3 hypercare | DevOps + Support | 72 h |
| Sunday | Batch 3 sign-off | Project Manager | 1 h |

**Batch 3 size**: 2–4 medium-risk branches

---

### Week 4 (M11-022 to M11-025 + M11-026 to M11-030): Batch 4 + Regional Validation

| Day | Activity | Owner | Duration |
|---|---|---|---|
| Monday | Review Batch 3 lessons learned | Project Manager | 1 h |
| Monday | Batch 4 staging dry run | DevOps | 6 h (high-risk branches need extra) |
| Tuesday | Staff training — Day 1 | Application Trainer | 8 h |
| Wednesday | Staff training — Day 2 | Application Trainer | 8 h |
| Wednesday | Competency assessments | QA Lead | 4 h |
| Thursday | Migration window — Batch 4 | DevOps | 8–12 h (large data volumes) |
| Thursday | Post-migration validation + go/no-go | QA Lead + steering | 3 h |
| Friday | Batch 4 go-live | DevOps | 1 h |
| Friday → Sunday | Batch 4 hypercare | DevOps + Support | 72 h |
| Sunday | Cross-branch validation (M11-026 to M11-029) | QA Lead | 4 h |
| Sunday | Regional management sign-off (M11-030) | Project Manager + Regional Admin | 2 h |

**Batch 4 size**: Remaining branches (including any high-risk deferred from earlier batches)

---

## 3. Per-Branch Migration Checklist

Use this checklist for **each branch** going through migration. All items must be checked before proceeding to go-live.

### T−2 weeks: Planning Gate

- [ ] Branch risk tier assessed (Low / Medium / High)
- [ ] Branch scored ≥3.5/5.0 on M10 readiness matrix
- [ ] Signed data-sharing agreement received from Branch Manager
- [ ] Batch assignment confirmed (which week, which batch JSON config)
- [ ] Legacy system export date and method confirmed with branch IT
- [ ] Migration window agreed (Tuesday or Wednesday recommended)
- [ ] Maintenance window notification sent to branch staff (48 h minimum)
- [ ] On-call engineer confirmed for migration window

### T−1 week: Staging Dry Run

- [ ] Legacy donor CSV exported and transferred to migration server (encrypted in transit)
- [ ] Legacy inventory CSV exported and transferred
- [ ] `branch-onboard.sh --dry-run` executed on staging without errors
- [ ] Full staging migration dry run completed (data import + validation queries)
- [ ] Staging dry run: donor count matches legacy ±1%
- [ ] Staging dry run: blood unit count matches legacy exactly
- [ ] Staging dry run: zero duplicate records
- [ ] Staging dry run: zero referential integrity violations
- [ ] Rollback procedure tested on staging (restore from pre-migration backup)
- [ ] Pre-migration database backup taken on staging

### T−2 days: Keycloak & Access

- [ ] Keycloak group `/regions/{region}/{branch}` created (via `branch-onboard.sh` Step 1)
- [ ] BRANCH_ADMIN user created with temporary password
- [ ] Temporary password delivered to Branch Admin via secure channel (not email plaintext)
- [ ] All branch staff Keycloak accounts created (see staff roster from Branch Manager)
- [ ] Role assignments verified for each staff account
- [ ] `branch_id` JWT attribute configured for all branch-scoped accounts
- [ ] At least 3 staff have successfully logged in to the training environment

### T−0: Migration Window

- [ ] Pre-migration database backup taken (production)
- [ ] Backup verification: `pg_restore --list` confirms backup is valid
- [ ] Backup archived to cold storage (S3 Glacier or equivalent)
- [ ] Maintenance mode enabled: `kubectl set env deployment/api-gateway MAINTENANCE_MODE=true`
- [ ] All services except api-gateway scaled to 0 (prevents writes during migration)
- [ ] Legacy donor data import executed (not dry-run)
- [ ] Legacy inventory data import executed (not dry-run)
- [ ] Post-import validation queries: all pass (see `data-migration-guide.md` Section 4)
- [ ] Audit trail entries created for all imported records
- [ ] Blood group distribution review (O+ highest, AB− lowest — sanity check)
- [ ] Services scaled back to desired replicas
- [ ] Maintenance mode disabled
- [ ] All services healthy: `kubectl get pods -n bloodbank-prod` (all Running)

### T+0: Go-Live

- [ ] `verify-branch.sh` executed and all checks PASS (or WARNs reviewed and accepted)
- [ ] `scaling-check.sh` executed: no FAIL results
- [ ] Branch Admin successfully logs in with BRANCH_ADMIN role
- [ ] Branch Admin can see branch dashboard (no cross-branch data visible)
- [ ] End-to-end smoke test: donor registration → collection → test order → result → blood issue
- [ ] Notification delivery confirmed (test notification sent to branch admin email)
- [ ] Go/no-go meeting held — GO decision recorded with signatures
- [ ] Support escalation contacts distributed to all branch staff
- [ ] Hypercare schedule communicated (3-day intensive 24/7)

### T+3 days: Hypercare Sign-off

- [ ] All P1/P2 issues resolved
- [ ] Daily hypercare call held each day (9:00 AM local time)
- [ ] Branch Manager satisfied with system behaviour
- [ ] Scaling check re-run after 72 h of live traffic
- [ ] No data integrity issues detected
- [ ] No cross-branch data leaks detected (audit log review)
- [ ] Batch sign-off document signed by Branch Manager and Project Manager

---

## 4. Staff Training Schedule Template

This template is used for **each batch**. Adjust role counts and room assignments per branch.

### Pre-Training Requirements

| Requirement | Owner | Deadline |
|---|---|---|
| Training environment provisioned (500 synthetic donors) | DevOps | T−3 days |
| Training environment URL communicated to trainers | Project Manager | T−3 days |
| Training materials printed/distributed | Application Trainer | T−2 days |
| Role-specific quick reference guides distributed | Application Trainer | T−2 days |
| All staff Keycloak accounts activated in training environment | DevOps | T−2 days |
| Projector / screen confirmed in training room | Branch Manager | T−1 day |

---

### Day 1 — Operational Staff (7 h of training)

| Time | Session | Role Group | Facilitator | Reference Guide |
|---|---|---|---|---|
| 08:30–09:00 | Registration + system overview | All staff | Project Manager | — |
| 09:00–12:00 | Reception & Intake | Receptionists | Application Trainer | [`quick-ref-reception.md`](./user-guides/quick-ref-reception.md) |
| 12:00–13:00 | Lunch break | — | — | — |
| 13:00–16:00 | Blood Collection | Phlebotomists | Application Trainer | [`quick-ref-donor.md`](./user-guides/quick-ref-donor.md) |
| 16:00–17:00 | Q&A + open floor practice | Reception + Phlebotomy | Application Trainer | — |

**Day 1 Hands-on Scenarios:**
1. Register a new walk-in donor (check eligibility, consent, vitals)
2. Book and reschedule a donation appointment
3. Check in a donor on collection day
4. Record a successful blood collection with volume and bag numbers
5. Record a mild adverse reaction (dizziness) during collection

---

### Day 2 — Clinical & Lab Staff (8 h of training)

| Time | Session | Role Group | Facilitator | Reference Guide |
|---|---|---|---|---|
| 09:00–12:00 | Laboratory Operations | Lab Technicians | Application Trainer | [`quick-ref-lab.md`](./user-guides/quick-ref-lab.md) |
| 12:00–13:00 | Lunch break | — | — | — |
| 13:00–15:00 | Clinical & Transfusion | Doctors, Nurses | Application Trainer | [`quick-ref-clinical.md`](./user-guides/quick-ref-clinical.md) |
| 15:00–16:00 | Inventory Management | Inventory Managers | Application Trainer | [`quick-ref-inventory.md`](./user-guides/quick-ref-inventory.md) |
| 16:00–17:00 | Administration & Reporting | Branch Admin, Branch Manager | Application Trainer | [`quick-ref-admin.md`](./user-guides/quick-ref-admin.md) |

**Day 2 Hands-on Scenarios:**
1. Process a TTI test order and record results
2. Process blood components (separate WB into RBC + plasma + platelets)
3. Perform a cross-match and issue blood to a patient
4. Record a transfusion reaction and submit hemovigilance report
5. Transfer blood units to another branch and track delivery
6. Generate the daily inventory reconciliation report

---

### Day 3 — Competency Assessment & Go-Live Prep

| Time | Activity | Owner |
|---|---|---|
| 09:00–11:00 | Competency assessment — 10 practical scenarios | QA Lead |
| 11:00–12:00 | Assessment review + remedial coaching for borderline cases | Application Trainer |
| 12:00–13:00 | Lunch break | — |
| 13:00–14:00 | Post-training survey sent and completed | Project Manager |
| 14:00–15:00 | Billing & Camp Coordinator training (specialist roles) | Application Trainer |
| 15:00–17:00 | Final system checks + admin user passwords issued | DevOps + Branch Admin |

**Competency assessment pass threshold**: ≥80% scenarios completed correctly
**Remedial requirement**: Staff scoring <60% must attend a 2-hour remedial session before go-live

---

### Post-Training Specialist Onboarding

| Role | Delivery method | Timing | Owner |
|---|---|---|---|
| HOSPITAL_USER | 1-hour webinar (remote) | 1 week after branch go-live | Application Trainer |
| CAMP_COORDINATOR | On-site workshop (2 h) | Before first camp | Application Trainer |
| BILLING_CLERK | On-site (Day 3 afternoon) | T−0 | Application Trainer |
| DONOR (self-service portal) | Help centre article + onboarding email | On go-live day | Project Manager |

---

### Training Environment

| Property | Value |
|---|---|
| URL | `https://training.bloodbank.<YOUR_DOMAIN>` (replace with actual training environment URL) |
| Pre-loaded data | 500 synthetic donors, 200 synthetic blood units |
| Nightly reset | 02:00 UTC (data reset to baseline each night) |
| Access period | T−7 days through T+7 days post go-live |

---

### Training Sign-off Checklist

- [ ] All staff attended their role-group session
- [ ] Competency assessment completed for all clinical staff
- [ ] ≥80% of assessed staff passed (≥80% scenarios correct)
- [ ] Remedial sessions scheduled for any staff below 60%
- [ ] Training completion logged in HR system
- [ ] Post-training survey completion rate ≥90%
- [ ] Trainer feedback submitted to Project Manager

---

## 5. Automated Rollout Scripts Reference

| Script | Issue | Purpose |
|---|---|---|
| `k8s/scripts/branch-onboard.sh` | M11-001 | End-to-end branch provisioning (Keycloak + migration + isolation check) |
| `k8s/scripts/batch-rollout.sh` | M11-002 | Orchestrate a full batch: onboard → verify → scaling check → report |
| `k8s/scripts/verify-branch.sh` | M11-004 | Post-onboarding verification (auth, isolation, service health, workflows) |
| `k8s/scripts/scaling-check.sh` | M11-005 | Infrastructure health: HPA, DB pool, Redis hit rate, RabbitMQ queues |

### Quick-Start: Running a Batch

```bash
# 1. Set required environment variables
export KEYCLOAK_ADMIN_PASS="<keycloak-admin-password>"
export BLOODBANK_SERVICE_SECRET="<service-client-secret>"
export BLOODBANK_NAMESPACE="bloodbank-prod"

# 2. Create your batch config file (see Section 1.4 for format)
cat > batch-1.json <<'EOF'
{
  "batch": {
    "id":          "batch-1",
    "week":        1,
    "description": "North Region — Week 1",
    "contact":     "ops@bloodbank.org"
  },
  "branches": [
    {
      "name":        "Central City",
      "region":      "north-america",
      "admin_email": "admin@centralcity.bloodbank.org",
      "priority":    1
    }
  ]
}
EOF

# 3. Dry-run first (always)
./k8s/scripts/batch-rollout.sh --config batch-1.json --dry-run

# 4. Execute (after dry-run review)
./k8s/scripts/batch-rollout.sh --config batch-1.json

# 5. Review reports in /tmp/bloodbank-logs/
ls -lh /tmp/bloodbank-logs/
```

---

## 6. Rollout Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Legacy data quality issues | Medium | High | Mandatory staging dry-run; 1% error tolerance |
| Branch internet outage during go-live | Low | High | Offline fallback procedure; reschedule window |
| Staff training non-completion | Medium | Medium | Minimum 80% threshold enforced; remedial sessions |
| HPA scaling insufficient at peak | Low | Medium | Pre-load test with 2× expected traffic before each batch |
| RabbitMQ queue backlog after go-live | Low | Medium | `scaling-check.sh` run after each batch |
| Cross-batch interference (shared DB) | Very Low | Critical | 4-layer isolation verified by `verify-branch.sh` for every branch |
| Keycloak capacity under increased user load | Low | High | Load test Keycloak before Batch 4 (largest user count) |

---

## 7. Regional Sign-off Criteria (M11-030)

All of the following must be met before Regional Management sign-off:

| # | Criterion | Measurement |
|---|---|---|
| RS1 | All regional branches live | All batch sign-offs complete |
| RS2 | Cross-branch transfers operational | M11-026 verified |
| RS3 | Regional dashboard aggregates all branches | M11-027 verified |
| RS4 | Emergency broadcasts reach all branches | M11-028 verified |
| RS5 | REGIONAL_ADMIN can see all branches in region | M11-029 verified |
| RS6 | No open P1 issues | Incident tracker |
| RS7 | P95 API response time <200ms across region | Prometheus/Grafana |
| RS8 | Redis cache hit rate >80% | `scaling-check.sh` output |
| RS9 | No DLQ messages outstanding | RabbitMQ management console |

---

## References

- [`docs/operations/data-migration-guide.md`](./data-migration-guide.md) — Full data import/rollback procedures
- [`docs/operations/pilot-plan.md`](./pilot-plan.md) — Pilot (M10) experience and go/no-go criteria
- [`docs/security/branch-isolation.md`](../security/branch-isolation.md) — 4-layer isolation architecture
- [`docs/milestones/M11-regional-rollout.md`](../milestones/M11-regional-rollout.md) — Issue tracking
- [`docs/operations/hypercare-plan.md`](./hypercare-plan.md) — Post-go-live support runbook
