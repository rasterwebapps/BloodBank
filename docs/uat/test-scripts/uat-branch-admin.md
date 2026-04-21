# UAT Test Script — BRANCH_ADMIN

**Role**: BRANCH_ADMIN
**Scope**: Branch-level (single branch, all operations)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-branch-admin@bloodbank.test`
**Keycloak Client Role**: `BRANCH_ADMIN`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## Instructions

1. Log in with the test account credentials from [`keycloak-test-users.md`](../keycloak-test-users.md)
2. Execute each step in order
3. Record Pass / Fail / Partial / Skip / Blocked in the result column
4. For any Fail, record a defect ID (format: `DEF-YYYYMMDD-NNN`) in the Notes column

---

## TC-BA-001: Login and Branch Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login page appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-branch-admin@bloodbank.test` / `UatBranchAdmin!2026` | Authenticated | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe landing screen | Branch dashboard shows current branch KPIs only | ☐ Pass ☐ Fail | | |
| 4 | Verify branch scope | Check that data is filtered to `UAT Central Branch` | No data from other branches visible | ☐ Pass ☐ Fail | | |

---

## TC-BA-002: Branch Configuration

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Branch Settings | Click `Administration → Branch Settings` | Branch settings page opens | ☐ Pass ☐ Fail | | |
| 2 | Update operating hours | Change Saturday hours to 09:00–13:00 | Hours updated | ☐ Pass ☐ Fail | | |
| 3 | Add equipment | Click `Equipment → + Add`, enter `Apheresis Machine SN-001` | Equipment record created | ☐ Pass ☐ Fail | | |
| 4 | Save settings | Click `Save` | Settings saved, confirmation shown | ☐ Pass ☐ Fail | | |

---

## TC-BA-003: Staff Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Staff | Click `Administration → Staff` | Staff list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Search staff | Search by name `UAT Phlebotomist` | Staff record found | ☐ Pass ☐ Fail | | |
| 3 | View staff roles | Click on staff member | Roles assigned shown | ☐ Pass ☐ Fail | | |
| 4 | Deactivate staff | Toggle status to `Inactive` | Staff deactivated; login will be blocked | ☐ Pass ☐ Fail | | |
| 5 | Reactivate staff | Toggle back to `Active` | Staff reactivated | ☐ Pass ☐ Fail | | |

---

## TC-BA-004: Donor Registration

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors → Register New Donor` | Donor registration form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill donor details | Enter: FirstName=`UAT`, LastName=`Donor Test`, DOB=`1985-06-15`, BloodGroup=`A+`, Email=`uat.donor.test@example.com`, Phone=`+1-555-0200` | All fields accept input | ☐ Pass ☐ Fail | | |
| 3 | Complete health questionnaire | Answer all pre-donation health questions | Questionnaire captured | ☐ Pass ☐ Fail | | |
| 4 | Submit registration | Click `Register Donor` | Donor created with unique donor ID | ☐ Pass ☐ Fail | | |
| 5 | Verify donor record | Search for `UAT Donor Test` | Donor found with branch = `UAT Central Branch` | ☐ Pass ☐ Fail | | |

---

## TC-BA-005: Deferral Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to donor created in TC-BA-004 | Search for `UAT Donor Test` | Donor profile opens | ☐ Pass ☐ Fail | | |
| 2 | Add deferral | Click `Deferrals → + Add Deferral` | Deferral form opens | ☐ Pass ☐ Fail | | |
| 3 | Set deferral reason | Select `Recent Surgery`, set start date=today, end date=+84 days | Deferral form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Save deferral | Click `Save` | Deferral created; donor status shows `Deferred` | ☐ Pass ☐ Fail | | |
| 5 | Verify deferral in list | Click `Deferrals` tab on donor profile | Deferral entry listed with dates | ☐ Pass ☐ Fail | | |

---

## TC-BA-006: Blood Collection Oversight

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Collections | Click `Collections → Today's Collections` | Today's collections for branch shown | ☐ Pass ☐ Fail | | |
| 2 | View collection details | Click on any collection | Collection detail opens (donor, unit, vitals, volume) | ☐ Pass ☐ Fail | | |
| 3 | Approve collection | If collection is pending approval, click `Approve` | Collection approved; status updates | ☐ Pass ☐ Fail | | |
| 4 | View collection history | Filter by last 7 days | All collections for period shown | ☐ Pass ☐ Fail | | |

