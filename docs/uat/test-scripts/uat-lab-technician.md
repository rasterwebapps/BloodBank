# UAT Test Script — LAB_TECHNICIAN

**Role**: LAB_TECHNICIAN
**Scope**: Branch-level (test orders, results entry, QC, dual-review, component processing)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-lab-technician@bloodbank.test`
**Keycloak Client Role**: `LAB_TECHNICIAN`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-LAB-001: Login and Lab Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-lab-technician@bloodbank.test` / `UatLabTech!2026` | Authenticated; lab workstation dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View pending orders | Check pending test orders widget | Outstanding test orders shown with priority | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right | Displays `LAB_TECHNICIAN` | ☐ Pass ☐ Fail | | |

---

## TC-LAB-002: Create Test Order

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Test Orders | Click `Lab -> Test Orders -> + New Order` | Test order form opens | ☐ Pass ☐ Fail | | |
| 2 | Select blood unit | Search unit by sample tube ID from TC-PHL-003 | Unit found and selected | ☐ Pass ☐ Fail | | |
| 3 | Select test panel | Choose `Full TTI Panel` (HIV, HBV, HCV, Syphilis, Malaria) + `ABO/Rh Typing` | All tests added to order | ☐ Pass ☐ Fail | | |
| 4 | Assign instrument | Select `Analyzer-1` | Instrument assigned | ☐ Pass ☐ Fail | | |
| 5 | Submit order | Click `Submit Test Order` | Order created with unique test order ID; status=`Pending` | ☐ Pass ☐ Fail | | |
| 6 | Verify order in list | Check `Pending Orders` tab | New order appears | ☐ Pass ☐ Fail | | |

---

## TC-LAB-003: Enter Test Results — All Negative

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open test order | Click on order from TC-LAB-002 | Test order detail with result entry form | ☐ Pass ☐ Fail | | |
| 2 | Enter TTI results | HIV=`Non-Reactive`, HBsAg=`Non-Reactive`, HCV=`Non-Reactive`, VDRL=`Non-Reactive`, Malaria=`Negative` | All fields accept values | ☐ Pass ☐ Fail | | |
| 3 | Enter ABO/Rh | Blood Group=`B+`, ABO=`B`, Rh=`Positive` | Result recorded | ☐ Pass ☐ Fail | | |
| 4 | Save results | Click `Save Results` | Results saved with status `Pending Verification` | ☐ Pass ☐ Fail | | |
| 5 | Note dual-review requirement | Check status | Status shows "Awaiting supervisor verification" | ☐ Pass ☐ Fail | | |

---

## TC-LAB-004: Enter Test Results — Reactive (Positive Screening)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open a different test order | Select another pending test order | Test order opens | ☐ Pass ☐ Fail | | |
| 2 | Enter reactive result | Set HBsAg=`Reactive` | System flags reactive result with visual warning | ☐ Pass ☐ Fail | | |
| 3 | Save results | Click `Save Results` | Results saved; blood unit automatically quarantined; alert sent to BRANCH_MANAGER | ☐ Pass ☐ Fail | | |
| 4 | Verify unit status | Check blood unit in inventory | Unit status=`QUARANTINED` | ☐ Pass ☐ Fail | | |
| 5 | Verify donor deferral alert | Check if deferral notification triggered | Alert visible for receptionist/phlebotomist to defer donor | ☐ Pass ☐ Fail | | |

---

