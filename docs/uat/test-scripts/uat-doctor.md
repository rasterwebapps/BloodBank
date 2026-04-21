# UAT Test Script — DOCTOR

**Role**: DOCTOR
**Scope**: Branch-level (blood requests, cross-match orders, transfusion prescriptions, hemovigilance)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-doctor@bloodbank.test`
**Keycloak Client Role**: `DOCTOR`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-DOC-001: Login and Clinical Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-doctor@bloodbank.test` / `UatDoctor!2026` | Authenticated; clinical dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View dashboard widgets | Observe dashboard | Pending cross-match requests, active transfusions, recent blood issues shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `DOCTOR` | ☐ Pass ☐ Fail | | |

---

## TC-DOC-002: Cross-Match Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Cross-Match | Click `Clinical -> Cross-Match -> + New Request` | Cross-match request form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill patient details | Patient Name=`UAT Patient A`, ID=`PAT-UAT-001`, Blood Group=`B+`, DOB=`1975-08-22` | Fields accept input | ☐ Pass ☐ Fail | | |
| 3 | Set clinical indication | Indication=`Pre-operative`, Units Required=2, Urgency=`Routine` | Indication set | ☐ Pass ☐ Fail | | |
| 4 | Submit request | Click `Submit Cross-Match Request` | Request created; lab technician notified; status=`Pending` | ☐ Pass ☐ Fail | | |
| 5 | View request status | Check request in `My Cross-Match Requests` | Request shows with status and ETA | ☐ Pass ☐ Fail | | |

---

## TC-DOC-003: Receive Cross-Match Result and Issue Blood

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View completed cross-match | Navigate to cross-match request from TC-DOC-002 | Status=`Compatible` result shown | ☐ Pass ☐ Fail | | |
| 2 | Review compatibility result | Check result details | Compatible units listed with unit IDs | ☐ Pass ☐ Fail | | |
| 3 | Request blood issue | Click `Issue Blood` | Blood issue form pre-filled with patient and unit info | ☐ Pass ☐ Fail | | |
| 4 | Confirm issue | Click `Confirm Issue Request` | Issue request submitted for approval; manager notified | ☐ Pass ☐ Fail | | |

---

## TC-DOC-004: Emergency Blood Issue (Break-Glass)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Emergency Issue | Click `Clinical -> Blood Issues -> Emergency Issue` | Emergency issue form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill emergency details | Patient=`UAT Trauma Patient`, Units=2 x `O-`, Justification=`Hemorrhagic shock — no time for cross-match` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit emergency issue | Click `Issue Emergency Units` | Break-glass issue created; stock decremented; audit record contains `BREAK_GLASS` action | ☐ Pass ☐ Fail | | |
| 4 | Verify audit trail | Navigate to `Compliance -> Audit Logs`, filter Action=`BREAK_GLASS` | Break-glass entry recorded | ☐ Pass ☐ Fail | | |
| 5 | Verify auto-revoke | Wait 60 minutes or ask observer to verify | Break-glass access auto-revoked after 60 minutes | ☐ Pass ☐ Fail | | |

---

## TC-DOC-005: Blood Request to Hospital

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospital Requests | Click `Hospital -> Requests -> + New Request` | Hospital blood request form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill request | Hospital=`UAT General Hospital`, Blood Group=`A+`, Units=4, Urgency=`Urgent`, Clinical Indication=`Post-surgical` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit Request` | Request submitted; `BloodRequestCreatedEvent` fires; blood bank notified | ☐ Pass ☐ Fail | | |
| 4 | Track request | View request in `My Hospital Requests` | Request status visible | ☐ Pass ☐ Fail | | |

---

## TC-DOC-006: Transfusion Order

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfusions | Click `Clinical -> Transfusions -> + New` | Transfusion form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill transfusion details | Patient=`UAT Patient A`, Blood Unit=select issued unit, Indication=`Anemia`, Prescribing Doctor=self | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Set pre-transfusion vitals | BP=125/82, Temp=37.0, Pulse=78 | Vitals recorded | ☐ Pass ☐ Fail | | |
| 4 | Submit transfusion | Click `Start Transfusion` | Transfusion created; nurse notified for administration | ☐ Pass ☐ Fail | | |

---

## TC-DOC-007: View Transfusion Status

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfusions | Click `Clinical -> Transfusions` | Transfusion list loads | ☐ Pass ☐ Fail | | |
| 2 | View in-progress transfusion | Click on transfusion from TC-DOC-006 | Transfusion detail shows status, unit info, nurse administering | ☐ Pass ☐ Fail | | |
| 3 | Update transfusion | Click `Update Transfusion`, add note | Update saved | ☐ Pass ☐ Fail | | |

---

## TC-DOC-008: Hemovigilance Report

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hemovigilance | Click `Clinical -> Hemovigilance Reports -> + New` | Hemovigilance form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill report | Transfusion ID=select, Reaction Type=`Urticaria`, Severity=`Mild`, Onset=30min post-infusion | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit report | Click `Submit` | Report created; `TransfusionReactionEvent` fires; manager notified | ☐ Pass ☐ Fail | | |
| 4 | View report | Click on submitted report | Full report with all fields shown | ☐ Pass ☐ Fail | | |

---

## TC-DOC-009: Transfusion Reaction — Doctor Response

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View transfusion reactions | Click `Clinical -> Transfusion Reactions` | Active reactions listed | ☐ Pass ☐ Fail | | |
| 2 | Document clinical response | Click on a reaction, click `Add Clinical Note` | Clinical note form opens | ☐ Pass ☐ Fail | | |
| 3 | Submit clinical note | Enter intervention details, click `Save` | Note saved and linked to transfusion reaction record | ☐ Pass ☐ Fail | | |

---

## TC-DOC-010: Deviation Reporting (Clinical)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Deviations | Click `Compliance -> Deviations -> + New` | Deviation form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill deviation | Type=`Clinical`, Description=`Patient received wrong blood group — near miss`, Severity=`Critical` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit` | Deviation recorded; escalation alert sent | ☐ Pass ☐ Fail | | |

