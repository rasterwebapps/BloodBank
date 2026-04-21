# UAT Tracking — Scenarios, Testers, Status, and Defects

**Last Updated**: 2026-04-21
**Project**: BloodBank UAT — Milestone M9
**UAT Lead**: ___________________________
**Environment**: `https://uat.bloodbank.internal`

---

## How to Use This Document

1. Assign a **Tester** to each scenario block before each UAT session
2. After each session, update **Status** and record any **Defect IDs**
3. For each defect, fill in the Defect Register table below
4. Update scenario status once defects are re-tested

### Status Values

| Status | Meaning |
|---|---|
| `PENDING` | Not yet executed |
| `PASS` | Executed successfully |
| `FAIL` | Executed with defects |
| `PARTIAL` | Partially passing — minor deviation |
| `SKIP` | Skipped (feature not available / out of scope) |
| `BLOCKED` | Blocked by a prior defect |

---

## Session Log

| Session # | Date | Roles Tested | Tester(s) | Observer | Notes |
|---|---|---|---|---|---|
| S-01 | | BRANCH_ADMIN, BRANCH_MANAGER | | | |
| S-02 | | RECEPTIONIST, PHLEBOTOMIST | | | |
| S-03 | | DOCTOR, NURSE, LAB_TECHNICIAN | | | |
| S-04 | | INVENTORY_MANAGER, CAMP_COORDINATOR | | | |
| S-05 | | BILLING_CLERK | | | |
| S-06 | | HOSPITAL_USER, DONOR | | | |
| S-07 | | SUPER_ADMIN, REGIONAL_ADMIN, SYSTEM_ADMIN, AUDITOR | | | |

---

## Scenario Tracking — SUPER_ADMIN (15 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-SA-001 | Login and Dashboard Access | | S-07 | PENDING | | | |
| TC-SA-002 | Branch Creation | | S-07 | PENDING | | | |
| TC-SA-003 | Global System Settings | | S-07 | PENDING | | | |
| TC-SA-004 | Feature Flag Management | | S-07 | PENDING | | | |
| TC-SA-005 | Scheduled Job Monitoring | | S-07 | PENDING | | | |
| TC-SA-006 | Global Donor Search | | S-07 | PENDING | | | |
| TC-SA-007 | Global Stock Overview | | S-07 | PENDING | | | |
| TC-SA-008 | Global Reports | | S-07 | PENDING | | | |
| TC-SA-009 | Audit Log Access | | S-07 | PENDING | | | |
| TC-SA-010 | User Management | | S-07 | PENDING | | | |
| TC-SA-011 | Recall Management | | S-07 | PENDING | | | |
| TC-SA-012 | Emergency Broadcast | | S-07 | PENDING | | | |
| TC-SA-013 | Multi-Region Dashboard | | S-07 | PENDING | | | |
| TC-SA-014 | Disaster Event Management | | S-07 | PENDING | | | |
| TC-SA-015 | Notification Template Management | | S-07 | PENDING | | | |

---

## Scenario Tracking — REGIONAL_ADMIN (12 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-RA-001 | Login and Regional Dashboard | | S-07 | PENDING | | | |
| TC-RA-002 | Multi-Branch Stock Overview | | S-07 | PENDING | | | |
| TC-RA-003 | Approve Stock Transfer | | S-07 | PENDING | | | |
| TC-RA-004 | Regional Donation Report | | S-07 | PENDING | | | |
| TC-RA-005 | Branch Performance Dashboard | | S-07 | PENDING | | | |
| TC-RA-006 | Branch Settings Update | | S-07 | PENDING | | | |
| TC-RA-007 | Audit Log Access | | S-07 | PENDING | | | |
| TC-RA-008 | Emergency Request Oversight | | S-07 | PENDING | | | |
| TC-RA-009 | Donor Mobilization | | S-07 | PENDING | | | |
| TC-RA-010 | Hospital Contract Management | | S-07 | PENDING | | | |
| TC-RA-011 | Recall Oversight | | S-07 | PENDING | | | |
| TC-RA-012 | Notification Preferences | | S-07 | PENDING | | | |

---

