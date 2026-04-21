# UAT Test Script — CAMP_COORDINATOR

**Role**: CAMP_COORDINATOR
**Scope**: Branch-level (blood camp planning, resource management, donor mobilization, collection)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-camp-coordinator@bloodbank.test`
**Keycloak Client Role**: `CAMP_COORDINATOR`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-CAM-001: Login and Camp Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-camp-coordinator@bloodbank.test` / `UatCampCoord!2026` | Authenticated; camp coordination dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View camp overview | Observe dashboard | Upcoming camps, active registrations, collection targets shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `CAMP_COORDINATOR` | ☐ Pass ☐ Fail | | |

---

## TC-CAM-002: Create Blood Camp

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Camps | Click `Operations -> Blood Camps -> + New Camp` | Camp creation form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill camp details | Name=`UAT Community Drive`, Location=`UAT Town Hall`, Date=next Saturday, Start=09:00, End=16:00 | Fields accept input | ☐ Pass ☐ Fail | | |
| 3 | Set target | Donation Target=50, Blood Groups Needed=`O-, A-, B-` | Target set | ☐ Pass ☐ Fail | | |
| 4 | Add coordinator | Coordinator=self | Coordinator assigned | ☐ Pass ☐ Fail | | |
| 5 | Submit | Click `Create Camp` | Camp created with unique ID; status=`Planned` | ☐ Pass ☐ Fail | | |
| 6 | Verify in list | Navigate to camp list | `UAT Community Drive` appears | ☐ Pass ☐ Fail | | |

---

## TC-CAM-003: Resource Planning

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open camp | Navigate to `UAT Community Drive` | Camp detail opens | ☐ Pass ☐ Fail | | |
| 2 | Add resources | Click `Resources -> + Add Resource` | Resource form opens | ☐ Pass ☐ Fail | | |
| 3 | Add phlebotomist | Resource Type=`Staff`, Role=`Phlebotomist`, Count=3 | Resource added | ☐ Pass ☐ Fail | | |
| 4 | Add equipment | Resource Type=`Equipment`, Item=`Donation Bed`, Count=6 | Equipment added | ☐ Pass ☐ Fail | | |
| 5 | Add supplies | Resource Type=`Supplies`, Item=`Collection Bags (450ml)`, Count=60 | Supplies added | ☐ Pass ☐ Fail | | |
| 6 | View resource summary | Check `Resources` tab | All resources listed with planned quantities | ☐ Pass ☐ Fail | | |

---

## TC-CAM-004: Donor Pre-Registration for Camp

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open camp | Navigate to `UAT Community Drive` | Camp detail opens | ☐ Pass ☐ Fail | | |
| 2 | Register existing donor | Click `+ Register Donor`, search `John Smith` | Donor found and registered for camp | ☐ Pass ☐ Fail | | |
| 3 | Register new walk-in | Click `+ Register Donor -> New Donor` | New donor mini-registration form opens | ☐ Pass ☐ Fail | | |
| 4 | Complete registration | Enter minimal required fields and submit | New donor registered and linked to camp | ☐ Pass ☐ Fail | | |
| 5 | View registrations | Click `Registrations` tab | Both donors appear in camp registration list | ☐ Pass ☐ Fail | | |

---

## TC-CAM-005: Camp Collection Recording

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open camp | Navigate to `UAT Community Drive` | Camp detail opens | ☐ Pass ☐ Fail | | |
| 2 | Record collection | Click `+ New Collection`, select `John Smith` from camp registrations | Collection form opens with camp pre-filled | ☐ Pass ☐ Fail | | |
| 3 | Fill collection | Volume=450ml, Type=`Whole Blood`, Bag=`UAT-CAMP-BAG-001` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Complete collection | Click `Complete Collection` | Collection created; linked to camp and donor | ☐ Pass ☐ Fail | | |
| 5 | Update camp progress | Check camp dashboard | Collection count incremented; % of target shown | ☐ Pass ☐ Fail | | |