---

## TC-DOC-011: Donor Health Record

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors -> All Donors` | Donor list loads (read-only for doctor) | ☐ Pass ☐ Fail | | |
| 2 | Search donor | Search by name | Donor found | ☐ Pass ☐ Fail | | |
| 3 | Add health record | Click on donor, click `+ Add Health Record` | Health record form opens | ☐ Pass ☐ Fail | | |
| 4 | Submit health record | Fill and submit | Health record added; donor profile updated | ☐ Pass ☐ Fail | | |
| 5 | Add deferral | Click `+ Add Deferral` | Deferral form opens | ☐ Pass ☐ Fail | | |
| 6 | Submit deferral | Fill deferral, reason=`Hemoglobin disorder`, submit | Deferral created | ☐ Pass ☐ Fail | | |

---

## TC-DOC-012: Read-Only Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View inventory | Navigate to `Inventory -> Blood Units` | Read-only view of available units | ☐ Pass ☐ Fail | | |
| 2 | Verify no stock editing | Try to edit a blood unit | No edit controls visible for doctor | ☐ Pass ☐ Fail | | |
| 3 | View test results | Navigate to `Lab -> Test Results` | Read-only view of test results | ☐ Pass ☐ Fail | | |
| 4 | Verify no lab editing | Try to enter test results | No data entry form accessible | ☐ Pass ☐ Fail | | |

---

## TC-DOC-013: Document Upload

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Documents | Click `Documents -> Clinical Documents` | Document list loads | ☐ Pass ☐ Fail | | |
| 2 | Upload clinical document | Click `+ Upload`, select a PDF, category=`Clinical Report` | Upload dialog opens | ☐ Pass ☐ Fail | | |
| 3 | Complete upload | Click `Upload` | Document uploaded and stored | ☐ Pass ☐ Fail | | |

---

## TC-DOC-014: SOP and Regulatory Compliance

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to SOPs | Click `Compliance -> SOP Documents` | SOP list loads | ☐ Pass ☐ Fail | | |
| 2 | View transfusion SOP | Click on `Transfusion Administration SOP v2.0` | SOP opens | ☐ Pass ☐ Fail | | |
| 3 | View regulatory frameworks | Click `Compliance -> Regulatory Frameworks` | HIPAA, AABB, WHO frameworks listed | ☐ Pass ☐ Fail | | |

---

## TC-DOC-015: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on user profile | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Enable `Cross-Match Ready` alert, `Transfusion Reaction Alert` | Preferences saved | ☐ Pass ☐ Fail | | |

---

## TC-DOC-016: Lookback Investigation Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Lookback | Click `Clinical -> Lookback Investigations` | Lookback investigation list loads | ☐ Pass ☐ Fail | | |
| 2 | View a lookback | Click on an investigation | Vein-to-vein traceability shown (donor -> collection -> testing -> storage -> transfusion) | ☐ Pass ☐ Fail | | |
| 3 | Verify complete chain | Check all links in chain of custody | All steps traceable end-to-end | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-DOC-001: Login and Clinical Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-002: Cross-Match Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-003: Receive Cross-Match Result and Issue Blood | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-004: Emergency Blood Issue (Break-Glass) | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-005: Blood Request to Hospital | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-006: Transfusion Order | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-007: View Transfusion Status | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-008: Hemovigilance Report | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-009: Transfusion Reaction — Doctor Response | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-010: Deviation Reporting (Clinical) | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-011: Donor Health Record | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-012: Read-Only Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-013: Document Upload | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-014: SOP and Regulatory Compliance | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-015: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DOC-016: Lookback Investigation Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 16
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
