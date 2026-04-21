# UAT Test Script — SUPER_ADMIN

**Role**: SUPER_ADMIN
**Scope**: Global (all branches, all regions)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-super-admin@bloodbank.test`
**Keycloak Realm Role**: `SUPER_ADMIN`

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
4. For any Fail, record a defect ID (format: `DEF-YYYYMMDD-NNN`) and describe the issue in the Notes column
5. Do not proceed past a P1 defect — escalate to the UAT Lead immediately

---

## TC-SA-001: Login and Dashboard Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to `https://uat.bloodbank.internal` | Open browser and enter URL | Login/SSO redirect page appears | ☐ Pass ☐ Fail | | |
| 2 | Enter credentials | Use `uat-super-admin@bloodbank.test` / `UatSuperAdmin!2026` | Redirect to Keycloak login | ☐ Pass ☐ Fail | | |
| 3 | Complete MFA | Enter TOTP code from authenticator app | Successfully authenticated | ☐ Pass ☐ Fail | | |
| 4 | View dashboard | Observe landing screen | Global dashboard loads with system-wide stats (all branches, total donations, stock levels) | ☐ Pass ☐ Fail | | |
| 5 | Verify role badge | Check displayed role in top-right menu | Displays `SUPER_ADMIN` | ☐ Pass ☐ Fail | | |

---

## TC-SA-002: Branch Creation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Branch Management | Click `Administration → Branches` | Branch list page loads showing all branches | ☐ Pass ☐ Fail | | |
| 2 | Initiate new branch | Click `+ New Branch` | Branch creation form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill branch details | Enter: Name=`UAT Test Branch`, Region=`North`, City=`Test City`, Phone=`+1-555-0100` | All fields accept input | ☐ Pass ☐ Fail | | |
| 4 | Set operating hours | Click `Operating Hours` tab, set Mon–Fri 08:00–17:00 | Hours saved correctly | ☐ Pass ☐ Fail | | |
| 5 | Submit branch | Click `Create Branch` | Branch created, appears in branch list | ☐ Pass ☐ Fail | | |
| 6 | Verify branch in list | Search for `UAT Test Branch` | Branch appears with status `Active` | ☐ Pass ☐ Fail | | |

---

## TC-SA-003: Global System Settings

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to System Settings | Click `Administration → System Settings` | System settings page loads | ☐ Pass ☐ Fail | | |
| 2 | View current settings | Review displayed settings | Settings display correctly (max deferral days, mandatory test panels, etc.) | ☐ Pass ☐ Fail | | |
| 3 | Update a setting | Change `Default Deferral Period` from 84 to 90 days | Field accepts numeric input | ☐ Pass ☐ Fail | | |
| 4 | Save settings | Click `Save Changes` | Confirmation message: "Settings updated successfully" | ☐ Pass ☐ Fail | | |
| 5 | Verify persistence | Refresh page, re-check setting | Updated value (90 days) is persisted | ☐ Pass ☐ Fail | | |
| 6 | Revert change | Restore to 84 days and save | Setting reverts correctly | ☐ Pass ☐ Fail | | |

---

## TC-SA-004: Feature Flag Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Feature Flags | Click `Administration → Feature Flags` | Feature flags list loads | ☐ Pass ☐ Fail | | |
| 2 | Toggle a feature flag | Toggle `DONOR_PORTAL_ENABLED` off | Toggle switches to off, confirmation dialog appears | ☐ Pass ☐ Fail | | |
| 3 | Confirm toggle | Click `Confirm` | Feature flag disabled, change logged in audit trail | ☐ Pass ☐ Fail | | |
| 4 | Re-enable flag | Toggle `DONOR_PORTAL_ENABLED` back on | Flag enabled, confirmation shown | ☐ Pass ☐ Fail | | |

---

## TC-SA-005: Scheduled Job Monitoring

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Scheduled Jobs | Click `Administration → Scheduled Jobs` | List of all scheduled jobs shown | ☐ Pass ☐ Fail | | |
| 2 | View job details | Click on `Expired Units Disposal Job` | Job details panel opens (cron, last run, next run, status) | ☐ Pass ☐ Fail | | |
| 3 | View job history | Click `Run History` tab | Last 10 executions shown with timestamps and status | ☐ Pass ☐ Fail | | |
| 4 | Trigger manual run | Click `Run Now` on `Stock Level Alert Job` | Job executes, confirmation shown, run appears in history | ☐ Pass ☐ Fail | | |

---

