# UAT Test Script — BRANCH_MANAGER

**Role**: BRANCH_MANAGER
**Scope**: Branch-level (operational oversight, approvals, reports)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-branch-manager@bloodbank.test`
**Keycloak Client Role**: `BRANCH_MANAGER`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-BM-001: Login and Operational Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-branch-manager@bloodbank.test` / `UatBranchManager!2026` | Authenticated, operational dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | Verify dashboard scope | Observe KPIs | Today's donations, pending lab approvals, stock alerts shown for branch | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `BRANCH_MANAGER` | ☐ Pass ☐ Fail | | |

---

## TC-BM-002: Daily Operations Overview

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View today's collections | Click `Collections → Today` | List of today's blood collections shown | ☐ Pass ☐ Fail | | |
| 2 | View pending test orders | Click `Lab → Pending Orders` | All outstanding test orders shown | ☐ Pass ☐ Fail | | |
| 3 | Check stock alerts | Click `Inventory → Stock Alerts` | Critical and low stock levels highlighted | ☐ Pass ☐ Fail | | |
| 4 | View pending approvals | Click `Approvals → All Pending` | Consolidated approval queue shown | ☐ Pass ☐ Fail | | |

---

## TC-BM-003: Lab Test Result Approval

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Lab Approvals | Click `Lab → Pending Approvals` | List of results awaiting manager review | ☐ Pass ☐ Fail | | |
| 2 | Review result | Click on a pending result | Full test panel results shown (HIV, HBV, HCV, syphilis, ABO/Rh) | ☐ Pass ☐ Fail | | |
| 3 | Verify dual-review rule | Check that result was entered by a different user | Submitted by LAB_TECHNICIAN, not same user | ☐ Pass ☐ Fail | | |
| 4 | Approve result | Click `Verify & Approve` | Result released; unit status changes to `Released`; audit trail updated | ☐ Pass ☐ Fail | | |

---

## TC-BM-004: Blood Unit Issuing Approval

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Issuing Approvals | Click `Clinical → Blood Issues → Pending Approval` | Pending blood issue requests shown | ☐ Pass ☐ Fail | | |
| 2 | View issue request | Click on a pending issue | Issue details shown (patient, blood group, cross-match result, requesting doctor) | ☐ Pass ☐ Fail | | |
| 3 | Approve issue | Click `Approve Issue` | Blood unit issued; inventory decremented; `BloodIssuedEvent` fires | ☐ Pass ☐ Fail | | |

---

## TC-BM-005: Stock Transfer Approval

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfers | Click `Inventory → Stock Transfers` | Transfer requests shown | ☐ Pass ☐ Fail | | |
| 2 | View transfer request | Click on a pending transfer | Transfer details (from/to branch, blood group, units) shown | ☐ Pass ☐ Fail | | |
| 3 | Approve transfer | Click `Approve` | Transfer approved; source inventory decremented; destination incremented | ☐ Pass ☐ Fail | | |
| 4 | View transfer history | Click `Completed` tab | Historical transfers with timestamps shown | ☐ Pass ☐ Fail | | |

---

## TC-BM-006: Donor Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors → All Donors` | Donor list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Search donor | Search by name or blood group | Search works correctly | ☐ Pass ☐ Fail | | |
| 3 | View donor profile | Click on a donor | Full donor profile visible (health records, collection history, deferrals) | ☐ Pass ☐ Fail | | |
| 4 | Register new donor | Click `+ Register Donor`, complete form | Donor registered successfully | ☐ Pass ☐ Fail | | |
| 5 | Add deferral | Navigate to donor, click `+ Add Deferral` | Deferral added with reason and end date | ☐ Pass ☐ Fail | | |

---

## TC-BM-007: Collection Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View collection list | Click `Collections → All Collections` | Collections for branch shown | ☐ Pass ☐ Fail | | |
| 2 | Filter by date | Filter to last 7 days | Only collections in range shown | ☐ Pass ☐ Fail | | |
| 3 | View adverse reactions | Click `Adverse Reactions` tab | Any recorded reactions shown | ☐ Pass ☐ Fail | | |
| 4 | Create adverse reaction report | Click `+ Record Adverse Reaction` on a collection | Reaction form opens and submits | ☐ Pass ☐ Fail | | |

---

