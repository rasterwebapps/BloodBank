# UAT Test Script — INVENTORY_MANAGER

**Role**: INVENTORY_MANAGER
**Scope**: Branch-level (blood unit stock, storage, transfers, disposals, transport)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-inventory-manager@bloodbank.test`
**Keycloak Client Role**: `INVENTORY_MANAGER`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-INV-001: Login and Inventory Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-inventory-manager@bloodbank.test` / `UatInventory!2026` | Authenticated; inventory dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View stock summary | Observe dashboard | Blood unit counts by blood group, expiry alerts, and storage status shown | ☐ Pass ☐ Fail | | |
| 4 | Verify branch scope | Check that only branch inventory is visible | No data from other branches | ☐ Pass ☐ Fail | | |

---

## TC-INV-002: View Blood Unit Inventory

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Blood Units | Click `Inventory -> Blood Units` | Blood unit list loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by blood group | Filter = `O-` | Only O-Negative units shown | ☐ Pass ☐ Fail | | |
| 3 | Filter by status | Filter = `Available` | Only available units shown | ☐ Pass ☐ Fail | | |
| 4 | View unit detail | Click on a blood unit | Unit detail shows (unit ID, blood group, collection date, expiry, storage location, test status) | ☐ Pass ☐ Fail | | |
| 5 | Check expiry alert | Filter by expiry in next 7 days | Expiring units highlighted | ☐ Pass ☐ Fail | | |

---

## TC-INV-003: Blood Component Inventory

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Components | Click `Inventory -> Blood Components` | Component list loads | ☐ Pass ☐ Fail | | |
| 2 | View by type | Filter Component Type = `Packed Red Blood Cells` | Only PRBCs shown | ☐ Pass ☐ Fail | | |
| 3 | Update component status | Select a component, click `Edit`, change storage location | Storage location updated | ☐ Pass ☐ Fail | | |
| 4 | View component labels | Click `Print Label` on a component | Label preview shown with component ID, type, expiry | ☐ Pass ☐ Fail | | |

---

## TC-INV-004: Storage Location Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Storage | Click `Inventory -> Storage Locations` | Storage location list loads | ☐ Pass ☐ Fail | | |
| 2 | View location | Click on `Refrigerator-1` | Storage details shown (temperature range, capacity, current occupancy) | ☐ Pass ☐ Fail | | |
| 3 | Add storage location | Click `+ New Location`, enter: Name=`UAT Fridge-5`, Type=`Refrigerator`, Temp=`2-6C`, Capacity=100 | Location created | ☐ Pass ☐ Fail | | |
| 4 | Assign unit to location | Select a blood unit, update storage location to `UAT Fridge-5` | Unit assigned to new location | ☐ Pass ☐ Fail | | |

---

## TC-INV-005: Stock Transfer Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfers | Click `Inventory -> Stock Transfers -> + New` | Transfer request form opens | ☐ Pass ☐ Fail | | |
| 2 | Set transfer details | From=`UAT Central Branch`, To=`UAT Branch B`, Blood Group=`A+`, Units=3 | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Select units | Click `Select Units`, choose 3 available A+ units | Units selected and listed | ☐ Pass ☐ Fail | | |
| 4 | Submit transfer | Click `Submit for Approval` | Transfer request created; status=`Pending Approval` | ☐ Pass ☐ Fail | | |
| 5 | Verify inventory hold | Check that selected units show status `Reserved for Transfer` | Units are reserved and not available for other use | ☐ Pass ☐ Fail | | |

---

## TC-INV-006: Unit Disposal Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Disposals | Click `Inventory -> Unit Disposals -> + New` | Disposal request form opens | ☐ Pass ☐ Fail | | |
| 2 | Select units for disposal | Choose an expired or quarantined unit | Unit selected | ☐ Pass ☐ Fail | | |
| 3 | Set disposal reason | Reason=`Expired`, Disposal Method=`Autoclave and Incinerate` | Reason and method set | ☐ Pass ☐ Fail | | |
| 4 | Submit for approval | Click `Submit for Approval` | Disposal request created; BRANCH_ADMIN/MANAGER notified for dual-review | ☐ Pass ☐ Fail | | |
| 5 | Verify pending status | Check disposal request status | Status=`Pending Approval` | ☐ Pass ☐ Fail | | |

---