---

## TC-BA-007: Lab Test Results Approval (Dual-Review)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Lab | Click `Lab → Pending Approvals` | Lab results awaiting branch admin approval shown | ☐ Pass ☐ Fail | | |
| 2 | View result | Click on a pending test result | Result detail shows all test values | ☐ Pass ☐ Fail | | |
| 3 | Approve result | Click `Verify & Approve` | Result released; blood unit status updated; RabbitMQ event fires | ☐ Pass ☐ Fail | | |
| 4 | Reject result | On another result, click `Reject` with reason | Result rejected; lab technician notified | ☐ Pass ☐ Fail | | |

---

## TC-BA-008: Blood Unit Disposal (Dual-Review)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Inventory | Click `Inventory → Disposal Requests` | Pending disposal requests shown | ☐ Pass ☐ Fail | | |
| 2 | View request | Click on disposal request | Disposal details shown (reason, units, requestor) | ☐ Pass ☐ Fail | | |
| 3 | Approve disposal | Click `Approve Disposal` | Disposal approved; units marked as disposed; audit trail updated | ☐ Pass ☐ Fail | | |

---

## TC-BA-009: Invoice Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Billing | Click `Billing → Invoices` | Invoice list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | View invoice | Click on an invoice | Invoice details with line items shown | ☐ Pass ☐ Fail | | |
| 3 | Approve invoice | If pending approval, click `Approve` | Invoice status changes to `Approved` | ☐ Pass ☐ Fail | | |
| 4 | Generate billing report | Click `Reports → Monthly Billing Summary` | Report generates with correct totals | ☐ Pass ☐ Fail | | |

---

## TC-BA-010: Stock Transfer Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfers | Click `Inventory → Stock Transfers` | Transfer list loads | ☐ Pass ☐ Fail | | |
| 2 | Initiate transfer | Click `+ New Transfer` | Transfer form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill transfer | To Branch=`Branch B`, Blood Group=`O-`, Units=5 | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit transfer | Click `Submit` | Transfer created with status `Pending Approval` | ☐ Pass ☐ Fail | | |
| 5 | Approve own transfer | Approve as Branch Admin | Transfer approved; inventory updated | ☐ Pass ☐ Fail | | |

---

## TC-BA-011: SOP Document Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to SOPs | Click `Compliance → SOP Documents` | SOP list loads | ☐ Pass ☐ Fail | | |
| 2 | View a SOP | Click on `Blood Collection SOP v2.0` | Document opens/downloads | ☐ Pass ☐ Fail | | |
| 3 | Search SOP | Search for `transfusion` | Relevant SOPs returned | ☐ Pass ☐ Fail | | |

---

## TC-BA-012: Deviation Reporting

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Deviations | Click `Compliance → Deviations` | Deviation list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Report deviation | Click `+ New Deviation` | Form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill deviation | Type=`Process`, Description=`Centrifuge temperature exceeded 4°C by 0.5°C during spin`, Severity=`Minor` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit` | Deviation created with ID; CAPA process initiated | ☐ Pass ☐ Fail | | |

---

## TC-BA-013: Hospital Request Approval

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospital Requests | Click `Hospital → Requests` | Hospital blood requests for branch shown | ☐ Pass ☐ Fail | | |
| 2 | View request | Click on a pending request | Request details shown (hospital, blood group, units, urgency) | ☐ Pass ☐ Fail | | |
| 3 | Approve request | Click `Approve & Allocate` | Request approved; allocation begins | ☐ Pass ☐ Fail | | |

---

## TC-BA-014: Recall Initiation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Recalls | Click `Compliance → Recalls` | Recall list loads | ☐ Pass ☐ Fail | | |
| 2 | Initiate recall | Click `+ New Recall` | Recall form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill recall | Reason=`Test Panel Failure — HIV Reactive`, Unit IDs=`[UAT-UNIT-001]` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Initiate Recall` | Recall initiated; `RecallInitiatedEvent` fires; affected units quarantined | ☐ Pass ☐ Fail | | |
| 5 | Track recall | View recall status | All affected units tracked | ☐ Pass ☐ Fail | | |

