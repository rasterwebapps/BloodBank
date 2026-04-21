# UAT Test Script — REGIONAL_ADMIN

**Role**: REGIONAL_ADMIN
**Scope**: Region-wide (all branches within assigned region)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-regional-admin@bloodbank.test`
**Keycloak Realm Role**: `REGIONAL_ADMIN`

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

## TC-RA-001: Login and Regional Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-regional-admin@bloodbank.test` / `UatRegionalAdmin!2026` | Authenticated successfully | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe landing screen | Regional dashboard loads — shows only branches in assigned region | ☐ Pass ☐ Fail | | |
| 4 | Verify region scope | Check visible branches | Only branches in `North Region` are visible (not all global branches) | ☐ Pass ☐ Fail | | |
| 5 | Verify role badge | Check top-right menu | Displays `REGIONAL_ADMIN` | ☐ Pass ☐ Fail | | |

---

## TC-RA-002: Multi-Branch Stock Overview

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Inventory | Click `Inventory → Regional Stock` | Stock overview for all regional branches loads | ☐ Pass ☐ Fail | | |
| 2 | View aggregated stock | Review stock by blood group across region | Aggregated totals displayed per blood group | ☐ Pass ☐ Fail | | |
| 3 | Drill to branch | Click on `Branch A` stock row | Branch-level stock detail opens | ☐ Pass ☐ Fail | | |
| 4 | Identify critical stock | Check critical alerts | Branches with critical levels highlighted | ☐ Pass ☐ Fail | | |
| 5 | Attempt out-of-region access | Manually change branch filter to out-of-region branch | Access denied or branch not shown | ☐ Pass ☐ Fail | | |

---

## TC-RA-003: Approve Stock Transfer Between Branches

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Stock Transfers | Click `Inventory → Stock Transfers` | Transfer list loads showing pending transfers in region | ☐ Pass ☐ Fail | | |
| 2 | View transfer request | Click on a pending inter-branch transfer | Transfer details shown (from branch, to branch, units, blood group) | ☐ Pass ☐ Fail | | |
| 3 | Approve transfer | Click `Approve` | Transfer status changes to `Approved`; inventory updates | ☐ Pass ☐ Fail | | |
| 4 | Verify inventory update | Check source and destination branch stock | Source decremented, destination incremented | ☐ Pass ☐ Fail | | |

---

## TC-RA-004: Regional Donation Report

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reports | Click `Reports → Generate Report` | Report form loads | ☐ Pass ☐ Fail | | |
| 2 | Select report type | Choose `Regional Donation Summary` | Region pre-filled; date range selectable | ☐ Pass ☐ Fail | | |
| 3 | Set date range | Select last 30 days | Parameters accepted | ☐ Pass ☐ Fail | | |
| 4 | Generate | Click `Generate` | Report renders with totals per branch | ☐ Pass ☐ Fail | | |
| 5 | Export | Click `Export PDF` | PDF downloads correctly | ☐ Pass ☐ Fail | | |

---

## TC-RA-005: Branch Performance Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Analytics | Click `Analytics → Branch Performance` | Dashboard shows KPIs for all regional branches | ☐ Pass ☐ Fail | | |
| 2 | Compare branches | Review donations, tests, issues metrics side by side | Data shown per branch in comparable format | ☐ Pass ☐ Fail | | |
| 3 | Filter by month | Select a prior month | Data updates for selected month | ☐ Pass ☐ Fail | | |

---

## TC-RA-006: Branch Settings Update

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Branches | Click `Administration → Branches` | Branch list for region loads | ☐ Pass ☐ Fail | | |
| 2 | Edit branch | Click `Edit` on `Branch A` | Edit form opens | ☐ Pass ☐ Fail | | |
| 3 | Update contact info | Change phone number | Field accepts update | ☐ Pass ☐ Fail | | |
| 4 | Save | Click `Save Changes` | Confirmation shown; changes persisted | ☐ Pass ☐ Fail | | |
| 5 | Attempt to create branch | Click `+ New Branch` | Button not visible OR permission denied message | ☐ Pass ☐ Fail | | |

