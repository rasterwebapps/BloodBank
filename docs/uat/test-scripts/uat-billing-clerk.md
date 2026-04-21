# UAT Test Script — BILLING_CLERK

**Role**: BILLING_CLERK
**Scope**: Branch-level (rate master, invoices, payments, credit notes, billing reports)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-billing-clerk@bloodbank.test`
**Keycloak Client Role**: `BILLING_CLERK`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-BIL-001: Login and Billing Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-billing-clerk@bloodbank.test` / `UatBillingClerk!2026` | Authenticated; billing dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View dashboard | Observe billing dashboard | Outstanding invoices, payments due, recent activity shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `BILLING_CLERK` | ☐ Pass ☐ Fail | | |

---

## TC-BIL-002: Rate Master Management

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Rate Master | Click `Billing -> Rate Master` | Rate master list loads | ☐ Pass ☐ Fail | | |
| 2 | View existing rates | Review listed rates | Component rates, processing fees, and service charges shown | ☐ Pass ☐ Fail | | |
| 3 | Add new rate | Click `+ New Rate`, Component=`Whole Blood`, Rate=$120.00, Effective=today | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Save rate | Click `Save` | New rate created and listed | ☐ Pass ☐ Fail | | |
| 5 | Update existing rate | Click `Edit` on an existing rate, change amount | Updated rate saved | ☐ Pass ☐ Fail | | |

---

## TC-BIL-003: Create Invoice

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Invoices | Click `Billing -> Invoices -> + New Invoice` | Invoice creation form opens | ☐ Pass ☐ Fail | | |
| 2 | Select hospital | Hospital=`UAT General Hospital` | Hospital selected; contract rates auto-loaded | ☐ Pass ☐ Fail | | |
| 3 | Add line items | Click `+ Add Line Item`, select `Packed Red Blood Cells x 2` | Line items added with auto-calculated amounts | ☐ Pass ☐ Fail | | |
| 4 | Add service charge | Add `Processing Fee` = $25.00 | Service charge line item added | ☐ Pass ☐ Fail | | |
| 5 | Review totals | Check subtotal, tax, and total | Totals calculated correctly | ☐ Pass ☐ Fail | | |
| 6 | Submit invoice | Click `Generate Invoice` | Invoice created with unique invoice number; status=`Pending Approval` | ☐ Pass ☐ Fail | | |

---

## TC-BIL-004: View and Manage Invoices

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Invoice List | Click `Billing -> Invoices` | Invoice list loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by status | Filter = `Pending Payment` | Only unpaid invoices shown | ☐ Pass ☐ Fail | | |
| 3 | View invoice | Click on invoice from TC-BIL-003 | Invoice detail shows all line items and totals | ☐ Pass ☐ Fail | | |
| 4 | Download invoice PDF | Click `Download PDF` | Invoice PDF downloaded correctly | ☐ Pass ☐ Fail | | |
| 5 | Email invoice | Click `Send to Hospital` | Invoice emailed; notification triggered | ☐ Pass ☐ Fail | | |

---

## TC-BIL-005: Record Payment

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Payments | Click `Billing -> Payments -> + New Payment` | Payment form opens | ☐ Pass ☐ Fail | | |
| 2 | Select invoice | Search and select invoice from TC-BIL-003 | Invoice details pre-filled | ☐ Pass ☐ Fail | | |
| 3 | Enter payment details | Amount=invoice total, Method=`Bank Transfer`, Reference=`PAY-UAT-001`, Date=today | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit payment | Click `Record Payment` | Payment created; invoice status changes to `Paid` | ☐ Pass ☐ Fail | | |
| 5 | Verify invoice status | Navigate back to invoice | Invoice status=`Paid`; payment reference shown | ☐ Pass ☐ Fail | | |

---

## TC-BIL-006: Partial Payment

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Select unpaid invoice | Open a second invoice | Invoice detail opens | ☐ Pass ☐ Fail | | |
| 2 | Record partial payment | Amount = 50% of invoice total, Method=`Cheque` | Partial payment accepted | ☐ Pass ☐ Fail | | |
| 3 | Submit | Click `Record Payment` | Payment recorded; invoice status=`Partially Paid`; outstanding balance shown | ☐ Pass ☐ Fail | | |

---

## TC-BIL-007: Credit Note

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Credit Notes | Click `Billing -> Credit Notes -> + New Credit Note` | Credit note form opens | ☐ Pass ☐ Fail | | |
| 2 | Select invoice | Link to a paid invoice | Invoice selected | ☐ Pass ☐ Fail | | |
| 3 | Fill credit note | Reason=`Unit returned — unused`, Amount=$120.00 | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Issue Credit Note` | Credit note created; linked to original invoice; hospital balance updated | ☐ Pass ☐ Fail | | |

---

## TC-BIL-008: Read-Only Access to Blood Issues

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Blood Issues | Click `Clinical -> Blood Issues` | Blood issue list loads (read-only for billing clerk) | ☐ Pass ☐ Fail | | |
| 2 | View issue details | Click on a blood issue | Issue detail shown for billing reference | ☐ Pass ☐ Fail | | |
| 3 | Verify no clinical editing | Try to modify blood issue | No edit controls available for billing clerk | ☐ Pass ☐ Fail | | |

---

## TC-BIL-009: Hospital Invoice Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hospital Billing | Click `Billing -> Hospitals` | Hospital billing summary loads | ☐ Pass ☐ Fail | | |
| 2 | Select hospital | Click on `UAT General Hospital` | Hospital billing history shown (all invoices, payments, outstanding balance) | ☐ Pass ☐ Fail | | |
| 3 | View aging report | Click `Aging Report` | Invoices grouped by age (0-30, 31-60, 60+ days overdue) shown | ☐ Pass ☐ Fail | | |

---

## TC-BIL-010: Billing Report

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Reports | Click `Reports -> Generate Report` | Report form loads | ☐ Pass ☐ Fail | | |
| 2 | Generate billing summary | Type=`Monthly Billing Summary`, Period=last 30 days | Report generates with totals per hospital | ☐ Pass ☐ Fail | | |
| 3 | Export report | Click `Export CSV` | CSV downloaded with correct data | ☐ Pass ☐ Fail | | |

---

## TC-BIL-011: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt donor registration | Navigate to `Donors -> Register New Donor` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt lab access | Navigate to `Lab -> Test Orders` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt inventory management | Navigate to `Inventory -> Storage Locations` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## TC-BIL-012: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on user profile | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Enable `Invoice Approved` and `Payment Received` notifications | Preferences saved | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-BIL-001: Login and Billing Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-002: Rate Master Management | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-003: Create Invoice | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-004: View and Manage Invoices | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-005: Record Payment | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-006: Partial Payment | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-007: Credit Note | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-008: Read-Only Access to Blood Issues | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-009: Hospital Invoice Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-010: Billing Report | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-011: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-BIL-012: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 12
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