## TC-SA-006: Global Donor Search

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors → All Donors` | Donor list loads (all branches visible) | ☐ Pass ☐ Fail | | |
| 2 | Search by blood group | Filter by `O-` | Results show only O-Negative donors | ☐ Pass ☐ Fail | | |
| 3 | Cross-branch search | Filter by Region=`All` | Donors from all branches are returned | ☐ Pass ☐ Fail | | |
| 4 | View donor details | Click on any donor record | Donor detail page loads with full PII visible (SUPER_ADMIN has full access) | ☐ Pass ☐ Fail | | |
| 5 | Verify PII visibility | Check name, email, phone, national ID | All PII fields visible and unmasked | ☐ Pass ☐ Fail | | |

---

## TC-SA-007: Global Stock Overview

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Inventory | Click `Inventory → Global Stock` | Global stock dashboard loads | ☐ Pass ☐ Fail | | |
| 2 | View by blood group | Review stock level chart | All 8 blood groups shown with current unit counts | ☐ Pass ☐ Fail | | |
| 3 | Filter by branch | Select a specific branch | Stock filtered to selected branch | ☐ Pass ☐ Fail | | |
| 4 | Check critical alerts | View `Critical Stock Levels` section | Branches with critical stock highlighted in red | ☐ Pass ☐ Fail | | |
| 5 | Export report | Click `Export → CSV` | CSV download starts with correct data | ☐ Pass ☐ Fail | | |

---

## TC-SA-008: Global Reports

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reports | Click `Reports → Generate Report` | Report generation form loads | ☐ Pass ☐ Fail | | |
| 2 | Select report type | Choose `Monthly Donation Summary` | Report parameters form shows date range and branch selectors | ☐ Pass ☐ Fail | | |
| 3 | Set parameters | Select date range = last 30 days, Branch = All | Parameters set correctly | ☐ Pass ☐ Fail | | |
| 4 | Generate report | Click `Generate` | Report generates and downloads/displays | ☐ Pass ☐ Fail | | |
| 5 | Verify data | Review report content | Data matches system records for the period | ☐ Pass ☐ Fail | | |

---

## TC-SA-009: Audit Log Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance → Audit Logs` | Audit log viewer loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by user | Filter by `uat-super-admin@bloodbank.test` | Only actions by this user shown | ☐ Pass ☐ Fail | | |
| 3 | Filter by action type | Filter Action = `SYSTEM_SETTINGS_UPDATED` | Relevant audit entries shown (from TC-SA-003) | ☐ Pass ☐ Fail | | |
| 4 | Verify immutability | Attempt to edit or delete an audit entry | No edit/delete controls visible; entries are read-only | ☐ Pass ☐ Fail | | |

---

## TC-SA-010: User Management (Keycloak Integration)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to User Management | Click `Administration → Users` | User management page loads | ☐ Pass ☐ Fail | | |
| 2 | Search for user | Search `uat-branch-admin@bloodbank.test` | User record found | ☐ Pass ☐ Fail | | |
| 3 | View user roles | Click on user, view Roles tab | User's assigned roles displayed correctly | ☐ Pass ☐ Fail | | |
| 4 | Deactivate user | Toggle user status to `Inactive` | User deactivated, confirmation shown | ☐ Pass ☐ Fail | | |
| 5 | Reactivate user | Toggle user back to `Active` | User reactivated | ☐ Pass ☐ Fail | | |

---

## TC-SA-011: Recall Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Compliance | Click `Compliance → Recalls` | Recall records list loads | ☐ Pass ☐ Fail | | |
| 2 | Initiate a recall | Click `+ New Recall` | Recall creation form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill recall details | Enter: Reason=`Contamination Alert`, Blood Unit IDs, Severity=`Critical` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit recall | Click `Initiate Recall` | Recall initiated; RabbitMQ event fires; notifications sent | ☐ Pass ☐ Fail | | |
| 5 | Track recall | View recall status | Recall shows status `In Progress` with affected units listed | ☐ Pass ☐ Fail | | |

---

## TC-SA-012: Emergency Broadcast

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Notifications | Click `Notifications → Campaigns` | Campaign management loads | ☐ Pass ☐ Fail | | |
| 2 | Create emergency campaign | Click `+ Emergency Broadcast` | Emergency broadcast form opens | ☐ Pass ☐ Fail | | |
| 3 | Set recipients | Select All Donors, Blood Group=`O-` | Recipients filtered correctly | ☐ Pass ☐ Fail | | |
| 4 | Write message | Enter subject and body | Message composed | ☐ Pass ☐ Fail | | |
| 5 | Send broadcast | Click `Send Now` | Broadcast dispatched; notification count shown | ☐ Pass ☐ Fail | | |

---

## TC-SA-013: Multi-Region Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View region summary | Navigate to `Dashboard → Regional View` | Map/chart showing all regions with KPIs | ☐ Pass ☐ Fail | | |
| 2 | Drill into a region | Click on `North Region` | Region detail shows all branches in that region | ☐ Pass ☐ Fail | | |
| 3 | Compare regions | View side-by-side KPI comparison | Donation counts, stock levels, active camps shown per region | ☐ Pass ☐ Fail | | |

---

## TC-SA-014: Disaster Event Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Emergency | Click `Operations → Disaster Events` | Disaster event page loads | ☐ Pass ☐ Fail | | |
| 2 | Create disaster event | Click `+ New Disaster Event`, enter: Name=`UAT Flood Test`, Date=today, Affected Region=`South` | Form submits successfully | ☐ Pass ☐ Fail | | |
| 3 | View emergency requests | Navigate to related emergency requests | Requests linked to disaster event visible | ☐ Pass ☐ Fail | | |
| 4 | Resolve event | Set event status to `Resolved` | Event marked resolved, no further emergency requests accepted | ☐ Pass ☐ Fail | | |

---

## TC-SA-015: Notification Template Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Templates | Click `Notifications → Templates` | Template list loads | ☐ Pass ☐ Fail | | |
| 2 | View existing template | Click `DONATION_REMINDER` template | Template content, variables, and channels shown | ☐ Pass ☐ Fail | | |
| 3 | Edit template | Modify body text, save | Template updated with confirmation | ☐ Pass ☐ Fail | | |
| 4 | Create new template | Click `+ New Template`, fill details | New template created and appears in list | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-SA-001: Login and Dashboard Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-002: Branch Creation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-003: Global System Settings | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-004: Feature Flag Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-005: Scheduled Job Monitoring | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-006: Global Donor Search | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-007: Global Stock Overview | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-008: Global Reports | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-009: Audit Log Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-010: User Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-011: Recall Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-012: Emergency Broadcast | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-013: Multi-Region Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-014: Disaster Event Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SA-015: Notification Template Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 15
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
