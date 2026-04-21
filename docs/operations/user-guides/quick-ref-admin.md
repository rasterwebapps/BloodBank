# Quick Reference — Branch Administration

**Roles**: Branch Admin · Branch Manager
**Last Updated**: 2026-04-21
**Portal**: Staff Portal → `https://bloodbank.example.com/staff`

> This guide covers administrative functions. For clinical or inventory tasks, refer to the relevant role guide.

---

## Logging In

1. Go to `https://bloodbank.example.com/staff`
2. Click **Sign in with Keycloak**
3. Enter your **staff email** and **password**
4. If MFA is enabled, enter the 6-digit code from your authenticator app
5. Select your **branch** from the branch selector (top-right)

> ⚠ Branch Admins and Managers see **only their own branch's data**. You cannot access data from other branches.

---

## Branch Admin Tasks

### Create a Staff User Account

**Menu**: Administration → Users → New User

1. Click **+ Add User**
2. Enter: first name, last name, staff email, staff ID
3. Select **role** from the dropdown (see role list below)
4. Click **Create** — the user receives a welcome email with a temporary password
5. User must change their password on first login

### Assign Roles

| Role | What They Can Do |
|---|---|
| RECEPTIONIST | Register donors, schedule appointments |
| PHLEBOTOMIST | Conduct collections, record reactions |
| LAB_TECHNICIAN | Enter test results, manage QC |
| INVENTORY_MANAGER | Manage stock, process components, issue units |
| DOCTOR | Approve donors, review reactions, order crossmatch |
| NURSE | Administer transfusions, record observations |
| BILLING_CLERK | Create invoices, process payments |
| CAMP_COORDINATOR | Manage blood donation camps |
| BRANCH_MANAGER | View all reports, approve key actions |

### Deactivate a Staff User

**Menu**: Administration → Users → [user] → Deactivate

1. Search for the user
2. Click **Deactivate Account**
3. Confirm — the account is disabled immediately in Keycloak
4. Active sessions are terminated within 60 seconds

> Always deactivate accounts immediately when staff leave or change roles.

### Reset a Staff Password

**Menu**: Administration → Users → [user] → Reset Password

1. Click **Send Password Reset Email**
2. The user receives a reset link valid for 24 hours
3. If the user has no email access, click **Set Temporary Password** and communicate it securely

---

## Branch Manager Tasks

### View the Branch Dashboard

**Menu**: Dashboard

The dashboard shows:
- Today's collections count
- Current blood stock by group and component
- Pending requests from hospitals
- Overdue test orders
- Staff on duty today

### Approve an Emergency Blood Issue

**Menu**: Inventory → Blood Requests → Emergency Pending Approval

1. Review the request details
2. Click **Approve** or **Reject with reason**
3. Approved requests are immediately visible to Inventory Manager

### Run a Branch Report

**Menu**: Reports → Branch Reports

Available reports:
- Daily Collections Summary
- Stock Level Report
- Blood Request Fulfilment Rate
- Donor Activity Report
- Adverse Reactions Summary
- Audit Log

1. Select the report type
2. Set the date range
3. Click **Generate** — report renders as a table and can be exported as PDF or CSV

### Review the Audit Log

**Menu**: Reports → Audit Log

- Filter by user, action type, entity type, and date range
- All user actions in the system are recorded immutably

### Manage Branch Operating Hours

**Menu**: Administration → Branch Settings → Operating Hours

1. Set regular hours per day of the week
2. Add exceptions (public holidays, etc.)
3. Click **Save** — the appointment booking system uses these hours automatically

---

## Camp Coordinator Tasks (if applicable)

### Create a Blood Donation Camp

**Menu**: Camps → New Camp

1. Enter camp name, location, date, and target collection count
2. Assign staff members to the camp
3. Click **Save** — donors can be linked to this camp during registration

---

## Key Metrics to Monitor Daily

| Metric | Target | Action if Below |
|---|---|---|
| Collections today | ≥ branch daily target | Contact Receptionist to boost scheduling |
| O-negative stock | ≥ 5 units | Notify Regional Admin, request transfer |
| Pending test orders > 4 h | 0 | Contact Lab Technician |
| Hospital requests pending > 2 h | 0 | Contact Inventory Manager |

---

## Getting Help

| Issue | Action |
|---|---|
| Cannot create a user | Check you have BRANCH_ADMIN role |
| Report not generating | Try a shorter date range; contact support if persists |
| System login issues for staff | Use Administration → Users → Reset Password |
| System down | Call on-call engineer: see support contact sheet |
