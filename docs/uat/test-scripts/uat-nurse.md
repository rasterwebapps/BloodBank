# UAT Test Script — NURSE

**Role**: NURSE
**Scope**: Branch-level (transfusion administration, reaction monitoring, hemovigilance)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-nurse@bloodbank.test`
**Keycloak Client Role**: `NURSE`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-NUR-001: Login and Nursing Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-nurse@bloodbank.test` / `UatNurse!2026` | Authenticated; nursing dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | View pending transfusions | Check transfusion queue | Transfusions ordered by doctor and awaiting nurse administration shown | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `NURSE` | ☐ Pass ☐ Fail | | |

---

## TC-NUR-002: Administer Transfusion

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Select transfusion order | Click on a pending transfusion from TC-DOC-006 | Transfusion detail opens | ☐ Pass ☐ Fail | | |
| 2 | Verify patient identity | Confirm patient name, ID, blood group against blood unit label | All details match | ☐ Pass ☐ Fail | | |
| 3 | Start transfusion | Click `Start Administration` | Transfusion status changes to `In Progress`; start time recorded | ☐ Pass ☐ Fail | | |
| 4 | Record pre-transfusion check | Confirm bed-side checklist: 2-person verification, unit label check | Checklist items marked | ☐ Pass ☐ Fail | | |
| 5 | Log infusion progress | Click `Log Progress`, enter 15-min check vitals: BP=130/85, Temp=37.1, Pulse=80 | Progress log recorded | ☐ Pass ☐ Fail | | |
| 6 | Complete transfusion | Click `Complete Transfusion`, enter end time and total volume infused | Transfusion marked `Completed`; `TransfusionCompletedEvent` fires | ☐ Pass ☐ Fail | | |

---

## TC-NUR-003: Transfusion Reaction Reporting

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open in-progress transfusion | Select the transfusion from TC-NUR-002 | Transfusion detail opens | ☐ Pass ☐ Fail | | |
| 2 | Stop transfusion due to reaction | Click `Stop Transfusion — Reaction Suspected` | Transfusion paused; reaction report form opens immediately | ☐ Pass ☐ Fail | | |
| 3 | Fill reaction report | Reaction Type=`Febrile Non-Hemolytic`, Onset=`20 minutes post-infusion`, Symptoms=`Chills and fever (38.9C)`, Severity=`Moderate` | Form accepts all input | ☐ Pass ☐ Fail | | |
| 4 | Submit reaction | Click `Submit Reaction Report` | Reaction recorded; `TransfusionReactionEvent` fires; doctor and manager notified immediately | ☐ Pass ☐ Fail | | |
| 5 | Follow-up notification | Confirm alert received | On-screen alert + notification to treating doctor visible | ☐ Pass ☐ Fail | | |

---

## TC-NUR-004: Post-Transfusion Documentation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open completed transfusion | Navigate to the completed transfusion from TC-NUR-002 | Transfusion detail opens | ☐ Pass ☐ Fail | | |
| 2 | Update post-transfusion vitals | Click `Post-Transfusion Vitals`, enter BP=128/82, Temp=37.0, Pulse=76 | Vitals saved | ☐ Pass ☐ Fail | | |
| 3 | Add nursing note | Click `Add Note`, enter clinical note | Note saved and timestamped | ☐ Pass ☐ Fail | | |
| 4 | Confirm patient outcome | Set outcome=`No adverse events` | Outcome recorded | ☐ Pass ☐ Fail | | |

---

## TC-NUR-005: View Transfusion History

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Transfusions | Click `Clinical -> Transfusions` | Transfusion list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by patient | Enter patient name filter | Patient-specific transfusions shown | ☐ Pass ☐ Fail | | |
| 3 | View transfusion detail | Click on a completed transfusion | Full record shown (prescriber, nurse, units, vitals, reactions) | ☐ Pass ☐ Fail | | |

---

## TC-NUR-006: View Hemovigilance Reports

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Hemovigilance | Click `Clinical -> Hemovigilance Reports` | Reports list loads | ☐ Pass ☐ Fail | | |
| 2 | View a report | Click on a report | Full report visible | ☐ Pass ☐ Fail | | |
| 3 | Verify read-only for nurse | Try to edit a submitted hemovigilance report | No edit controls — read-only | ☐ Pass ☐ Fail | | |

---

## TC-NUR-007: Collection Adverse Reaction Reporting

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Collections | Click `Collections -> All Collections` | Collection list loads (read-only for nurse) | ☐ Pass ☐ Fail | | |
| 2 | Report adverse reaction | Click on a collection, click `+ Record Adverse Reaction` | Reaction form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill reaction | Type=`Hematoma`, Severity=`Minor`, Action=`Ice pack applied` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit` | Reaction recorded; linked to collection | ☐ Pass ☐ Fail | | |

---

## TC-NUR-008: Read-Only Donor Information

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donors | Click `Donors -> All Donors` | Donor list loads (read-only for nurse) | ☐ Pass ☐ Fail | | |
| 2 | View donor profile | Click on a donor | Donor read-only profile shown (health records, deferrals visible) | ☐ Pass ☐ Fail | | |
| 3 | Verify no edit access | Try to edit donor profile | No edit controls visible for nurse | ☐ Pass ☐ Fail | | |

---

## TC-NUR-009: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on user profile | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Enable alert | Toggle `New Transfusion Order` alert on | Preference saved | ☐ Pass ☐ Fail | | |
| 3 | Enable reaction alert | Toggle `Transfusion Reaction Escalation` alert on | Preference saved | ☐ Pass ☐ Fail | | |

---

## TC-NUR-010: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt inventory management | Navigate to `Inventory -> Storage Locations` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt billing access | Navigate to `Billing -> Invoices` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt lab test entry | Navigate to `Lab -> Enter Test Results` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Attempt cross-match request | Navigate to `Lab -> Cross-Match -> + New Request` | Access denied or not in navigation | ☐ Pass ☐ Fail | | |

---

## TC-NUR-011: SOP Document Access

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to SOPs | Click `Compliance -> SOP Documents` | SOP list loads | ☐ Pass ☐ Fail | | |
| 2 | View nursing SOP | Click on `Transfusion Administration SOP v2.0` | SOP opens or downloads | ☐ Pass ☐ Fail | | |
| 3 | Verify no SOP editing | Look for edit controls | No edit/delete available for nurse | ☐ Pass ☐ Fail | | |

---

## TC-NUR-012: Transfusion Reaction — Escalation Workflow

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View reaction reports | Navigate to `Clinical -> Transfusion Reactions` | Active reactions listed | ☐ Pass ☐ Fail | | |
| 2 | Escalate reaction | Click `Escalate to Doctor` on a reaction | Escalation sent; doctor receives notification | ☐ Pass ☐ Fail | | |
| 3 | Track escalation status | View reaction status | Escalation status shows `Escalated — Awaiting Doctor Response` | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-NUR-001: Login and Nursing Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-002: Administer Transfusion | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-003: Transfusion Reaction Reporting | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-004: Post-Transfusion Documentation | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-005: View Transfusion History | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-006: View Hemovigilance Reports | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-007: Collection Adverse Reaction Reporting | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-008: Read-Only Donor Information | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-009: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-010: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-011: SOP Document Access | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-NUR-012: Transfusion Reaction — Escalation Workflow | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 12
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
