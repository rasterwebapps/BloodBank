# UAT Test Script — PHLEBOTOMIST

**Role**: PHLEBOTOMIST
**Scope**: Branch-level (blood collection, vitals recording, adverse reactions)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-phlebotomist@bloodbank.test`
**Keycloak Client Role**: `PHLEBOTOMIST`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-PHL-001: Login and Collection Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-phlebotomist@bloodbank.test` / `UatPhlebotomist!2026` | Authenticated; collection workstation loads | ☐ Pass ☐ Fail | | |
| 3 | View donor queue | Observe today's donor queue | Checked-in donors listed in order | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `PHLEBOTOMIST` | ☐ Pass ☐ Fail | | |

---

## TC-PHL-002: Pre-Donation Vitals Recording

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Select donor from queue | Click on `John Smith` in donor queue | Donor collection workflow opens | ☐ Pass ☐ Fail | | |
| 2 | Review donor eligibility | Check donor status and last donation date | Eligibility displayed (eligible / deferred) | ☐ Pass ☐ Fail | | |
| 3 | Record vitals | Click `Record Vitals`, enter: Weight=72kg, BP=120/80, Pulse=72, Hb=14.5 g/dL, Temperature=36.8°C | All fields accept numeric input | ☐ Pass ☐ Fail | | |
| 4 | Validate Hb threshold | Enter borderline Hb=12.0 g/dL (below 12.5 threshold) | System warns: "Hemoglobin below minimum threshold — donor may not be eligible" | ☐ Pass ☐ Fail | | |
| 5 | Correct Hb | Change to 14.5 and save | Vitals saved, no warning | ☐ Pass ☐ Fail | | |

---

## TC-PHL-003: Blood Collection Recording

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Start collection | Click `Start Collection` after vitals recorded | Collection form opens with donor pre-filled | ☐ Pass ☐ Fail | | |
| 2 | Enter collection details | Volume=450ml, Collection Type=`Whole Blood`, Bag Serial=`UAT-BAG-001`, Start Time=now | Form accepts all values | ☐ Pass ☐ Fail | | |
| 3 | Record sample tubes | Add sample tube IDs for lab testing | Sample IDs linked to collection | ☐ Pass ☐ Fail | | |
| 4 | Complete collection | Click `Complete Collection`, End Time=now+10min | Collection record created with unique blood unit ID | ☐ Pass ☐ Fail | | |
| 5 | Verify unit created | Check that blood unit appears in inventory (as `Collected`) | Unit visible with status `COLLECTED` | ☐ Pass ☐ Fail | | |
| 6 | Verify event fired | Ask observer to confirm `DonationCompletedEvent` was received in RabbitMQ | Event confirmed in queue/log | ☐ Pass ☐ Fail | | |

---

## TC-PHL-004: Record Adverse Reaction During Collection

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open active collection | Select a collection in progress | Collection detail opens | ☐ Pass ☐ Fail | | |
| 2 | Record adverse reaction | Click `Record Adverse Reaction` | Reaction form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill reaction details | Type=`Vasovagal`, Severity=`Mild`, Action Taken=`Elevated legs, applied cold compress` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Submit | Click `Submit` | Reaction recorded; linked to collection; alert sent to BRANCH_MANAGER | ☐ Pass ☐ Fail | | |
| 5 | Verify collection status | Check collection record | Collection shows `Adverse Reaction Recorded` flag | ☐ Pass ☐ Fail | | |

---

## TC-PHL-005: Donor Deferral During Screening

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Select new donor | Select a donor from the queue | Collection workflow opens | ☐ Pass ☐ Fail | | |
| 2 | Add deferral | Click `Defer Donor` | Deferral form opens | ☐ Pass ☐ Fail | | |
| 3 | Select reason | Reason=`Recent Travel to Malaria Zone`, Period=6 months | Deferral reason and end date set | ☐ Pass ☐ Fail | | |
| 4 | Submit deferral | Click `Submit Deferral` | Donor deferred; status updated; donor notified | ☐ Pass ☐ Fail | | |
| 5 | Verify deferral | Check donor profile `Deferrals` tab | Deferral entry visible with correct dates | ☐ Pass ☐ Fail | | |

