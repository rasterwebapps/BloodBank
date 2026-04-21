# BloodBank UAT — Overview & Process

**Last Updated**: 2026-04-21
**Milestone**: M9 — UAT + Compliance Validation
**Status**: 🔴 NOT STARTED

---

## 1. Purpose

User Acceptance Testing (UAT) validates that the BloodBank system meets the business requirements and operational workflows of real blood bank staff. UAT is conducted with domain experts from each of the 16 user roles before the system proceeds to pilot deployment (M10).

UAT is **not** a regression suite — it tests real-world scenarios using production-like data from the perspective of each role's daily responsibilities.

---

## 2. UAT Environment

| Item | Value |
|---|---|
| Environment URL | `https://uat.bloodbank.internal` |
| API Base URL | `https://api.uat.bloodbank.internal/api/v1` |
| Keycloak URL | `https://auth.uat.bloodbank.internal` |
| Keycloak Realm | `bloodbank` |
| Database | PostgreSQL 17 (`bloodbank_db_uat`) |
| Data Seed | Production-anonymized dataset (~6 months history) |
| Deployment | Kubernetes namespace `bloodbank-uat` |
| Reset Policy | Environment reset to baseline before each UAT session |

### Infrastructure Requirements

- All 14 microservices must be running and healthy
- RabbitMQ queues must be empty at session start
- Redis cache must be flushed at session start
- MinIO document store seeded with sample documents
- Keycloak UAT test users provisioned (see [`keycloak-test-users.md`](./keycloak-test-users.md))

---

## 3. Participants

### Stakeholder Groups

| Group | Representative Roles | Session Count |
|---|---|---|
| Blood Bank Operations | BRANCH_ADMIN, BRANCH_MANAGER, RECEPTIONIST, PHLEBOTOMIST | 2 sessions |
| Clinical | DOCTOR, NURSE, LAB_TECHNICIAN | 2 sessions |
| Inventory & Logistics | INVENTORY_MANAGER, CAMP_COORDINATOR | 1 session |
| Finance | BILLING_CLERK | 1 session |
| External Partners | HOSPITAL_USER, DONOR | 1 session |
| Administration | SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR | 1 session |

### UAT Roles

| Role | Responsibility |
|---|---|
| **UAT Lead** | Coordinates sessions, owns sign-off |
| **Tester** | Blood bank staff member executing test scripts |
| **Observer** | Developer/analyst present to capture defects |
| **Scribe** | Records results, defect IDs, and notes |
| **System Admin** | Manages UAT environment and user accounts |

---

## 4. Timeline

| Phase | Duration | Activities |
|---|---|---|
| **Preparation** (M9-001–M9-005) | Week 1 | Environment setup, test accounts, test scripts, scheduling |
| **Execution — Operations** | Week 2, Day 1–2 | BRANCH_ADMIN, BRANCH_MANAGER, RECEPTIONIST, PHLEBOTOMIST |
| **Execution — Clinical** | Week 2, Day 3 | DOCTOR, NURSE, LAB_TECHNICIAN |
| **Execution — Inventory** | Week 2, Day 4 | INVENTORY_MANAGER, CAMP_COORDINATOR |
| **Execution — Finance** | Week 2, Day 4 (PM) | BILLING_CLERK |
| **Execution — External** | Week 3, Day 1 | HOSPITAL_USER, DONOR |
| **Execution — Admin** | Week 3, Day 2 | SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR |
| **Defect Triage** | Week 3, Day 3 | Classify and prioritize defects |
| **Critical Fix Cycle** | Week 3–4 | Fix P1/P2 defects, regression test |
| **Re-test & Sign-off** | Week 4 | Re-run failed scenarios, stakeholder sign-off |

---

## 5. Test Script Structure

Each role has a dedicated test script in [`test-scripts/`](./test-scripts/). Scripts are organized by workflow and use the following format:

### Script Header

```
Role: <ROLE_NAME>
UAT Environment: https://uat.bloodbank.internal
Test Account: uat-<role>@bloodbank.test
Tester Name: ___________________________
Test Date: ___________________________
Session #: ___________________________
```

### Scenario Format

| # | Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|---|
| TC-001 | 1 | Navigate to login page | Login page renders | ☐ Pass / ☐ Fail | |

### Result Codes

| Code | Meaning |
|---|---|
| ✅ Pass | Scenario behaves exactly as expected |
| ❌ Fail | Scenario does not match expected result |
| ⚠️ Partial | Scenario partially passes — minor deviation noted |
| ⏭️ Skip | Scenario skipped (feature not available / out of scope) |
| 🔁 Blocked | Scenario blocked by a prior defect |

---

## 6. Defect Management

### Severity Classification

| Severity | Definition | Target Fix |
|---|---|---|
| **P1 — Critical** | Data loss, patient safety risk, system unavailable | Within 24 hours |
| **P2 — High** | Core workflow broken, no workaround | Within 3 business days |
| **P3 — Medium** | Functionality impaired, workaround exists | Before sign-off |
| **P4 — Low** | Cosmetic, non-impacting, UX polish | Backlog |