## Scenario Tracking — BRANCH_ADMIN (20 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-BA-001 | Login and Branch Dashboard | | S-01 | PENDING | | | |
| TC-BA-002 | Branch Configuration | | S-01 | PENDING | | | |
| TC-BA-003 | Staff Management | | S-01 | PENDING | | | |
| TC-BA-004 | Donor Registration | | S-01 | PENDING | | | |
| TC-BA-005 | Deferral Management | | S-01 | PENDING | | | |
| TC-BA-006 | Blood Collection Oversight | | S-01 | PENDING | | | |
| TC-BA-007 | Lab Test Results Approval | | S-01 | PENDING | | | |
| TC-BA-008 | Blood Unit Disposal | | S-01 | PENDING | | | |
| TC-BA-009 | Invoice Management | | S-01 | PENDING | | | |
| TC-BA-010 | Stock Transfer Management | | S-01 | PENDING | | | |
| TC-BA-011 | SOP Document Access | | S-01 | PENDING | | | |
| TC-BA-012 | Deviation Reporting | | S-01 | PENDING | | | |
| TC-BA-013 | Hospital Request Approval | | S-01 | PENDING | | | |
| TC-BA-014 | Recall Initiation | | S-01 | PENDING | | | |
| TC-BA-015 | Hemovigilance Report Review | | S-01 | PENDING | | | |
| TC-BA-016 | Emergency Issue Override | | S-01 | PENDING | | | |
| TC-BA-017 | Document Upload | | S-01 | PENDING | | | |
| TC-BA-018 | Audit Trail Verification | | S-01 | PENDING | | | |
| TC-BA-019 | Notification Campaign | | S-01 | PENDING | | | |
| TC-BA-020 | Data Export (GDPR Portability) | | S-01 | PENDING | | | |

---

## Scenario Tracking — BRANCH_MANAGER (16 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-BM-001 | Login and Operational Dashboard | | S-01 | PENDING | | | |
| TC-BM-002 | Daily Operations Overview | | S-01 | PENDING | | | |
| TC-BM-003 | Lab Test Result Approval | | S-01 | PENDING | | | |
| TC-BM-004 | Blood Unit Issuing Approval | | S-01 | PENDING | | | |
| TC-BM-005 | Stock Transfer Approval | | S-01 | PENDING | | | |
| TC-BM-006 | Donor Management | | S-01 | PENDING | | | |
| TC-BM-007 | Collection Management | | S-01 | PENDING | | | |
| TC-BM-008 | Branch Report Generation | | S-01 | PENDING | | | |
| TC-BM-009 | Hospital Request Management | | S-01 | PENDING | | | |
| TC-BM-010 | Hemovigilance Report | | S-01 | PENDING | | | |
| TC-BM-011 | Emergency Blood Issue | | S-01 | PENDING | | | |
| TC-BM-012 | Notification Preferences | | S-01 | PENDING | | | |
| TC-BM-013 | Blood Camp Overview | | S-01 | PENDING | | | |
| TC-BM-014 | Dispatch Log Dashboard | | S-01 | PENDING | | | |
| TC-BM-015 | Deviation Approval | | S-01 | PENDING | | | |
| TC-BM-016 | Read-Only Audit Access | | S-01 | PENDING | | | |

---

## Scenario Tracking — RECEPTIONIST (14 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-REC-001 | Login and Reception Dashboard | | S-02 | PENDING | | | |
| TC-REC-002 | New Donor Walk-In Registration | | S-02 | PENDING | | | |
| TC-REC-003 | Duplicate Donor Detection | | S-02 | PENDING | | | |
| TC-REC-004 | Update Existing Donor Profile | | S-02 | PENDING | | | |
| TC-REC-005 | Donor Check-In for Walk-In | | S-02 | PENDING | | | |
| TC-REC-006 | Appointment Scheduling | | S-02 | PENDING | | | |
| TC-REC-007 | Cancel/Reschedule Appointment | | S-02 | PENDING | | | |
| TC-REC-008 | Deferral Check at Reception | | S-02 | PENDING | | | |
| TC-REC-009 | Donor Search and Read-Only View | | S-02 | PENDING | | | |
| TC-REC-010 | Blood Camp Registration | | S-02 | PENDING | | | |
| TC-REC-011 | Notification Preferences | | S-02 | PENDING | | | |
| TC-REC-012 | Consent Document Upload | | S-02 | PENDING | | | |
| TC-REC-013 | Restricted Access Verification | | S-02 | PENDING | | | |
| TC-REC-014 | Loyalty Points View | | S-02 | PENDING | | | |