## TC-BM-008: Branch Report Generation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reports | Click `Reports → Generate Report` | Report generation form loads | ☐ Pass ☐ Fail | | |
| 2 | Generate donation report | Type=`Weekly Donation Summary`, Period=last 7 days | Report generates with daily breakdown | ☐ Pass ☐ Fail | | |
| 3 | Generate stock report | Type=`Inventory Status`, Branch=current | Report shows current stock levels | ☐ Pass ☐ Fail | | |
| 4 | Export to PDF | Click `Export PDF` on either report | PDF downloaded correctly | ☐ Pass ☐ Fail | | |

---

## TC-BM-009: Hospital Request Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospital Requests | Click `Hospital → Requests` | Hospital blood requests for branch shown | ☐ Pass ☐ Fail | | |
| 2 | View request | Click on pending request | Request details shown | ☐ Pass ☐ Fail | | |
| 3 | Approve request | Click `Approve & Allocate` | Request fulfilled; hospital notified | ☐ Pass ☐ Fail | | |
| 4 | Create hospital request | Click `+ New Request` | Form opens to raise a request on behalf of a hospital | ☐ Pass ☐ Fail | | |

---

## TC-BM-010: Hemovigilance Report

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hemovigilance | Click `Clinical → Hemovigilance Reports` | Reports list loads | ☐ Pass ☐ Fail | | |
| 2 | Create report | Click `+ New Hemovigilance Report` | Form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill report | Patient=`UAT Patient`, Reaction Type=`Febrile`, Severity=`Mild`, Transfusion ID=select existing | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit Report` | Report created; `TransfusionReactionEvent` fires | ☐ Pass ☐ Fail | | |
| 5 | View report | Click on the submitted report | Report detail shows all fields | ☐ Pass ☐ Fail | | |

---

## TC-BM-011: Emergency Blood Issue

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Emergency Issues | Click `Clinical → Blood Issues → Emergency` | Emergency issue form loads | ☐ Pass ☐ Fail | | |
| 2 | Fill emergency issue | Patient=`Emergency UAT`, Blood Group=`O-`, Units=2, Justification=`Trauma surgery` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Issue Emergency Units` | Issue created; stock decremented; audit records emergency flag | ☐ Pass ☐ Fail | | |

---

## TC-BM-012: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on user profile menu | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Click `Notification Preferences` | Preference settings shown | ☐ Pass ☐ Fail | | |
| 3 | Toggle notifications | Disable `Daily Stock Summary`, enable `Critical Stock Alerts` | Preferences saved | ☐ Pass ☐ Fail | | |

---

## TC-BM-013: Blood Camp Overview

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Camps | Click `Operations → Blood Camps` | Camp list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | View camp details | Click on an active camp | Camp info shown (location, date, coordinator, registered donors) | ☐ Pass ☐ Fail | | |
| 3 | View camp collections | Click `Collections` tab | Collections recorded at the camp shown | ☐ Pass ☐ Fail | | |

---

## TC-BM-014: Dispatch Log Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Dispatch | Click `Inventory → Transport Requests` | Transport requests list loads | ☐ Pass ☐ Fail | | |
| 2 | View in-progress transport | Click on an active transport | Cold-chain logs and delivery status shown | ☐ Pass ☐ Fail | | |
| 3 | Approve transport request | Approve a pending request | Transport status updates to `Dispatched` | ☐ Pass ☐ Fail | | |

---

## TC-BM-015: Deviation Approval

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Deviations | Click `Compliance → Deviations` | Deviation list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Review deviation | Click on a submitted deviation | Deviation details shown | ☐ Pass ☐ Fail | | |
| 3 | Approve CAPA | Click `Approve CAPA Plan` | CAPA approved; status updated | ☐ Pass ☐ Fail | | |

---

## TC-BM-016: Read-Only Audit Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance → Audit Logs` | Audit log loads for branch | ☐ Pass ☐ Fail | | |
| 2 | Filter to today | Filter date=today | Today's audit entries shown | ☐ Pass ☐ Fail | | |
| 3 | Confirm read-only | Try to edit or delete an entry | No edit/delete controls visible | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-BM-001: Login and Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-002: Daily Operations Overview | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-003: Lab Test Result Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-004: Blood Unit Issuing Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-005: Stock Transfer Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-006: Donor Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-007: Collection Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-008: Branch Report Generation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-009: Hospital Request Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-010: Hemovigilance Report | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-011: Emergency Blood Issue | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-012: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-013: Blood Camp Overview | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-014: Dispatch Log Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-015: Deviation Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BM-016: Read-Only Audit Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 16
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