---

## TC-RA-007: Audit Log Access (Regional Scope)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance → Audit Logs` | Audit log loads filtered to regional branches only | ☐ Pass ☐ Fail | | |
| 2 | Filter by branch | Filter by `Branch B` | Only audit entries for Branch B shown | ☐ Pass ☐ Fail | | |
| 3 | Verify read-only | Attempt to delete or edit entry | No edit/delete controls visible | ☐ Pass ☐ Fail | | |
| 4 | Attempt out-of-region query | Change filter to out-of-region branch | No results returned | ☐ Pass ☐ Fail | | |

---

## TC-RA-008: Emergency Request Oversight

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Emergency | Click `Operations → Emergency Requests` | Emergency requests for region loads | ☐ Pass ☐ Fail | | |
| 2 | View active emergencies | Review open requests | Open emergency requests visible with priority | ☐ Pass ☐ Fail | | |
| 3 | Create emergency request | Click `+ Emergency Request` | Form opens | ☐ Pass ☐ Fail | | |
| 4 | Submit request | Fill details and submit | Request created, RabbitMQ event fires | ☐ Pass ☐ Fail | | |

---

## TC-RA-009: Donor Mobilization

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Mobilization | Click `Operations → Donor Mobilization` | Mobilization list loads | ☐ Pass ☐ Fail | | |
| 2 | Create mobilization | Click `+ New Mobilization`, set blood group target=`O-`, target quantity=50 | Form accepts input | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Launch Mobilization` | Mobilization created; notification campaign triggered | ☐ Pass ☐ Fail | | |
| 4 | Track progress | View mobilization status | Response count updates as donors confirm | ☐ Pass ☐ Fail | | |

---

## TC-RA-010: Hospital Contract Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospitals | Click `Hospital → Contracts` | Hospital contract list loads | ☐ Pass ☐ Fail | | |
| 2 | View contracts | Review listed contracts | Contracts for hospitals in region shown | ☐ Pass ☐ Fail | | |
| 3 | Create contract | Click `+ New Contract` | Contract form opens | ☐ Pass ☐ Fail | | |
| 4 | Fill details | Hospital=`UAT General Hospital`, Type=`Monthly`, Rate=standard | Form accepts input | ☐ Pass ☐ Fail | | |
| 5 | Save contract | Click `Save` | Contract created and listed | ☐ Pass ☐ Fail | | |

---

## TC-RA-011: Recall Oversight

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Recalls | Click `Compliance → Recalls` | Recall records for region loads | ☐ Pass ☐ Fail | | |
| 2 | View active recall | Click on active recall | Recall details, affected units, status shown | ☐ Pass ☐ Fail | | |
| 3 | Attempt to create recall | Click `+ New Recall` (if available) | Either form opens OR permission denied per role | ☐ Pass ☐ Fail | | |

---

## TC-RA-012: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Preferences | Click `Profile → Notification Preferences` | Preferences page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Toggle off `Low Stock Alerts`, save | Preference saved | ☐ Pass ☐ Fail | | |
| 3 | Create regional campaign | Click `Notifications → Campaigns → + New Campaign` | Campaign form opens | ☐ Pass ☐ Fail | | |
| 4 | Set target audience | Select Region=`North`, Roles=`DONOR` | Recipients filtered to regional donors | ☐ Pass ☐ Fail | | |
| 5 | Schedule campaign | Set send time = tomorrow 09:00 | Campaign scheduled successfully | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-RA-001: Login and Regional Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-002: Multi-Branch Stock Overview | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-003: Approve Stock Transfer | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-004: Regional Donation Report | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-005: Branch Performance Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-006: Branch Settings Update | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-007: Audit Log Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-008: Emergency Request Oversight | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-009: Donor Mobilization | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-010: Hospital Contract Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-011: Recall Oversight | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-RA-012: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 12
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
