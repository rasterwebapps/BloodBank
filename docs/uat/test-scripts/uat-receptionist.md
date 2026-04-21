# UAT Test Script — RECEPTIONIST

**Role**: RECEPTIONIST
**Scope**: Branch-level (donor intake, registration, appointments)
**UAT Environment**: `https://uat.bloodbank.internal`
**Test Account**: `uat-receptionist@bloodbank.test`
**Keycloak Client Role**: `RECEPTIONIST`
**Test Branch**: `UAT Central Branch`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-REC-001: Login and Reception Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to UAT URL | Open `https://uat.bloodbank.internal` | Login redirect appears | ☐ Pass ☐ Fail | | |
| 2 | Login | Use `uat-receptionist@bloodbank.test` / `UatReceptionist!2026` | Authenticated; reception dashboard loads | ☐ Pass ☐ Fail | | |
| 3 | Verify limited scope | Check navigation menu | Only reception-relevant sections visible (Donors, Appointments, Camps) | ☐ Pass ☐ Fail | | |
| 4 | Verify branch scope | Check that data is filtered to `UAT Central Branch` | No cross-branch data visible | ☐ Pass ☐ Fail | | |

---

## TC-REC-002: New Donor Walk-In Registration

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open registration | Click `Donors → Register New Donor` | Registration form opens | ☐ Pass ☐ Fail | | |
| 2 | Fill personal details | FirstName=`John`, LastName=`Smith`, DOB=`1990-03-20`, BloodGroup=`B+`, Gender=`Male` | Fields accept input | ☐ Pass ☐ Fail | | |
| 3 | Fill contact details | Email=`john.smith.uat@example.com`, Phone=`+1-555-0301`, Address=`123 Test St` | Contact fields accepted | ☐ Pass ☐ Fail | | |
| 4 | Enter national ID | NationalID=`UAT-NATID-001` | Field accepts input | ☐ Pass ☐ Fail | | |
| 5 | Complete health questionnaire | Answer all pre-donation screening questions | Form progresses | ☐ Pass ☐ Fail | | |
| 6 | Capture consent | Check `Donor Consent` box and record consent date | Consent recorded | ☐ Pass ☐ Fail | | |
| 7 | Submit | Click `Register Donor` | Donor created with unique ID; confirmation shown | ☐ Pass ☐ Fail | | |
| 8 | Verify donor ID | Note the generated donor ID | Format: `DON-YYYYMMDD-NNN` | ☐ Pass ☐ Fail | | |

---

## TC-REC-003: Duplicate Donor Detection

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt duplicate registration | Re-enter same email `john.smith.uat@example.com` and try to register | System warns: "A donor with this email already exists" | ☐ Pass ☐ Fail | | |
| 2 | Search by email | Click `Find Existing Donor` | Existing donor record found | ☐ Pass ☐ Fail | | |

---

## TC-REC-004: Update Existing Donor Profile

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Search for donor | Search `John Smith` | Donor found | ☐ Pass ☐ Fail | | |
| 2 | Update phone | Click `Edit`, change phone to `+1-555-0302` | Field updated | ☐ Pass ☐ Fail | | |
| 3 | Save | Click `Save Changes` | Changes saved with confirmation | ☐ Pass ☐ Fail | | |
| 4 | Verify PII masking | Check that National ID and email appear in full | Receptionist has full PII access | ☐ Pass ☐ Fail | | |

---

## TC-REC-005: Donor Check-In for Walk-In Donation

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Search for donor | Search `John Smith` | Donor profile opens | ☐ Pass ☐ Fail | | |
| 2 | Check donation eligibility | Review donor status | Status shows `Eligible` or `Deferred` | ☐ Pass ☐ Fail | | |
| 3 | Check-in for donation | Click `Walk-In Check-In` | Check-in confirmation; donor queued for phlebotomist | ☐ Pass ☐ Fail | | |
| 4 | Verify queue | Check the phlebotomist queue | `John Smith` appears in today's donor queue | ☐ Pass ☐ Fail | | |

---

## TC-REC-006: Appointment Scheduling

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Appointments | Click `Appointments → Schedule Appointment` | Appointment scheduling form opens | ☐ Pass ☐ Fail | | |
| 2 | Select donor | Search and select `John Smith` | Donor pre-populated | ☐ Pass ☐ Fail | | |
| 3 | Select date/time | Choose next available slot | Available slots shown; one selected | ☐ Pass ☐ Fail | | |
| 4 | Set type | Type = `Whole Blood Donation` | Type set | ☐ Pass ☐ Fail | | |
| 5 | Book appointment | Click `Book` | Appointment created; confirmation email notification triggered | ☐ Pass ☐ Fail | | |
| 6 | View appointment | Check `Appointments → Upcoming` | Appointment shows in calendar/list | ☐ Pass ☐ Fail | | |

---