---

## TC-PHL-006: Blood Camp Collection

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to active camp | Click `Camps → Active Camps` | Active camp list shown | ☐ Pass ☐ Fail | | |
| 2 | Open camp collection | Click on camp, then `+ New Collection` | Collection form with camp pre-filled | ☐ Pass ☐ Fail | | |
| 3 | Fill collection | Donor=`Camp Donor UAT`, Volume=450ml, Type=`Whole Blood` | Form accepts input | ☐ Pass ☐ Fail | | |
| 4 | Complete | Click `Complete Collection` | Collection created and linked to camp | ☐ Pass ☐ Fail | | |

---

## TC-PHL-007: View Donor Health Records

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Search for donor | Search `John Smith` | Donor profile opens | ☐ Pass ☐ Fail | | |
| 2 | View health records | Click `Health Records` tab | Prior health assessments and medical history shown | ☐ Pass ☐ Fail | | |
| 3 | Add health record | Click `+ Add Health Record` | Health record form opens | ☐ Pass ☐ Fail | | |
| 4 | Submit record | Fill health info and submit | Health record added to donor profile | ☐ Pass ☐ Fail | | |

---

## TC-PHL-008: View Collection History

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Collections | Click `Collections → All Collections` | Collections for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by date | Filter to last 7 days | Collections in range shown | ☐ Pass ☐ Fail | | |
| 3 | View my collections | Filter by `My Collections` | Only collections recorded by this phlebotomist shown | ☐ Pass ☐ Fail | | |

---

## TC-PHL-009: Update Collection Record

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open recent collection | Find the collection from TC-PHL-003 | Collection detail opens | ☐ Pass ☐ Fail | | |
| 2 | Update volume | Click `Edit`, change volume to 455ml | Field updated | ☐ Pass ☐ Fail | | |
| 3 | Save | Click `Save` | Update saved; audit trail entry created | ☐ Pass ☐ Fail | | |

---

## TC-PHL-010: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt inventory management | Navigate to `Inventory → Storage Locations` | Access denied OR not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt billing | Navigate to `Billing → Invoices` | Access denied OR not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt test result entry | Navigate to `Lab → Enter Test Results` | Access denied OR not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Verify read-only donor list | Navigate to `Donors`, try to delete a donor | No delete button visible | ☐ Pass ☐ Fail | | |

---

## TC-PHL-011: Donor Notification on Collection Complete

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Complete a collection | Complete the collection from TC-PHL-003 | Collection finalized | ☐ Pass ☐ Fail | | |
| 2 | Verify donor notification | Ask observer to check that notification was sent | Email/SMS notification to donor triggered | ☐ Pass ☐ Fail | | |
| 3 | Check notification content | View notification log (if accessible) | Content includes donation confirmation and next eligible date | ☐ Pass ☐ Fail | | |

---

## TC-PHL-012: Mandatory Test Panel Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Create a collection without samples | Attempt to complete a collection with no sample tubes | System prevents completion: "Sample tubes required for mandatory testing" | ☐ Pass ☐ Fail | | |
| 2 | Add samples and retry | Add required sample tube IDs | Collection completes successfully | ☐ Pass ☐ Fail | | |

---

## TC-PHL-013: Print Donation Receipt / Labels

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open completed collection | Navigate to completed collection from TC-PHL-003 | Collection detail page | ☐ Pass ☐ Fail | | |
| 2 | Print label | Click `Print Blood Unit Label` | Label preview shown with unit ID, blood group, collection date | ☐ Pass ☐ Fail | | |
| 3 | Print donor receipt | Click `Print Donor Receipt` | Receipt preview with donor name, unit ID, next eligible date | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-PHL-001: Login and Collection Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-002: Pre-Donation Vitals Recording | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-003: Blood Collection Recording | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-004: Record Adverse Reaction | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-005: Donor Deferral During Screening | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-006: Blood Camp Collection | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-007: View Donor Health Records | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-008: View Collection History | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-009: Update Collection Record | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-010: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-011: Donor Notification on Collection | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-012: Mandatory Test Panel Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-PHL-013: Print Donation Receipt / Labels | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 13
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
