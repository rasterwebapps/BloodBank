# UAT Test Script — SYSTEM_ADMIN

**Role**: SYSTEM_ADMIN
**Scope**: Global (system monitoring, feature flags, scheduled jobs, master data, notification templates)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-system-admin@bloodbank.test`
**Keycloak Realm Role**: `SYSTEM_ADMIN`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-SYA-001: Login and System Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-system-admin@bloodbank.test` / `UatSysAdmin!2026` | Authenticated; system administration dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe system status panels | Service health indicators, job queue status, alert counts shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `SYSTEM_ADMIN` | ☐ Pass ☐ Fail | | |

---

## TC-SYA-002: System Settings Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to System Settings | Click `Administration -> System Settings` | System settings page loads | ☐ Pass ☐ Fail | | |
| 2 | View settings | Review all settings categories | Settings organized by category (General, Security, Notifications, Compliance) | ☐ Pass ☐ Fail | | |
| 3 | Update a setting | Change `Session Timeout` from 30 to 45 minutes | Field updated | ☐ Pass ☐ Fail | | |
| 4 | Save | Click `Save Changes` | Settings saved; audit trail entry created | ☐ Pass ☐ Fail | | |
| 5 | Verify persistence | Refresh page | Updated value (45 min) persisted | ☐ Pass ☐ Fail | | |
| 6 | Revert | Restore to 30 minutes and save | Setting reverted | ☐ Pass ☐ Fail | | |

---

## TC-SYA-003: Feature Flag Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Feature Flags | Click `Administration -> Feature Flags` | Feature flags list loads | ☐ Pass ☐ Fail | | |
| 2 | View flag details | Click on `DONOR_PORTAL_ENABLED` | Flag detail shows: current state, description, last modified | ☐ Pass ☐ Fail | | |
| 3 | Toggle flag | Toggle `DONOR_PORTAL_ENABLED` off | Confirmation dialog appears | ☐ Pass ☐ Fail | | |
| 4 | Confirm toggle | Click `Confirm` | Flag disabled; change logged in audit trail | ☐ Pass ☐ Fail | | |
| 5 | Verify effect | Try to access donor portal as a donor | Donor portal shows maintenance/unavailable message | ☐ Pass ☐ Fail | | |
| 6 | Re-enable | Toggle flag back on | Flag enabled; donor portal accessible again | ☐ Pass ☐ Fail | | |

---

## TC-SYA-004: Scheduled Job Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Scheduled Jobs | Click `Administration -> Scheduled Jobs` | All scheduled jobs listed with cron, status, and last/next run times | ☐ Pass ☐ Fail | | |
| 2 | View job detail | Click on `Expired Units Alert Job` | Job detail: description, cron expression, last 5 executions | ☐ Pass ☐ Fail | | |
| 3 | Trigger manual run | Click `Run Now` | Job executes; status shows `Running` then `Success` | ☐ Pass ☐ Fail | | |
| 4 | View run history | Click `Run History` | Log of past executions with timestamps and outcomes | ☐ Pass ☐ Fail | | |
| 5 | Pause job | Click `Pause` on a job | Job paused; next run shows `Paused` | ☐ Pass ☐ Fail | | |
| 6 | Resume job | Click `Resume` | Job resumed; next scheduled run shown | ☐ Pass ☐ Fail | | |

---

## TC-SYA-005: Master Data Management — Blood Groups

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Blood Groups | Click `Administration -> Master Data -> Blood Groups` | Blood group list loads (A+, A-, B+, B-, AB+, AB-, O+, O-) | ☐ Pass ☐ Fail | | |
| 2 | View blood group | Click on `AB+` | Details shown | ☐ Pass ☐ Fail | | |
| 3 | Add a custom blood group | Click `+ New`, Name=`Rh-null (Golden Blood)` | Form opens | ☐ Pass ☐ Fail | | |
| 4 | Save | Click `Save` | New blood group added | ☐ Pass ☐ Fail | | |

---

## TC-SYA-006: Master Data Management — Component Types

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Component Types | Click `Administration -> Master Data -> Component Types` | Component types listed | ☐ Pass ☐ Fail | | |
| 2 | View component type | Click on `Platelets` | Details shown (storage temp, shelf life, volume range) | ☐ Pass ☐ Fail | | |
| 3 | Add component type | Click `+ New`, Name=`Cryoprecipitate`, StorageTemp=`-18C`, ShelfLife=`365 days` | New type created | ☐ Pass ☐ Fail | | |

---

## TC-SYA-007: Notification Template Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Templates | Click `Notifications -> Templates` | Template list loads | ☐ Pass ☐ Fail | | |
| 2 | View template | Click on `DONATION_COMPLETED` | Template with subject, body, available variables shown | ☐ Pass ☐ Fail | | |
| 3 | Create template | Click `+ New Template`, Name=`SYSTEM_MAINTENANCE`, Channel=`Email` | Template form opens | ☐ Pass ☐ Fail | | |
| 4 | Fill template | Subject=`Scheduled Maintenance`, Body=`The system will be unavailable on {date} from {start_time} to {end_time}` | Variables accepted | ☐ Pass ☐ Fail | | |
| 5 | Save template | Click `Save` | Template created and listed | ☐ Pass ☐ Fail | | |

---

## TC-SYA-008: Branch and Service Health Monitoring

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Service Health | Click `Monitoring -> Service Health` | Health dashboard loads showing all 14 microservices | ☐ Pass ☐ Fail | | |
| 2 | View service detail | Click on `donor-service` | Service metrics shown: uptime, latency, error rate, pod count | ☐ Pass ☐ Fail | | |
| 3 | View all branches | Click `Monitoring -> Branch Status` | All branches shown with connectivity status | ☐ Pass ☐ Fail | | |

---

## TC-SYA-009: Audit Log Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Audit Logs | Click `Compliance -> Audit Logs` | Audit log loads (global scope for SYSTEM_ADMIN) | ☐ Pass ☐ Fail | | |
| 2 | Filter by action | Filter Action = `SYSTEM_SETTINGS_UPDATED` | System settings changes shown | ☐ Pass ☐ Fail | | |
| 3 | Verify immutability | Attempt to edit or delete an audit entry | No edit/delete controls visible | ☐ Pass ☐ Fail | | |

---

## TC-SYA-010: Regulatory Framework Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Regulatory | Click `Compliance -> Regulatory Frameworks` | Framework list loads (HIPAA, GDPR, FDA, AABB, WHO) | ☐ Pass ☐ Fail | | |
| 2 | View framework | Click on `HIPAA` | Framework details shown (requirements, controls, status) | ☐ Pass ☐ Fail | | |
| 3 | Add framework | Click `+ New Framework`, Name=`ISO 15189`, Region=`EU` | New framework created | ☐ Pass ☐ Fail | | |

---

## TC-SYA-011: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt donor registration | Navigate to `Donors -> Register New Donor` | Access denied or not in navigation — SYSTEM_ADMIN does not manage clinical workflows | ☐ Pass ☐ Fail | | |
| 2 | Attempt blood collection | Navigate to `Collections -> + New Collection` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt billing | Navigate to `Billing -> Invoices` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Verify no branch creation | Navigate to `Administration -> Branches -> + New Branch` | Action not available — only SUPER_ADMIN and REGIONAL_ADMIN create branches | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-SYA-001: Login and System Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-002: System Settings Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-003: Feature Flag Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-004: Scheduled Job Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-005: Master Data — Blood Groups | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-006: Master Data — Component Types | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-007: Notification Template Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-008: Service Health Monitoring | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-009: Audit Log Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-010: Regulatory Framework Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-SYA-011: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 11
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
