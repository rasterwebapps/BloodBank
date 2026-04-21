# Quick Reference — Laboratory

**Role**: Lab Technician
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
Receive bag from phlebotomy
       ↓
Scan unit code → system creates Test Order
       ↓
Run mandatory test panel
       ↓
Enter results in system
       ↓
System auto-interprets: PASS / FAIL / INCONCLUSIVE
       ↓
  PASS → Unit released to processing
  FAIL → Unit quarantined, donor notification triggered
```

---

## Receiving a Blood Unit

**Menu**: Lab → Incoming Units

1. Click **Scan Incoming Unit** (or click + Receive Manually)
2. Scan the bag barcode — the unit details appear
3. Confirm the bag is intact: no leaks, correct label
4. Click **Confirm Receipt** — unit status changes to `IN LAB`
5. A test order is automatically generated

---

## Entering Test Results

**Menu**: Lab → Test Orders → Pending

1. Click on the test order for the unit
2. The **mandatory test panel** is shown:
   - HIV Ag/Ab
   - Hepatitis B Surface Antigen (HBsAg)
   - Hepatitis C Antibody (anti-HCV)
   - Syphilis (RPR / TPHA)
   - Malaria (where applicable per branch setting)
   - Blood grouping confirmation (ABO + Rh)
3. Enter the result for each test (Reactive / Non-Reactive, or titre where applicable)
4. Enter the instrument ID / lot number for each test
5. Click **Save Results**

### Auto-Interpretation Rules

| Panel Result | System Action |
|---|---|
| All non-reactive, blood group confirmed | Unit → `PASSED` → released for processing |
| Any reactive result | Unit → `QUARANTINED`, donor flagged for follow-up |
| Any inconclusive result | Unit → `PENDING RETEST`, alert sent to Lab Supervisor |

---

## Quality Control Records

**Menu**: Lab → Quality Control

Record QC for each instrument shift:

1. Select the instrument
2. Select QC level (Low / Normal / High)
3. Enter QC lot number and result value
4. System flags if result is outside ±2 SD of the moving mean

> ⚠ If QC fails: do NOT process patient samples. Run repeat QC. If repeat fails, take the instrument out of service and notify the Lab Supervisor.

---

## Releasing a Unit

**Menu**: Lab → Passed Units

1. Confirm all test results are entered and status is `PASSED`
2. Click **Release Unit**
3. Print the final unit label (blood group + component + expiry confirmed)
4. Hand unit to Inventory Manager or place in designated handover location

---

## Quarantining a Unit

**Menu**: Lab → Quarantined Units

1. Click the unit
2. Confirm the reactive/failing test
3. Click **Quarantine** — unit is moved to the quarantine storage location in the system
4. The system automatically notifies the Doctor and Branch Manager
5. Complete the **Donor Notification Checklist** — the system generates a task

---

## Retesting

**Menu**: Lab → Test Orders → Pending Retest

1. Select the inconclusive result
2. Click **Create Retest Order**
3. Run the retest on a fresh aliquot
4. Enter new results
5. If retest is conclusive: system updates the unit status automatically

---

## Common Statuses

| Status | Meaning |
|---|---|
| IN LAB | Unit received, awaiting testing |
| TESTING IN PROGRESS | Test order open |
| PASSED | All tests non-reactive, unit released |
| QUARANTINED | One or more reactive results |
| PENDING RETEST | Inconclusive result, awaiting retest |
| DISCARDED | Unit disposed of after reactive confirmation |

---

## Getting Help

| Issue | Action |
|---|---|
| Cannot find test order | Check unit was received (Menu: Lab → Incoming Units) |
| Instrument result not accepted | Check result format — enter as per test kit instructions |
| QC failure | Take instrument out of service, notify Lab Supervisor |
| System down | Call on-call engineer: see support contact sheet |
