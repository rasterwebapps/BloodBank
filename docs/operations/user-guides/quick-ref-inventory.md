# Quick Reference — Inventory Management

**Role**: Inventory Manager
**Last Updated**: 2026-04-21
**Portal**: Staff Portal → `https://bloodbank.example.com/staff`

> Print this card and keep it at your workstation. For full documentation, visit the Help Centre.

---

## Logging In

1. Go to `https://bloodbank.example.com/staff`
2. Click **Sign in with Keycloak**
3. Enter your **staff email** and **password**
4. If MFA is enabled, enter the 6-digit code from your authenticator app
5. Select your **branch** from the branch selector (top-right)

---

## Daily Workflow Overview

```
Receive released units from Lab
       ↓
Process into components (if applicable)
       ↓
Assign storage locations
       ↓
Monitor stock levels and expiry dates
       ↓
Fulfil blood requests / issue to hospitals
       ↓
Manage transfers and disposals
```

---

## Receiving Units from Lab

**Menu**: Inventory → Receive from Lab

1. Click **Scan Unit** (or + Add Manually)
2. Scan the unit barcode
3. Confirm the storage location (auto-suggested by the system based on component type)
4. Click **Confirm Receipt** — unit enters inventory with status `AVAILABLE`

---

## Processing Blood Components

**Menu**: Inventory → Processing → New Processing Job

1. Select the whole blood unit(s) to process
2. Select the processing type: Separation → Red Cells + Plasma + Platelets
3. Enter component volumes and expiry dates per component
4. Click **Complete Processing**
5. The system creates child component records linked to the parent unit
6. Print labels for each component

---

## Stock Level Dashboard

**Menu**: Inventory → Dashboard

The dashboard shows real-time stock by blood group and component type.

| Indicator | Meaning |
|---|---|
| 🟢 Green | Stock ≥ minimum threshold |
| 🟡 Yellow | Stock between minimum and critical threshold |
| 🔴 Red | Stock at or below critical threshold |

> ⚠ When any blood group goes **red**: notify the Branch Manager immediately. The system also sends an automatic alert.

---

## Issuing Blood to a Hospital Request

**Menu**: Inventory → Blood Requests → Pending

1. Click on the request
2. Review the request details: blood group, component, quantity, urgency
3. Click **Match Units** — the system suggests available units (FIFO by expiry)
4. Review the suggested units, override if needed
5. Click **Confirm Issue**
6. Print the **Issue Slip** and hand with the units to the transporter

### Emergency Issues

**Menu**: Inventory → Emergency Issue

For O-negative emergency requests:
1. Click **Emergency Issue**
2. Enter the requesting hospital or doctor
3. Select units (O-negative preferred)
4. Click **Issue Immediately** — no crossmatch required

---

## Monitoring Expiry

**Menu**: Inventory → Expiring Units

- Units expiring within 3 days are listed automatically
- For each unit, choose: **Transfer to partner branch**, **Allocate to pending request**, or **Schedule for disposal**

---

## Recording a Disposal

**Menu**: Inventory → Disposals → New Disposal

1. Scan or select the unit to dispose
2. Select disposal reason: Expired, Damaged, QC Failure, Reactive
3. Confirm the physical disposal method (biohazard waste, autoclave, etc.)
4. Click **Record Disposal** — audit trail entry is created automatically

---

## Stock Transfer Between Branches

**Menu**: Inventory → Transfers → Request Transfer

1. Select the destination branch
2. Select units to transfer
3. Enter transport method and cold-chain temperature log ID
4. Click **Submit Transfer Request**
5. Destination branch must confirm receipt

---

## Common Statuses

| Status | Meaning |
|---|---|
| AVAILABLE | Unit in stock, ready to issue |
| RESERVED | Unit matched to a pending request |
| IN TRANSIT | Unit being transferred to another branch |
| ISSUED | Unit issued to hospital |
| DISCARDED | Unit disposed of |
| EXPIRED | Unit passed expiry date (auto-set by system) |

---

## Getting Help

| Issue | Action |
|---|---|
| Unit not showing in inventory | Check lab has released it (Lab → Passed Units) |
| Cannot issue unit (status locked) | Check if unit is reserved for another request |
| Stock alert not clearing | Refresh dashboard after confirming receipt |
| System down | Call on-call engineer: see support contact sheet |