---

## Scenario Tracking — PHLEBOTOMIST (13 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-PHL-001 | Login and Collection Dashboard | | S-02 | PENDING | | | |
| TC-PHL-002 | Pre-Donation Vitals Recording | | S-02 | PENDING | | | |
| TC-PHL-003 | Blood Collection Recording | | S-02 | PENDING | | | |
| TC-PHL-004 | Record Adverse Reaction | | S-02 | PENDING | | | |
| TC-PHL-005 | Donor Deferral During Screening | | S-02 | PENDING | | | |
| TC-PHL-006 | Blood Camp Collection | | S-02 | PENDING | | | |
| TC-PHL-007 | View Donor Health Records | | S-02 | PENDING | | | |
| TC-PHL-008 | View Collection History | | S-02 | PENDING | | | |
| TC-PHL-009 | Update Collection Record | | S-02 | PENDING | | | |
| TC-PHL-010 | Restricted Access Verification | | S-02 | PENDING | | | |
| TC-PHL-011 | Donor Notification on Collection | | S-02 | PENDING | | | |
| TC-PHL-012 | Mandatory Test Panel Verification | | S-02 | PENDING | | | |
| TC-PHL-013 | Print Donation Receipt / Labels | | S-02 | PENDING | | | |

---

## Scenario Tracking — LAB_TECHNICIAN (14 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-LAB-001 | Login and Lab Dashboard | | S-03 | PENDING | | | |
| TC-LAB-002 | Create Test Order | | S-03 | PENDING | | | |
| TC-LAB-003 | Enter Test Results — All Negative | | S-03 | PENDING | | | |
| TC-LAB-004 | Enter Test Results — Reactive | | S-03 | PENDING | | | |
| TC-LAB-005 | Quality Control Record | | S-03 | PENDING | | | |
| TC-LAB-006 | Blood Component Processing | | S-03 | PENDING | | | |
| TC-LAB-007 | Cross-Match Result Entry | | S-03 | PENDING | | | |
| TC-LAB-008 | View and Search Test Results | | S-03 | PENDING | | | |
| TC-LAB-009 | Blood Issue Initiation | | S-03 | PENDING | | | |
| TC-LAB-010 | Deviation Reporting | | S-03 | PENDING | | | |
| TC-LAB-011 | SOP Document Access | | S-03 | PENDING | | | |
| TC-LAB-012 | Instrument Management | | S-03 | PENDING | | | |
| TC-LAB-013 | Restricted Access Verification | | S-03 | PENDING | | | |
| TC-LAB-014 | Upload Lab Document | | S-03 | PENDING | | | |

---

## Scenario Tracking — INVENTORY_MANAGER (15 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-INV-001 | Login and Inventory Dashboard | | S-04 | PENDING | | | |
| TC-INV-002 | View Blood Unit Inventory | | S-04 | PENDING | | | |
| TC-INV-003 | Blood Component Inventory | | S-04 | PENDING | | | |
| TC-INV-004 | Storage Location Management | | S-04 | PENDING | | | |
| TC-INV-005 | Stock Transfer Request | | S-04 | PENDING | | | |
| TC-INV-006 | Unit Disposal Request | | S-04 | PENDING | | | |
| TC-INV-007 | Unit Reservation | | S-04 | PENDING | | | |
| TC-INV-008 | Transport Request | | S-04 | PENDING | | | |
| TC-INV-009 | Expiry Management | | S-04 | PENDING | | | |
| TC-INV-010 | Emergency Request Handling | | S-04 | PENDING | | | |
| TC-INV-011 | Stock Alert Configuration | | S-04 | PENDING | | | |
| TC-INV-012 | Blood Components Read Access | | S-04 | PENDING | | | |
| TC-INV-013 | Restricted Access Verification | | S-04 | PENDING | | | |
| TC-INV-014 | Notification Preferences | | S-04 | PENDING | | | |
| TC-INV-015 | Recall Participation | | S-04 | PENDING | | | |

---