## TC-REC-007: Cancel/Reschedule Appointment

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View appointment | Navigate to the appointment from TC-REC-006 | Appointment detail opens | ☐ Pass ☐ Fail | | |
| 2 | Reschedule | Click `Reschedule`, select a new date/time | New slot available | ☐ Pass ☐ Fail | | |
| 3 | Confirm | Click `Confirm Reschedule` | Appointment updated; notification triggered | ☐ Pass ☐ Fail | | |
| 4 | Cancel appointment | Click `Cancel Appointment`, reason=`Donor request` | Appointment cancelled; slot freed | ☐ Pass ☐ Fail | | |

---

## TC-REC-008: Deferral Check at Reception

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Search for a deferred donor | Search a known deferred donor | Donor profile shows `Deferred` status | ☐ Pass ☐ Fail | | |
| 2 | View deferral reasons | Click `Deferrals` tab | Deferral reason, start date, end date shown | ☐ Pass ☐ Fail | | |
| 3 | Attempt check-in | Click `Walk-In Check-In` on deferred donor | Warning shown: "Donor is currently deferred until {date}" | ☐ Pass ☐ Fail | | |
| 4 | Proceed with override | Check if override is possible for receptionist | Override NOT available at receptionist level — doctor/admin only | ☐ Pass ☐ Fail | | |

---

## TC-REC-009: Donor Search and Read-Only View

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Search donors | Click `Donors → All Donors` | Donor list for branch loads | ☐ Pass ☐ Fail | | |
| 2 | Filter by blood group | Filter = `O-` | Only O-Negative donors shown | ☐ Pass ☐ Fail | | |
| 3 | View donation history | Click on a donor, click `Donation History` | Past donations shown (dates, volumes, statuses) | ☐ Pass ☐ Fail | | |
| 4 | Verify no admin actions | Check for edit/delete on test results | Test results section is read-only for receptionist | ☐ Pass ☐ Fail | | |

---

## TC-REC-010: Blood Camp Registration (Donor Side)

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View blood camp listing | Click `Camps → Upcoming Camps` | Upcoming camps in region listed | ☐ Pass ☐ Fail | | |
| 2 | Register donor for camp | Select a camp, click `Register Donor`, search `John Smith` | Donor registered for camp | ☐ Pass ☐ Fail | | |
| 3 | View camp registrations | Click on camp | Donor appears in camp registration list | ☐ Pass ☐ Fail | | |

---

## TC-REC-011: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click on profile icon | Profile page loads | ☐ Pass ☐ Fail | | |
| 2 | Update preferences | Click `Notification Preferences`, toggle settings | Preferences saved | ☐ Pass ☐ Fail | | |

---

## TC-REC-012: Consent Document Upload

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open donor profile | Search `John Smith`, open profile | Profile page | ☐ Pass ☐ Fail | | |
| 2 | Upload consent form | Click `Documents → Upload Consent Form`, select PDF | Upload dialog opens | ☐ Pass ☐ Fail | | |
| 3 | Complete upload | Click `Upload` | Consent form stored; linked to donor record | ☐ Pass ☐ Fail | | |
| 4 | Attempt non-consent upload | Try to upload a non-consent document type | Permission denied — receptionist can only upload consent forms | ☐ Pass ☐ Fail | | |

---

## TC-REC-013: Restricted Access Verification

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Attempt to access Lab | Navigate to `Lab → Test Orders` | Access denied OR section not in navigation | ☐ Pass ☐ Fail | | |
| 2 | Attempt to access Billing | Navigate to `Billing → Invoices` | Access denied OR section not in navigation | ☐ Pass ☐ Fail | | |
| 3 | Attempt to access System Settings | Navigate to `Administration → System Settings` | Access denied OR section not in navigation | ☐ Pass ☐ Fail | | |
| 4 | Attempt to access all donors globally | Try to see donors from another branch | No cross-branch data returned | ☐ Pass ☐ Fail | | |

---

## TC-REC-014: Loyalty Points View

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Open donor profile | Open `John Smith` profile | Profile page | ☐ Pass ☐ Fail | | |
| 2 | View loyalty status | Click `Loyalty` tab | Donation count, loyalty tier, points balance shown | ☐ Pass ☐ Fail | | |
| 3 | Inform donor | Confirm loyalty info can be communicated verbally | Data visible and accurate | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-REC-001: Login and Reception Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-002: New Donor Walk-In Registration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-003: Duplicate Donor Detection | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-004: Update Existing Donor Profile | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-005: Donor Check-In for Walk-In | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-006: Appointment Scheduling | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-007: Cancel/Reschedule Appointment | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-008: Deferral Check at Reception | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-009: Donor Search and Read-Only View | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-010: Blood Camp Registration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-011: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-012: Consent Document Upload | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-013: Restricted Access Verification | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-REC-014: Loyalty Points View | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 14
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