---

## TC-CAM-006: Update Camp Status

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open camp | Navigate to `UAT Community Drive` | Camp detail opens | ☐ Pass ☐ Fail | | |
| 2 | Activate camp | Click `Activate Camp` (day-of) | Camp status changes to `Active` | ☐ Pass ☐ Fail | | |
| 3 | Complete camp | Click `Complete Camp` | Camp status changes to `Completed`; `CampCompletedEvent` fires | ☐ Pass ☐ Fail | | |
| 4 | View summary | Click `Camp Summary` | Summary shows total donations, donors, adverse reactions, actual vs target | ☐ Pass ☐ Fail | | |

---

## TC-CAM-007: Update Camp Details

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open camp | Navigate to `UAT Community Drive` | Camp detail opens | ☐ Pass ☐ Fail | | |
| 2 | Edit camp | Click `Edit`, change end time to 17:00 | Edit form opens | ☐ Pass ☐ Fail | | |
| 3 | Save changes | Click `Save` | Camp updated with new end time | ☐ Pass ☐ Fail | | |

---

## TC-CAM-008: Donor Mobilization Campaign

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Mobilization | Click `Operations -> Donor Mobilization -> + New` | Mobilization form opens | ☐ Pass ☐ Fail | | |
| 2 | Set targets | Blood Group=`O-`, Donors Needed=20, Linked Camp=`UAT Community Drive` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Launch mobilization | Click `Launch` | Mobilization created; notification campaign triggered to eligible O- donors | ☐ Pass ☐ Fail | | |
| 4 | Track responses | View mobilization status | Donor confirmations count shown | ☐ Pass ☐ Fail | | |

---

## TC-CAM-009: Notification Campaign for Camp

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Campaigns | Click `Notifications -> Campaigns -> + New Campaign` | Campaign form opens | ☐ Pass ☐ Fail | | |
| 2 | Link to camp | Reference camp=`UAT Community Drive` | Camp reference set | ☐ Pass ☐ Fail | | |
| 3 | Set audience | Roles=`DONOR`, Branch/Region=local | Audience set | ☐ Pass ☐ Fail | | |
| 4 | Write message | Subject=`Blood Donation Drive - Saturday`, Body=`Join us...` | Message composed | ☐ Pass ☐ Fail | | |
| 5 | Schedule | Send date=3 days before camp date, Time=09:00 | Campaign scheduled | ☐ Pass ☐ Fail | | |

---

## TC-CAM-010: Read-Only Donor Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors -> All Donors` | Donor list loads (read-only for camp coordinator) | ☐ Pass ☐ Fail | | |
| 2 | View donor profile | Click on a donor | Read-only donor profile shown | ☐ Pass ☐ Fail | | |
| 3 | Verify no edit access | Try to edit donor | No edit controls available for camp coordinator | ☐ Pass ☐ Fail | | |

---

## TC-CAM-011: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt billing access | Navigate to `Billing -> Invoices` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt lab access | Navigate to `Lab -> Test Orders` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt inventory management | Navigate to `Inventory -> Storage Locations` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## TC-CAM-012: Camp Report

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reports | Click `Reports -> Generate Report` | Report form loads | ☐ Pass ☐ Fail | | |
| 2 | Generate camp report | Type=`Camp Summary Report`, Camp=`UAT Community Drive` | Report generates with donors registered, donations collected, reactions | ☐ Pass ☐ Fail | | |
| 3 | Export | Click `Export PDF` | PDF downloaded | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-CAM-001: Login and Camp Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-002: Create Blood Camp | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-003: Resource Planning | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-004: Donor Pre-Registration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-005: Camp Collection Recording | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-006: Update Camp Status | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-007: Update Camp Details | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-008: Donor Mobilization Campaign | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-009: Notification Campaign for Camp | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-010: Read-Only Donor Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-011: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-CAM-012: Camp Report | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 12
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