## TC-LAB-005: Quality Control Record

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to QC | Click `Lab -> Quality Control` | QC records list loads | ☐ Pass ☐ Fail | | |
| 2 | Create QC record | Click `+ New QC Record` | QC form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill QC data | Instrument=`Analyzer-1`, Lot=`UAT-LOT-2026`, Controls=within acceptable range | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit QC` | QC record created; if out-of-range, system flags for review | ☐ Pass ☐ Fail | | |
| 5 | Out-of-range test | Enter a control value outside acceptable limits | System warns: "QC out of range — results may not be reportable" | ☐ Pass ☐ Fail | | |

---

## TC-LAB-006: Blood Component Processing

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Components | Click `Inventory -> Component Processing -> + New` | Component processing form opens | ☐ Pass ☐ Fail | | |
| 2 | Select blood unit | Select a `Released` unit | Unit selected | ☐ Pass ☐ Fail | | |
| 3 | Add component | Component Type=`Packed Red Blood Cells`, Volume=280ml | Component added | ☐ Pass ☐ Fail | | |
| 4 | Add second component | Component Type=`Fresh Frozen Plasma`, Volume=150ml | Second component added | ☐ Pass ☐ Fail | | |
| 5 | Submit processing | Click `Submit` | Components created; linked to parent blood unit; labels generated | ☐ Pass ☐ Fail | | |

---

## TC-LAB-007: Cross-Match Result Entry

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Cross-Match | Click `Lab -> Cross-Match` | Cross-match request list loads | ☐ Pass ☐ Fail | | |
| 2 | Find pending request | Select a pending cross-match request | Cross-match detail opens | ☐ Pass ☐ Fail | | |
| 3 | Enter cross-match result | Result=`Compatible`, Method=`Immediate Spin`, Technician=self | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit result | Click `Submit Cross-Match Result` | Result recorded; requesting doctor notified | ☐ Pass ☐ Fail | | |
| 5 | Submit incompatible result | On another request, set Result=`Incompatible` | Alert sent to requesting doctor; blood issue blocked | ☐ Pass ☐ Fail | | |

---

## TC-LAB-008: View and Search Test Results

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Test Results | Click `Lab -> Test Results` | Results list loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by status | Filter = `Pending Verification` | Only unverified results shown | ☐ Pass ☐ Fail | | |
| 3 | Search by unit ID | Search for unit ID from TC-LAB-003 | Correct result found | ☐ Pass ☐ Fail | | |
| 4 | View result detail | Click on result | Full result panel shown | ☐ Pass ☐ Fail | | |

---

## TC-LAB-009: Blood Issue Initiation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Blood Issues | Click `Clinical -> Blood Issues -> + New Issue` | Blood issue form opens | ☐ Pass ☐ Fail | | |
| 2 | Select unit | Choose a compatible, released unit | Unit selected | ☐ Pass ☐ Fail | | |
| 3 | Fill issue details | Patient ID, Hospital, Requesting Doctor | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit for approval | Click `Submit for Approval` | Issue request created; awaiting BRANCH_MANAGER approval | ☐ Pass ☐ Fail | | |

---

## TC-LAB-010: Deviation Reporting

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Deviations | Click `Compliance -> Deviations` | Deviation list loads | ☐ Pass ☐ Fail | | |
| 2 | Create deviation | Click `+ New Deviation` | Form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill details | Type=`Equipment`, Description=`Analyzer-1 flagged internal error during batch run`, Severity=`Major` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit` | Deviation created; CAPA workflow initiated | ☐ Pass ☐ Fail | | |

---

## TC-LAB-011: SOP Document Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to SOPs | Click `Compliance -> SOP Documents` | SOP list loads | ☐ Pass ☐ Fail | | |
| 2 | View lab SOP | Click on `TTI Testing Procedure v3.1` | Document opens or downloads | ☐ Pass ☐ Fail | | |
| 3 | Search SOPs | Search `cross-match` | Relevant SOP returned | ☐ Pass ☐ Fail | | |

---

## TC-LAB-012: Instrument Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Instruments | Click `Lab -> Instruments` | Instrument list loads | ☐ Pass ☐ Fail | | |
| 2 | View instrument | Click on `Analyzer-1` | Instrument details shown (model, serial, calibration date, status) | ☐ Pass ☐ Fail | | |
| 3 | Mark for maintenance | Click `Schedule Maintenance` | Maintenance record created; instrument status updated | ☐ Pass ☐ Fail | | |

---

## TC-LAB-013: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt donor registration | Navigate to `Donors -> Register New Donor` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt billing access | Navigate to `Billing -> Invoices` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt system settings | Navigate to `Administration -> System Settings` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## TC-LAB-014: Upload Lab Document

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Documents | Click `Documents -> Lab Documents` | Lab document list loads | ☐ Pass ☐ Fail | | |
| 2 | Upload lab report | Click `+ Upload`, select PDF (QC report), set category=`Lab Report` | Upload dialog opens | ☐ Pass ☐ Fail | | |
| 3 | Complete upload | Click `Upload` | Document stored; linked to branch | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-LAB-001: Login and Lab Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-002: Create Test Order | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-003: Enter Test Results — All Negative | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-004: Enter Test Results — Reactive | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-005: Quality Control Record | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-006: Blood Component Processing | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-007: Cross-Match Result Entry | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-008: View and Search Test Results | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-009: Blood Issue Initiation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-010: Deviation Reporting | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-011: SOP Document Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-012: Instrument Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-013: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-LAB-014: Upload Lab Document | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 14
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
