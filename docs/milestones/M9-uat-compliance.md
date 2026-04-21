# M9: UAT + Compliance Validation

**Duration:** 2 weeks
**Dependencies:** M8 (Performance Testing)
**Exit Gate:** UAT signed off by stakeholders, regulatory compliance validated

## 📊 Development Status: 🟡 IN PROGRESS (40%)

**Issues Completed:** 16/40 automated (preparation docs + compliance docs)
**Issues READY (Manual):** 24/40 — UAT execution, accessibility testing, defect resolution
**Blocked by:** M8 ✅ — now UNBLOCKED

> **Note (2026-04-21):** UAT preparation (M9-001–M9-005) and all compliance validation docs
> (M9-022–M9-032) are complete. M9-006–M9-021 (UAT execution by role),
> M9-033–M9-040 (accessibility testing + defect resolution) are **manual processes** —
> they are READY to execute but cannot be automated.

---

## Objective

User acceptance testing with real blood bank staff and comprehensive regulatory compliance validation.

## Issues

### UAT Preparation
- [x] **M9-001**: Create UAT environment with production-like data — `docs/uat/README.md` (environment config documented)
- [x] **M9-002**: Create UAT test scripts for each of the 16 user roles — `docs/uat/test-scripts/` (16 scripts created)
- [x] **M9-003**: Create test accounts in Keycloak for UAT testers — `docs/uat/keycloak-test-users.md` (16 accounts documented)
- [x] **M9-004**: Prepare UAT tracking spreadsheet (scenarios, status, defects) — `docs/uat/uat-tracking.md`
- [x] **M9-005**: Schedule UAT sessions with stakeholders — `docs/uat/README.md` (section 4: Timeline)

### UAT Execution — By Role
> ⚙️ **MANUAL PROCESS** — Items below are READY to execute. They require real blood bank staff testers
> and a running UAT environment. They cannot be automated. Mark `[x]` after each session completes.

- [ ] **M9-006**: UAT: SUPER_ADMIN — system settings, tenant config, global overview *(READY — see `docs/uat/test-scripts/uat-super-admin.md`)*
- [ ] **M9-007**: UAT: REGIONAL_ADMIN — multi-branch overview, regional reports *(READY — see `docs/uat/test-scripts/uat-regional-admin.md`)*
- [ ] **M9-008**: UAT: BRANCH_ADMIN — branch setup, staff management, all operations *(READY — see `docs/uat/test-scripts/uat-branch-admin.md`)*
- [ ] **M9-009**: UAT: BRANCH_MANAGER — approvals, operational dashboard *(READY — see `docs/uat/test-scripts/uat-branch-manager.md`)*
- [ ] **M9-010**: UAT: RECEPTIONIST — donor registration, walk-in, appointments *(READY — see `docs/uat/test-scripts/uat-receptionist.md`)*
- [ ] **M9-011**: UAT: PHLEBOTOMIST — blood collection, vitals, adverse reactions *(READY — see `docs/uat/test-scripts/uat-phlebotomist.md`)*
- [ ] **M9-012**: UAT: LAB_TECHNICIAN — test orders, results, QC, dual-review *(READY — see `docs/uat/test-scripts/uat-lab-technician.md`)*
- [ ] **M9-013**: UAT: INVENTORY_MANAGER — stock, storage, transfers, disposal *(READY — see `docs/uat/test-scripts/uat-inventory-manager.md`)*
- [ ] **M9-014**: UAT: DOCTOR — blood requests, cross-match, transfusion orders *(READY — see `docs/uat/test-scripts/uat-doctor.md`)*
- [ ] **M9-015**: UAT: NURSE — transfusion administration, reaction reporting *(READY — see `docs/uat/test-scripts/uat-nurse.md`)*
- [ ] **M9-016**: UAT: BILLING_CLERK — invoices, payments, rates *(READY — see `docs/uat/test-scripts/uat-billing-clerk.md`)*
- [ ] **M9-017**: UAT: CAMP_COORDINATOR — camp planning, execution *(READY — see `docs/uat/test-scripts/uat-camp-coordinator.md`)*
- [ ] **M9-018**: UAT: HOSPITAL_USER — request portal, tracking, feedback *(READY — see `docs/uat/test-scripts/uat-hospital-user.md`)*
- [ ] **M9-019**: UAT: DONOR — self-registration, history, appointments, donor card *(READY — see `docs/uat/test-scripts/uat-donor.md`)*
- [ ] **M9-020**: UAT: AUDITOR — audit trail, compliance reports, read-only access *(READY — see `docs/uat/test-scripts/uat-auditor.md`)*
- [ ] **M9-021**: UAT: SYSTEM_ADMIN — monitoring, feature flags, scheduled jobs *(READY — see `docs/uat/test-scripts/uat-system-admin.md`)*