## Scenario Tracking — DOCTOR (16 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-DOC-001 | Login and Clinical Dashboard | | S-03 | PENDING | | | |
| TC-DOC-002 | Cross-Match Request | | S-03 | PENDING | | | |
| TC-DOC-003 | Receive Cross-Match Result and Issue Blood | | S-03 | PENDING | | | |
| TC-DOC-004 | Emergency Blood Issue (Break-Glass) | | S-03 | PENDING | | | |
| TC-DOC-005 | Blood Request to Hospital | | S-03 | PENDING | | | |
| TC-DOC-006 | Transfusion Order | | S-03 | PENDING | | | |
| TC-DOC-007 | View Transfusion Status | | S-03 | PENDING | | | |
| TC-DOC-008 | Hemovigilance Report | | S-03 | PENDING | | | |
| TC-DOC-009 | Transfusion Reaction — Doctor Response | | S-03 | PENDING | | | |
| TC-DOC-010 | Deviation Reporting (Clinical) | | S-03 | PENDING | | | |
| TC-DOC-011 | Donor Health Record | | S-03 | PENDING | | | |
| TC-DOC-012 | Read-Only Access Verification | | S-03 | PENDING | | | |
| TC-DOC-013 | Document Upload | | S-03 | PENDING | | | |
| TC-DOC-014 | SOP and Regulatory Compliance | | S-03 | PENDING | | | |
| TC-DOC-015 | Notification Preferences | | S-03 | PENDING | | | |
| TC-DOC-016 | Lookback Investigation Access | | S-03 | PENDING | | | |

---

## Scenario Tracking — NURSE (12 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-NUR-001 | Login and Nursing Dashboard | | S-03 | PENDING | | | |
| TC-NUR-002 | Administer Transfusion | | S-03 | PENDING | | | |
| TC-NUR-003 | Transfusion Reaction Reporting | | S-03 | PENDING | | | |
| TC-NUR-004 | Post-Transfusion Documentation | | S-03 | PENDING | | | |
| TC-NUR-005 | View Transfusion History | | S-03 | PENDING | | | |
| TC-NUR-006 | View Hemovigilance Reports | | S-03 | PENDING | | | |
| TC-NUR-007 | Collection Adverse Reaction Reporting | | S-03 | PENDING | | | |
| TC-NUR-008 | Read-Only Donor Information | | S-03 | PENDING | | | |
| TC-NUR-009 | Notification Preferences | | S-03 | PENDING | | | |
| TC-NUR-010 | Restricted Access Verification | | S-03 | PENDING | | | |
| TC-NUR-011 | SOP Document Access | | S-03 | PENDING | | | |
| TC-NUR-012 | Transfusion Reaction — Escalation Workflow | | S-03 | PENDING | | | |

---

## Scenario Tracking — BILLING_CLERK (12 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-BIL-001 | Login and Billing Dashboard | | S-05 | PENDING | | | |
| TC-BIL-002 | Rate Master Management | | S-05 | PENDING | | | |
| TC-BIL-003 | Create Invoice | | S-05 | PENDING | | | |
| TC-BIL-004 | View and Manage Invoices | | S-05 | PENDING | | | |
| TC-BIL-005 | Record Payment | | S-05 | PENDING | | | |
| TC-BIL-006 | Partial Payment | | S-05 | PENDING | | | |
| TC-BIL-007 | Credit Note | | S-05 | PENDING | | | |
| TC-BIL-008 | Read-Only Access to Blood Issues | | S-05 | PENDING | | | |
| TC-BIL-009 | Hospital Invoice Dashboard | | S-05 | PENDING | | | |
| TC-BIL-010 | Billing Report | | S-05 | PENDING | | | |
| TC-BIL-011 | Restricted Access Verification | | S-05 | PENDING | | | |
| TC-BIL-012 | Notification Preferences | | S-05 | PENDING | | | |

---

