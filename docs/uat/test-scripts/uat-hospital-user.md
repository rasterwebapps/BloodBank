# UAT Test Script — HOSPITAL_USER

**Role**: HOSPITAL_USER
**Scope**: Hospital portal (blood requests, order tracking, feedback)
**UAT Environment**: `https://uat.bloodbank.internal/hospital`
**Test Account**: `uat-hospital-user@bloodbank.test`
**Keycloak Client Role**: `HOSPITAL_USER`
**Test Hospital**: `UAT General Hospital`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-HOS-001: Login and Hospital Portal Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospital Portal | Open `https://uat.bloodbank.internal/hospital` | Hospital portal login appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-hospital-user@bloodbank.test` / `UatHospitalUser!2026` | Authenticated; hospital portal dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe portal | Open requests, request history, and stock availability indicator shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `HOSPITAL_USER` | ☐ Pass ☐ Fail | | |
| 5 | Verify hospital scope | Check that only `UAT General Hospital` data is visible | No other hospital's data accessible | ☐ Pass ☐ Fail | | |

---

## TC-HOS-002: Submit Routine Blood Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Requests | Click `Requests -> + New Request` | Blood request form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill request details | Blood Group=`A+`, Component=`Packed Red Blood Cells`, Units=3, Patient=`Hospital Patient UAT-001`, Indication=`Elective Surgery`, Urgency=`Routine`, Required By=tomorrow | Form accepts all input | ☐ Pass ☐ Fail | | |
| 3 | Submit request | Click `Submit Request` | Request created; `BloodRequestCreatedEvent` fires; blood bank notified | ☐ Pass ☐ Fail | | |
| 4 | View confirmation | Check request confirmation | Request ID shown; status=`Pending` | ☐ Pass ☐ Fail | | |

---

## TC-HOS-003: Submit Urgent Blood Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Create urgent request | Click `Requests -> + New Request` | Form opens | ☐ Pass ☐ Fail | | |
| 2 | Set urgency | Blood Group=`O-`, Units=2, Urgency=`Urgent`, Required By=in 4 hours | Urgency flag visible | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit Request` | Urgent request created; priority flag set; blood bank notified immediately | ☐ Pass ☐ Fail | | |
| 4 | Track status | View request in list | Request shows `Urgent` badge and current status | ☐ Pass ☐ Fail | | |

---

## TC-HOS-004: Submit Emergency Blood Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Create emergency request | Click `Requests -> + Emergency Request` | Emergency request form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill request | Blood Group=`O-`, Units=4, Justification=`Mass casualty event`, Urgency=`Emergency` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit Emergency Request` | `EmergencyRequestEvent` fires; on-call blood bank staff alerted | ☐ Pass ☐ Fail | | |

---

## TC-HOS-005: Track Request Status

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Request List | Click `Requests -> My Requests` | All hospital requests listed | ☐ Pass ☐ Fail | | |
| 2 | Filter by status | Filter = `In Progress` | Only in-progress requests shown | ☐ Pass ☐ Fail | | |
| 3 | View request detail | Click on a request | Status timeline shown (Pending -> Processing -> Ready -> Dispatched -> Delivered) | ☐ Pass ☐ Fail | | |
| 4 | View matched units | Click `Matched Units` tab | Compatible units allocated to request shown | ☐ Pass ☐ Fail | | |

---

## TC-HOS-006: View Delivery Status

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Find a dispatched request | Filter requests by status=`Dispatched` | Dispatched request found | ☐ Pass ☐ Fail | | |
| 2 | View transport info | Click on request, click `Transport Details` | Transport box info, cold chain status, estimated arrival shown | ☐ Pass ☐ Fail | | |
| 3 | Confirm receipt | Click `Confirm Receipt` | Delivery confirmed; blood bank notified; request status=`Delivered` | ☐ Pass ☐ Fail | | |

---

## TC-HOS-007: Submit Cross-Match Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Cross-Match | Click `Requests -> Cross-Match -> + New` | Cross-match request form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill request | Patient=`UAT Patient B`, Blood Group=`B-`, Sample Tube=`SAMPLE-UAT-001`, Urgency=`Routine` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit` | Cross-match request sent to blood bank lab | ☐ Pass ☐ Fail | | |
| 4 | Track status | View request | Status=`Pending Lab`; notification will be sent when complete | ☐ Pass ☐ Fail | | |

---

## TC-HOS-008: Submit Hospital Feedback

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Feedback | Click `Feedback -> + New Feedback` | Feedback form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill feedback | Type=`Service Quality`, Rating=4/5, Comments=`Good response time, unit quality excellent` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Submit Feedback` | Feedback submitted; blood bank notified | ☐ Pass ☐ Fail | | |

---

## TC-HOS-009: View Hospital Contract and Rate

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Contract | Click `Account -> My Contract` | Hospital contract details shown (type, effective date, applicable rates) | ☐ Pass ☐ Fail | | |
| 2 | View invoice history | Click `Account -> Invoices` | Read-only invoice list for the hospital shown | ☐ Pass ☐ Fail | | |
| 3 | Download invoice | Click `Download` on an invoice | Invoice PDF downloaded | ☐ Pass ☐ Fail | | |

---

## TC-HOS-010: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt to access other hospital data | Manually change hospital ID in URL | System returns 403 Forbidden or redirects to own hospital | ☐ Pass ☐ Fail | | |
| 2 | Attempt lab result access | Navigate to `Lab -> Test Results` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt donor management | Navigate to `Donors` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Attempt to view blood bank inventory | Navigate to `Inventory -> Blood Units` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-HOS-001: Login and Hospital Portal Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-002: Submit Routine Blood Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-003: Submit Urgent Blood Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-004: Submit Emergency Blood Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-005: Track Request Status | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-006: View Delivery Status | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-007: Submit Cross-Match Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-008: Submit Hospital Feedback | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-009: View Hospital Contract and Rate | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-HOS-010: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 10
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
