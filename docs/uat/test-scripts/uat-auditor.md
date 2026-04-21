# UAT Test Script — AUDITOR

**Role**: AUDITOR
**Scope**: Global read-only (audit trail, compliance reports, all logs — no write access)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-auditor@bloodbank.test`
**Keycloak Realm Role**: `AUDITOR`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-AUD-001: Login and Auditor Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-auditor@bloodbank.test` / `UatAuditor!2026` | Authenticated; auditor dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe landing screen | Compliance summary, recent audit activity, pending reviews shown | ☐ Pass ☐ Fail | | |
| 4 | Verify read-only UI | Check all sections | No `+ Add`, `Edit`, or `Delete` buttons visible anywhere | ☐ Pass ☐ Fail | | |
| 5 | Verify role badge | Check top-right menu | Displays `AUDITOR` | ☐ Pass ☐ Fail | | |

---

## TC-AUD-002: Audit Log Search and Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance -> Audit Logs` | Audit log viewer loads with all branches visible | ☐ Pass ☐ Fail | | |
| 2 | Filter by user | Filter User = `uat-branch-admin@bloodbank.test` | Only actions by that user shown | ☐ Pass ☐ Fail | | |
| 3 | Filter by action type | Filter Action = `DISPOSAL_APPROVED` | Relevant audit entries shown | ☐ Pass ☐ Fail | | |
| 4 | Filter by date range | Set date range to today | Only today's entries shown | ☐ Pass ☐ Fail | | |
| 5 | Filter by branch | Filter Branch = `UAT Central Branch` | Only that branch's entries shown | ☐ Pass ☐ Fail | | |
| 6 | Export audit log | Click `Export CSV` | CSV file downloads with filtered audit data | ☐ Pass ☐ Fail | | |

---

## TC-AUD-003: Audit Log Immutability Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View an audit entry | Click on any audit log record | Audit detail shows: user, action, timestamp, IP address, entity, before/after values | ☐ Pass ☐ Fail | | |
| 2 | Attempt to edit entry | Look for edit/modify button on the audit record | No edit controls present | ☐ Pass ☐ Fail | | |
| 3 | Attempt to delete entry | Look for delete button | No delete controls present | ☐ Pass ☐ Fail | | |
| 4 | Verify digital signature | Check if audit entry has a digital signature or hash | Signature/hash present for FDA 21 CFR Part 11 compliance | ☐ Pass ☐ Fail | | |

---

## TC-AUD-004: Break-Glass Access Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Filter break-glass events | In Audit Logs, filter Action = `BREAK_GLASS` | All break-glass access events listed | ☐ Pass ☐ Fail | | |
| 2 | View break-glass event | Click on a break-glass entry from TC-DOC-004 | Entry shows: doctor, patient, units issued, timestamp, justification | ☐ Pass ☐ Fail | | |
| 3 | Verify post-incident review | Check if review was completed | Review status shown on break-glass event | ☐ Pass ☐ Fail | | |

---

## TC-AUD-005: Compliance Report Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Compliance Reports | Click `Compliance -> Reports` | List of compliance report types shown | ☐ Pass ☐ Fail | | |
| 2 | View HIPAA access report | Select `HIPAA PHI Access Report`, set date range | Report generates showing all PHI access events | ☐ Pass ☐ Fail | | |
| 3 | View AABB traceability | Select `AABB Vein-to-Vein Traceability Report` | Report shows complete chain from donor to recipient | ☐ Pass ☐ Fail | | |
| 4 | View FDA audit report | Select `FDA 21 CFR Part 11 Electronic Records Report` | Report lists all electronically signed actions with timestamps | ☐ Pass ☐ Fail | | |
| 5 | Export report | Click `Export PDF` | Report PDF downloaded | ☐ Pass ☐ Fail | | |

---

## TC-AUD-006: Deviation and CAPA Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Deviations | Click `Compliance -> Deviations` | All branch deviation records visible | ☐ Pass ☐ Fail | | |
| 2 | Filter by severity | Filter Severity=`Critical` | Only critical deviations shown | ☐ Pass ☐ Fail | | |
| 3 | View deviation detail | Click on a deviation | Full deviation record shown (reporter, date, description, CAPA status) | ☐ Pass ☐ Fail | | |
| 4 | Verify read-only | Try to edit deviation or CAPA | No edit controls visible | ☐ Pass ☐ Fail | | |

---

## TC-AUD-007: Recall Record Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Recalls | Click `Compliance -> Recalls` | All recall records visible (global scope) | ☐ Pass ☐ Fail | | |
| 2 | View recall detail | Click on a recall | Recall details shown (affected units, reason, initiator, resolution status) | ☐ Pass ☐ Fail | | |
| 3 | Verify read-only | Try to modify recall status | No edit controls available | ☐ Pass ☐ Fail | | |

---

## TC-AUD-008: Donor and Clinical Data — Read-Only

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors -> All Donors` | Donor list loads (read-only, PII masked for non-clinical auditor) | ☐ Pass ☐ Fail | | |
| 2 | Verify PII masking | Check name, email, national ID display | PII partially masked for auditor role (not a direct care provider) | ☐ Pass ☐ Fail | | |
| 3 | View deferrals | Navigate to a donor deferrals tab | Deferral history shown in read-only | ☐ Pass ☐ Fail | | |
| 4 | Navigate to Lab Results | Click `Lab -> Test Results` | Test results visible in read-only format | ☐ Pass ☐ Fail | | |
| 5 | Verify no data entry | Try to enter a test result | No data entry form accessible | ☐ Pass ☐ Fail | | |

---

## TC-AUD-009: Electronic Signature Audit Trail

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Digital Signatures | Click `Compliance -> Digital Signatures` | List of electronically signed actions loads | ☐ Pass ☐ Fail | | |
| 2 | View signature record | Click on a signed blood issue or test result approval | Signature details shown: signer identity, timestamp, action, hash | ☐ Pass ☐ Fail | | |
| 3 | Verify non-repudiation | Check that signature is tied to a unique user | Signer identity linked to Keycloak user ID | ☐ Pass ☐ Fail | | |

---

## TC-AUD-010: Restricted Write Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt to create a donor | Navigate to `Donors -> + Register New Donor` | Access denied or action not available | ☐ Pass ☐ Fail | | |
| 2 | Attempt to approve a disposal | Navigate to `Inventory -> Disposals`, try to approve | No approve button visible | ☐ Pass ☐ Fail | | |
| 3 | Attempt to modify system settings | Navigate to `Administration -> System Settings` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Attempt to create a test result | Navigate to `Lab -> Enter Test Results` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-AUD-001: Login and Auditor Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-002: Audit Log Search and Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-003: Audit Log Immutability Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-004: Break-Glass Access Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-005: Compliance Report Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-006: Deviation and CAPA Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-007: Recall Record Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-008: Donor and Clinical Data — Read-Only | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-009: Electronic Signature Audit Trail | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-AUD-010: Restricted Write Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 10
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