## Scenario Tracking — CAMP_COORDINATOR (12 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-CAM-001 | Login and Camp Dashboard | | S-04 | PENDING | | | |
| TC-CAM-002 | Create Blood Camp | | S-04 | PENDING | | | |
| TC-CAM-003 | Resource Planning | | S-04 | PENDING | | | |
| TC-CAM-004 | Donor Pre-Registration | | S-04 | PENDING | | | |
| TC-CAM-005 | Camp Collection Recording | | S-04 | PENDING | | | |
| TC-CAM-006 | Update Camp Status | | S-04 | PENDING | | | |
| TC-CAM-007 | Update Camp Details | | S-04 | PENDING | | | |
| TC-CAM-008 | Donor Mobilization Campaign | | S-04 | PENDING | | | |
| TC-CAM-009 | Notification Campaign for Camp | | S-04 | PENDING | | | |
| TC-CAM-010 | Read-Only Donor Access | | S-04 | PENDING | | | |
| TC-CAM-011 | Restricted Access Verification | | S-04 | PENDING | | | |
| TC-CAM-012 | Camp Report | | S-04 | PENDING | | | |

---

## Scenario Tracking — HOSPITAL_USER (10 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-HOS-001 | Login and Hospital Portal Dashboard | | S-06 | PENDING | | | |
| TC-HOS-002 | Submit Routine Blood Request | | S-06 | PENDING | | | |
| TC-HOS-003 | Submit Urgent Blood Request | | S-06 | PENDING | | | |
| TC-HOS-004 | Submit Emergency Blood Request | | S-06 | PENDING | | | |
| TC-HOS-005 | Track Request Status | | S-06 | PENDING | | | |
| TC-HOS-006 | View Delivery Status | | S-06 | PENDING | | | |
| TC-HOS-007 | Submit Cross-Match Request | | S-06 | PENDING | | | |
| TC-HOS-008 | Submit Hospital Feedback | | S-06 | PENDING | | | |
| TC-HOS-009 | View Hospital Contract and Rate | | S-06 | PENDING | | | |
| TC-HOS-010 | Restricted Access Verification | | S-06 | PENDING | | | |

---

## Scenario Tracking — DONOR (10 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-DON-001 | Self-Registration | | S-06 | PENDING | | | |
| TC-DON-002 | Login and Donor Dashboard | | S-06 | PENDING | | | |
| TC-DON-003 | View Donation History | | S-06 | PENDING | | | |
| TC-DON-004 | Book Donation Appointment | | S-06 | PENDING | | | |
| TC-DON-005 | Cancel Appointment | | S-06 | PENDING | | | |
| TC-DON-006 | View and Download Donor Card | | S-06 | PENDING | | | |
| TC-DON-007 | Update Profile | | S-06 | PENDING | | | |
| TC-DON-008 | View Deferral Status | | S-06 | PENDING | | | |
| TC-DON-009 | Notification Preferences | | S-06 | PENDING | | | |
| TC-DON-010 | GDPR — Export My Data | | S-06 | PENDING | | | |

---

## Scenario Tracking — AUDITOR (10 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-AUD-001 | Login and Auditor Dashboard | | S-07 | PENDING | | | |
| TC-AUD-002 | Audit Log Search and Review | | S-07 | PENDING | | | |
| TC-AUD-003 | Audit Log Immutability Verification | | S-07 | PENDING | | | |
| TC-AUD-004 | Break-Glass Access Review | | S-07 | PENDING | | | |
| TC-AUD-005 | Compliance Report Review | | S-07 | PENDING | | | |
| TC-AUD-006 | Deviation and CAPA Review | | S-07 | PENDING | | | |
| TC-AUD-007 | Recall Record Review | | S-07 | PENDING | | | |
| TC-AUD-008 | Donor and Clinical Data — Read-Only | | S-07 | PENDING | | | |
| TC-AUD-009 | Electronic Signature Audit Trail | | S-07 | PENDING | | | |
| TC-AUD-010 | Restricted Write Access Verification | | S-07 | PENDING | | | |

---

## Scenario Tracking — SYSTEM_ADMIN (11 scenarios)

