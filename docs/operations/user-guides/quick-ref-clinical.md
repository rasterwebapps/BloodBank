# Quick Reference — Clinical Staff

**Roles**: Doctor · Nurse
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

## Doctor Tasks

### Approve or Defer a Donor (Pre-Collection Medical Review)

**Menu**: Collections → Pending Medical Review

1. Review the donor's health screening form and vitals submitted by the Phlebotomist
2. Click **Approve Collection** or **Defer**
3. If deferring: select the deferral reason and duration (temporary / permanent)
4. Add any clinical notes — these are stored in the donor health record

> ⚠ Permanent deferrals require your electronic signature. The system will prompt for Keycloak re-authentication.

### Review an Adverse Reaction Report

**Menu**: Collections → Adverse Reactions

1. Click on the reported reaction
2. Review severity and actions taken by phlebotomy staff
3. Add your clinical assessment and recommended follow-up
4. Change status to **Reviewed** or **Escalated**
5. If Severe: complete the **Haemovigilance Report** (see below)

### Order a Crossmatch

**Menu**: Transfusion → Crossmatch → New Request

1. Search for the recipient patient (or enter patient details)
2. Enter the blood group and component required
3. Select urgency: Routine (2 h), Urgent (30 min), Emergency (immediate)
4. Click **Submit** — the system notifies Inventory Manager and Lab

### Review Crossmatch Results

**Menu**: Transfusion → Crossmatch → Pending Review

1. Review the compatibility result
2. If compatible: click **Approve for Transfusion**
3. If incompatible: click **Reject** and add clinical note

### Complete a Haemovigilance Report

**Menu**: Transfusion → Haemovigilance → New Report

1. Enter the transfusion episode details
2. Select the reaction type (TRALI, TACO, Febrile, Allergic, etc.)
3. Enter the imputability grade (1–4)
4. Attach relevant documents if available
5. Submit — the report is queued for Compliance Officer review

---

## Nurse Tasks

### Administer a Transfusion

**Menu**: Transfusion → Active Transfusions

1. Click **Start Transfusion** on the assigned unit
2. Confirm patient identity: name + date of birth + patient ID (two-identifier rule)
3. Confirm blood unit: scan the unit label barcode
4. System confirms compatibility — if mismatch, transfusion is **blocked**
5. Enter the start time and rate (mL/h)
6. Click **Transfusion Started**

### Record Transfusion Progress

**Menu**: Transfusion → Active → [patient]

- Record observations at: 15 min, 30 min, 1 h, end of transfusion
- Observations: BP, pulse, temperature, SpO₂, patient comfort
- Click **Add Observation** for each check

### Record Transfusion Completion

1. Click **Complete Transfusion**
2. Enter volume actually transfused (mL)
3. Confirm stop time
4. Click **Save** — unit status changes to `TRANSFUSED`

### Record a Transfusion Reaction (Nurse)

**Menu**: Transfusion → Active → [patient] → Report Reaction

1. Click **Stop Transfusion** immediately if reaction suspected
2. Click **Report Reaction**
3. Enter: reaction type, time of onset, symptoms, actions taken
4. Click **Submit** — Doctor is notified immediately by the system

---

## Common Statuses

| Status | Meaning |
|---|---|
| PENDING MEDICAL REVIEW | Donor waiting for doctor sign-off |
| APPROVED FOR COLLECTION | Doctor cleared the donation |
| DEFERRED | Donor cannot donate (see reason) |
| CROSSMATCH PENDING | Compatibility test in progress |
| APPROVED FOR TRANSFUSION | Crossmatch passed, ready to transfuse |
| TRANSFUSION IN PROGRESS | Active transfusion |
| TRANSFUSED | Transfusion completed |

---

## Getting Help

| Issue | Action |
|---|---|
| Cannot find patient | Confirm patient is registered in the system by Receptionist |
| Crossmatch result pending too long | Contact Lab Technician directly |
| System blocked transfusion | DO NOT override — verify unit and patient labels physically |
| System down | Call on-call engineer. Use paper backup protocol. See support contact sheet. |

> ⚠ **If the system is unavailable during an emergency transfusion**: follow the paper backup protocol posted at the transfusion station. Record all details on paper and enter into the system as soon as it is restored.