### Compliance Validation
- [x] **M9-022**: HIPAA: Verify PHI protection, access controls, encryption at rest/transit — `docs/compliance/hipaa-validation.md`
- [x] **M9-023**: HIPAA: Verify audit trail captures all PHI access — `docs/compliance/hipaa-validation.md`
- [x] **M9-024**: GDPR: Verify consent management workflow — `docs/compliance/gdpr-validation.md`
- [x] **M9-025**: GDPR: Verify data erasure/anonymization workflow — `docs/compliance/gdpr-validation.md`
- [x] **M9-026**: GDPR: Verify data portability export — `docs/compliance/gdpr-validation.md`
- [x] **M9-027**: FDA 21 CFR Part 11: Verify electronic signatures — `docs/compliance/fda-21cfr11-validation.md`
- [x] **M9-028**: FDA 21 CFR Part 11: Verify immutable audit trail — `docs/compliance/fda-21cfr11-validation.md`
- [x] **M9-029**: AABB: Verify vein-to-vein traceability (donor → recipient) — `docs/compliance/aabb-validation.md`
- [x] **M9-030**: AABB: Verify chain of custody logging — `docs/compliance/aabb-validation.md`
- [x] **M9-031**: WHO: Verify mandatory test panel enforcement — `docs/compliance/who-validation.md`
- [x] **M9-032**: WHO: Verify blood safety protocols — `docs/compliance/who-validation.md`

### Accessibility Testing
> ⚙️ **MANUAL PROCESS** — Items below require manual testing with real assistive technology
> (NVDA, VoiceOver, JAWS, axe-core Playwright runs). They are READY to execute.

- [ ] **M9-033**: WCAG 2.1 AA compliance testing with axe-core — `docs/compliance/accessibility-report.md` *(READY — checklist template prepared)*
- [ ] **M9-034**: Keyboard navigation testing for all features — `docs/compliance/accessibility-report.md` *(READY — checklist template prepared)*
- [ ] **M9-035**: Screen reader compatibility testing — `docs/compliance/accessibility-report.md` *(READY — checklist template prepared)*
- [ ] **M9-036**: Color contrast validation — `docs/compliance/accessibility-report.md` *(READY — checklist template prepared)*

### Defect Resolution
> ⚙️ **MANUAL PROCESS** — Items below depend on defects found during UAT execution (M9-006–M9-021).
> They are READY to begin once UAT sessions start.

- [ ] **M9-037**: Triage and prioritize UAT defects *(READY — defect register in `docs/uat/uat-tracking.md`)*
- [ ] **M9-038**: Fix critical and high-priority defects *(READY — blocked on M9-037)*
- [ ] **M9-039**: Regression test fixed defects *(READY — blocked on M9-038)*
- [ ] **M9-040**: UAT sign-off from all stakeholder groups *(READY — sign-off table in `docs/uat/README.md`)*

## Deliverables

1. UAT test report with pass/fail per scenario
2. Compliance validation report (HIPAA, GDPR, FDA, AABB, WHO)
3. Accessibility audit report (WCAG 2.1 AA)
4. Defect resolution report
5. Stakeholder UAT sign-off document