| Scenario ID | Description | Tester | Session | Status | Defect ID | Re-test Date | Re-test Status |
|---|---|---|---|---|---|---|---|
| TC-SYA-001 | Login and System Dashboard | | S-07 | PENDING | | | |
| TC-SYA-002 | System Settings Management | | S-07 | PENDING | | | |
| TC-SYA-003 | Feature Flag Management | | S-07 | PENDING | | | |
| TC-SYA-004 | Scheduled Job Management | | S-07 | PENDING | | | |
| TC-SYA-005 | Master Data — Blood Groups | | S-07 | PENDING | | | |
| TC-SYA-006 | Master Data — Component Types | | S-07 | PENDING | | | |
| TC-SYA-007 | Notification Template Management | | S-07 | PENDING | | | |
| TC-SYA-008 | Service Health Monitoring | | S-07 | PENDING | | | |
| TC-SYA-009 | Audit Log Access | | S-07 | PENDING | | | |
| TC-SYA-010 | Regulatory Framework Management | | S-07 | PENDING | | | |
| TC-SYA-011 | Restricted Access Verification | | S-07 | PENDING | | | |

---

## UAT Progress Summary

| Role | Total | Pass | Fail | Partial | Skip | Blocked | Pass% |
|---|---|---|---|---|---|---|---|
| SUPER_ADMIN | 15 | 0 | 0 | 0 | 0 | 0 | 0% |
| REGIONAL_ADMIN | 12 | 0 | 0 | 0 | 0 | 0 | 0% |
| BRANCH_ADMIN | 20 | 0 | 0 | 0 | 0 | 0 | 0% |
| BRANCH_MANAGER | 16 | 0 | 0 | 0 | 0 | 0 | 0% |
| RECEPTIONIST | 14 | 0 | 0 | 0 | 0 | 0 | 0% |
| PHLEBOTOMIST | 13 | 0 | 0 | 0 | 0 | 0 | 0% |
| LAB_TECHNICIAN | 14 | 0 | 0 | 0 | 0 | 0 | 0% |
| INVENTORY_MANAGER | 15 | 0 | 0 | 0 | 0 | 0 | 0% |
| DOCTOR | 16 | 0 | 0 | 0 | 0 | 0 | 0% |
| NURSE | 12 | 0 | 0 | 0 | 0 | 0 | 0% |
| BILLING_CLERK | 12 | 0 | 0 | 0 | 0 | 0 | 0% |
| CAMP_COORDINATOR | 12 | 0 | 0 | 0 | 0 | 0 | 0% |
| HOSPITAL_USER | 10 | 0 | 0 | 0 | 0 | 0 | 0% |
| DONOR | 10 | 0 | 0 | 0 | 0 | 0 | 0% |
| AUDITOR | 10 | 0 | 0 | 0 | 0 | 0 | 0% |
| SYSTEM_ADMIN | 11 | 0 | 0 | 0 | 0 | 0 | 0% |
| **TOTAL** | **182** | **0** | **0** | **0** | **0** | **0** | **0%** |

---

## Defect Register

| Defect ID | Raised Date | Scenario | Description | Severity | Reproducible | Assigned To | Status | Fix Version | Verified By | Closed Date |
|---|---|---|---|---|---|---|---|---|---|---|
| | | | | | | | | | | |

### Defect Status Values

| Status | Description |
|---|---|
| `OPEN` | Defect reported; not yet assigned |
| `IN PROGRESS` | Developer actively working on fix |
| `FIXED` | Fix implemented; awaiting re-test |
| `VERIFIED` | Re-tested by UAT tester and confirmed fixed |
| `CLOSED` | Defect closed after successful verification |
| `WONT FIX` | Accepted as known limitation / deferred to backlog |
| `DUPLICATE` | Duplicate of an existing defect |

---

## UAT Sign-Off Checklist

### Pre-Sign-Off Requirements

- [ ] All P1 defects: VERIFIED
- [ ] All P2 defects: VERIFIED
- [ ] Overall pass rate >= 95% (minimum 173 of 182 scenarios)
- [ ] No open P3 defects affecting patient safety workflows
- [ ] Compliance scenarios passed (HIPAA, GDPR, FDA, AABB, WHO)
- [ ] Accessibility scenarios passed (WCAG 2.1 AA)

### Stakeholder Sign-Off

| Stakeholder Group | Representative | Signature | Date | Status |
|---|---|---|---|---|
| Blood Bank Operations | | | | PENDING |
| Clinical | | | | PENDING |
| Inventory & Logistics | | | | PENDING |
| Finance | | | | PENDING |
| External Partners | | | | PENDING |
| Administration | | | | PENDING |
| UAT Lead | | | | PENDING |

---

*Last updated: 2026-04-21*