## TC-INV-007: Unit Reservation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reservations | Click `Inventory -> Reservations -> + New` | Reservation form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill reservation | Blood Group=`O+`, Units=2, For=`Scheduled Surgery`, Date=tomorrow | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Reserve` | Units reserved; status=`Reserved` | ☐ Pass ☐ Fail | | |
| 4 | Release reservation | Navigate to reservation, click `Release` | Units return to available status | ☐ Pass ☐ Fail | | |

---

## TC-INV-008: Transport Request

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transport | Click `Inventory -> Transport Requests -> + New` | Transport form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill transport details | Destination=`UAT General Hospital`, Units=5 x `O-`, Box Serial=`BOX-UAT-001`, Temp=`2-6C` | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Dispatch` | Transport request created; cold chain monitoring begins | ☐ Pass ☐ Fail | | |
| 4 | Log cold chain | Click `Log Temperature`, enter 4.2°C | Cold chain log entry created | ☐ Pass ☐ Fail | | |
| 5 | Confirm delivery | Click `Confirm Delivery` | Transport completed; delivery confirmation created | ☐ Pass ☐ Fail | | |

---

## TC-INV-009: Expiry Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View expiry alerts | Click `Inventory -> Expiry Alerts` | Units expiring in next 3, 7, 14 days listed | ☐ Pass ☐ Fail | | |
| 2 | Act on expiring unit | Select an expiring unit, initiate disposal request | Disposal request created as per TC-INV-006 | ☐ Pass ☐ Fail | | |
| 3 | Verify event | Ask observer to confirm `UnitExpiringEvent` was triggered | Event confirmed | ☐ Pass ☐ Fail | | |

---

## TC-INV-010: Emergency Request Handling

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Emergency Requests | Click `Operations -> Emergency Requests` | Emergency requests visible | ☐ Pass ☐ Fail | | |
| 2 | Create emergency request | Click `+ New`, Blood Group=`O-`, Units=10, Priority=`Critical` | Request created | ☐ Pass ☐ Fail | | |
| 3 | Fulfill from stock | Click `Fulfill from Stock` | Available O- units reserved for emergency request | ☐ Pass ☐ Fail | | |

---

## TC-INV-011: Stock Alert Configuration

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Alerts | Click `Inventory -> Stock Alert Thresholds` | Alert threshold settings load | ☐ Pass ☐ Fail | | |
| 2 | Update threshold | Set `O-` Critical level = 5 units | Threshold updated | ☐ Pass ☐ Fail | | |
| 3 | Verify alert trigger | Check that current O- stock triggers alert if below 5 | Alert shown on dashboard if threshold breached | ☐ Pass ☐ Fail | | |

---

## TC-INV-012: Blood Components Read Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Components | Click `Inventory -> Blood Components` | Components list loads | ☐ Pass ☐ Fail | | |
| 2 | View component detail | Click on a platelet component | Component detail shown with all fields | ☐ Pass ☐ Fail | | |
| 3 | Attempt to delete component | Look for delete option | No delete option — only admin can delete | ☐ Pass ☐ Fail | | |

---

## TC-INV-013: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt donor registration | Navigate to `Donors -> Register New Donor` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt billing access | Navigate to `Billing -> Invoices` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt test result entry | Navigate to `Lab -> Enter Test Results` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## TC-INV-014: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on user profile | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Enable `Critical Stock Alert` and `Expiry Warning` notifications | Preferences saved | ☐ Pass ☐ Fail | | |

---

## TC-INV-015: Recall Participation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Recalls | Click `Compliance -> Recalls` | Active recall list loads | ☐ Pass ☐ Fail | | |
| 2 | Find affected units | View a recall, check affected units in inventory | Affected units highlighted/quarantined | ☐ Pass ☐ Fail | | |
| 3 | Initiate recall | Click `+ New Recall` | Recall form opens (Inventory Manager can initiate) | ☐ Pass ☐ Fail | | |
| 4 | Fill and submit | Reason=`Storage temperature excursion`, Units=affected units | Recall created; `RecallInitiatedEvent` fires | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-INV-001: Login and Inventory Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-002: View Blood Unit Inventory | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-003: Blood Component Inventory | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-004: Storage Location Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-005: Stock Transfer Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-006: Unit Disposal Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-007: Unit Reservation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-008: Transport Request | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-009: Expiry Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-010: Emergency Request Handling | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-011: Stock Alert Configuration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-012: Blood Components Read Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-013: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-014: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-INV-015: Recall Participation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 15
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