---

## TC-BA-015: Hemovigilance Report Review

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hemovigilance | Click `Clinical → Hemovigilance Reports` | Report list loads | ☐ Pass ☐ Fail | | |
| 2 | View report | Click on a hemovigilance report | Full report including reaction details shown | ☐ Pass ☐ Fail | | |
| 3 | Generate report | Click `Generate Hemovigilance Summary` | Report generates for branch | ☐ Pass ☐ Fail | | |

---

## TC-BA-016: Emergency Issue Override

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Blood Issues | Click `Clinical → Blood Issues` | Issue list loads | ☐ Pass ☐ Fail | | |
| 2 | Create emergency issue | Click `+ Emergency Issue (O-neg)` | Emergency issue form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill details | Patient Name=`UAT Emergency Patient`, Units=2, Reason=`Trauma` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Issue Now` | Emergency issue created; audit trail records `EMERGENCY_ISSUE`; stock decremented | ☐ Pass ☐ Fail | | |

---

## TC-BA-017: Document Upload

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Documents | Click `Documents → Branch Documents` | Document list loads | ☐ Pass ☐ Fail | | |
| 2 | Upload document | Click `+ Upload`, select a PDF, category=`License` | Upload dialog opens | ☐ Pass ☐ Fail | | |
| 3 | Complete upload | Click `Upload` | Document uploaded; version=1; stored in MinIO | ☐ Pass ☐ Fail | | |
| 4 | Delete document | Click `Delete` on the uploaded document | Document deleted with confirmation | ☐ Pass ☐ Fail | | |

---

## TC-BA-018: Audit Trail Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance → Audit Logs` | Audit log loads filtered to branch | ☐ Pass ☐ Fail | | |
| 2 | Filter by action | Filter Action = `DISPOSAL_APPROVED` | Disposal approvals from TC-BA-008 shown | ☐ Pass ☐ Fail | | |
| 3 | Verify entries | Check that all prior actions in this session are logged | Audit entries found for all TC-BA actions | ☐ Pass ☐ Fail | | |

---

## TC-BA-019: Notification Campaign

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Campaigns | Click `Notifications → Campaigns → + New Campaign` | Campaign form opens | ☐ Pass ☐ Fail | | |
| 2 | Set target | Audience=`Donors`, Blood Group=`B+`, Branch=`UAT Central Branch` | Recipient count shown | ☐ Pass ☐ Fail | | |
| 3 | Write message | Subject=`Urgent B+ Donation Needed`, Body=`Please donate...` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Send campaign | Click `Send Now` | Campaign sent; delivery count shown | ☐ Pass ☐ Fail | | |

---

## TC-BA-020: Data Export (GDPR Portability)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to donor | Open donor profile for `UAT Donor Test` | Donor profile shows | ☐ Pass ☐ Fail | | |
| 2 | Export donor data | Click `Actions → Export Data (GDPR)` | Data export dialog opens | ☐ Pass ☐ Fail | | |
| 3 | Download export | Click `Download` | JSON/CSV export downloads with all donor data | ☐ Pass ☐ Fail | | |
| 4 | Verify completeness | Review exported file | All PII, donation history, deferrals included | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-BA-001: Login and Branch Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-002: Branch Configuration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-003: Staff Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-004: Donor Registration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-005: Deferral Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-006: Blood Collection Oversight | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-007: Lab Test Results Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-008: Blood Unit Disposal | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-009: Invoice Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-010: Stock Transfer Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-011: SOP Document Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-012: Deviation Reporting | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-013: Hospital Request Approval | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-014: Recall Initiation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-015: Hemovigilance Report Review | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-016: Emergency Issue Override | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-017: Document Upload | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-018: Audit Trail Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-019: Notification Campaign | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BA-020: Data Export (GDPR Portability) | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 20
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