### Defect ID Format

```
DEF-{YYYYMMDD}-{NNN}
Example: DEF-20260421-001
```

### Defect Report Fields

- **ID**: Unique defect identifier
- **Scenario**: Test case ID (e.g., TC-REC-003)
- **Description**: What happened vs. what was expected
- **Severity**: P1 / P2 / P3 / P4
- **Reproducible**: Yes / No / Intermittent
- **Screenshot/Video**: Attached
- **Assigned To**: Developer name
- **Status**: Open / In Progress / Fixed / Verified / Closed

Track all defects in [`uat-tracking.md`](./uat-tracking.md).

---

## 7. Entry & Exit Criteria

### Entry Criteria (UAT Start)

- [ ] All 14 services pass smoke tests in UAT environment
- [ ] Performance benchmarks met (M8 sign-off)
- [ ] All UAT test accounts created in Keycloak
- [ ] UAT environment seeded with anonymized production-like data
- [ ] All test scripts reviewed and approved by UAT Lead
- [ ] UAT sessions scheduled and testers confirmed

### Exit Criteria (UAT Sign-off)

- [ ] All P1 defects resolved and re-tested ✅
- [ ] All P2 defects resolved and re-tested ✅
- [ ] ≥ 95% of test scenarios pass across all roles
- [ ] No open P3 defects that block patient safety workflows
- [ ] Compliance scenarios (HIPAA, GDPR, FDA, AABB, WHO) all pass
- [ ] Accessibility scenarios (WCAG 2.1 AA) pass
- [ ] Signed UAT sign-off document from all stakeholder group leads

---

## 8. Test Scripts by Role

| Role | File | Scenarios | Key Workflows |
|---|---|---|---|
| SUPER_ADMIN | [uat-super-admin.md](./test-scripts/uat-super-admin.md) | 15 | System settings, branch creation, global oversight |
| REGIONAL_ADMIN | [uat-regional-admin.md](./test-scripts/uat-regional-admin.md) | 12 | Regional dashboard, multi-branch oversight, reports |
| BRANCH_ADMIN | [uat-branch-admin.md](./test-scripts/uat-branch-admin.md) | 20 | Branch setup, staff, all operations, approvals |
| BRANCH_MANAGER | [uat-branch-manager.md](./test-scripts/uat-branch-manager.md) | 16 | Operational dashboard, approvals, reports |
| RECEPTIONIST | [uat-receptionist.md](./test-scripts/uat-receptionist.md) | 14 | Donor registration, walk-in, appointments |
| PHLEBOTOMIST | [uat-phlebotomist.md](./test-scripts/uat-phlebotomist.md) | 13 | Collection, vitals, adverse reactions |
| LAB_TECHNICIAN | [uat-lab-technician.md](./test-scripts/uat-lab-technician.md) | 14 | Test orders, results, QC, dual-review |
| INVENTORY_MANAGER | [uat-inventory-manager.md](./test-scripts/uat-inventory-manager.md) | 15 | Stock, storage, transfers, disposal |
| DOCTOR | [uat-doctor.md](./test-scripts/uat-doctor.md) | 16 | Blood requests, cross-match, transfusion orders |
| NURSE | [uat-nurse.md](./test-scripts/uat-nurse.md) | 12 | Transfusion administration, reaction reporting |
| BILLING_CLERK | [uat-billing-clerk.md](./test-scripts/uat-billing-clerk.md) | 12 | Invoices, payments, rates, credit notes |
| CAMP_COORDINATOR | [uat-camp-coordinator.md](./test-scripts/uat-camp-coordinator.md) | 12 | Camp planning, execution, collections |
| HOSPITAL_USER | [uat-hospital-user.md](./test-scripts/uat-hospital-user.md) | 10 | Blood request portal, tracking, feedback |
| DONOR | [uat-donor.md](./test-scripts/uat-donor.md) | 10 | Self-registration, history, appointments, donor card |
| AUDITOR | [uat-auditor.md](./test-scripts/uat-auditor.md) | 10 | Audit trail, compliance reports, read-only access |
| SYSTEM_ADMIN | [uat-system-admin.md](./test-scripts/uat-system-admin.md) | 11 | Monitoring, feature flags, scheduled jobs |

---

## 9. Supporting Documents

- [Keycloak Test Users](./keycloak-test-users.md) — 16 UAT test accounts with credentials
- [UAT Tracking](./uat-tracking.md) — Scenario tracking and defect log
- [RBAC Matrix](../security/rbac-matrix.md) — Role × Endpoint access reference
- [M9 Milestone](../milestones/M9-uat-compliance.md) — Full milestone issue list

---

## 10. Sign-Off

| Stakeholder Group | Representative | Signature | Date |
|---|---|---|---|
| Blood Bank Operations | | | |
| Clinical | | | |
| Inventory & Logistics | | | |
| Finance | | | |
| External Partners | | | |
| Administration | | | |
| **UAT Lead** | | | |
