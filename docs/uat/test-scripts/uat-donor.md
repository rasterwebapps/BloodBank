# UAT Test Script — DONOR

**Role**: DONOR
**Scope**: Donor self-service portal (registration, donation history, appointments, donor card)
**UAT Environment**: `https://uat.bloodbank.internal/donor`
**Test Account**: `uat-donor@bloodbank.test`
**Keycloak Client Role**: `DONOR`

---

**Tester Name**: ___________________________
**Test Date**: ___________________________
**Session #**: ___________________________
**Observer**: ___________________________

---

## TC-DON-001: Self-Registration

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donor Portal | Open `https://uat.bloodbank.internal/donor` | Donor portal landing page appears | ☐ Pass ☐ Fail | | |
| 2 | Click Register | Click `Register as a Donor` | Self-registration form opens | ☐ Pass ☐ Fail | | |
| 3 | Fill personal details | FirstName=`UAT`, LastName=`Self Donor`, DOB=`1992-11-10`, BloodGroup=`O+`, Email=`uat-donor@bloodbank.test`, Phone=`+1-555-0400` | Fields accept input | ☐ Pass ☐ Fail | | |
| 4 | Complete health questionnaire | Answer all pre-donation health screening questions honestly | Questionnaire captured | ☐ Pass ☐ Fail | | |
| 5 | Accept consent | Read and accept `Donor Consent for Data Processing` and `Donation Consent` | Consent checkboxes accepted | ☐ Pass ☐ Fail | | |
| 6 | Submit registration | Click `Register` | Account created; welcome email sent; Keycloak account provisioned | ☐ Pass ☐ Fail | | |
| 7 | Login | Use new credentials to log in | Authenticated; donor portal dashboard loads | ☐ Pass ☐ Fail | | |

---

## TC-DON-002: Login and Donor Dashboard

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Login | Use `uat-donor@bloodbank.test` / `UatDonor!2026` | Donor portal loads | ☐ Pass ☐ Fail | | |
| 2 | View dashboard | Observe portal content | Donation count, last donation date, next eligible date, loyalty points, upcoming appointments shown | ☐ Pass ☐ Fail | | |
| 3 | Verify self-scope | Check that only own data is visible | No other donor's data accessible | ☐ Pass ☐ Fail | | |
| 4 | Verify role badge | Check top-right menu | Displays `DONOR` | ☐ Pass ☐ Fail | | |

---

## TC-DON-003: View Donation History

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to History | Click `My Donations` | Donation history list loads | ☐ Pass ☐ Fail | | |
| 2 | View donation details | Click on a past donation | Donation detail shown (date, volume, collection site, blood unit ID) | ☐ Pass ☐ Fail | | |
| 3 | Verify test result visibility | Check if test results are shown | Test result status visible (e.g., "All Clear") — not raw values | ☐ Pass ☐ Fail | | |
| 4 | Download donation receipt | Click `Download Receipt` | PDF receipt downloads with donation details | ☐ Pass ☐ Fail | | |

---

## TC-DON-004: Book Donation Appointment

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Appointments | Click `Book Appointment` | Appointment booking form opens | ☐ Pass ☐ Fail | | |
| 2 | Select branch | Choose `UAT Central Branch` | Branch selected; available slots shown | ☐ Pass ☐ Fail | | |
| 3 | Select date and time | Choose a date in next 2 weeks, available time slot | Slot selected | ☐ Pass ☐ Fail | | |
| 4 | Select donation type | Type=`Whole Blood Donation` | Type set | ☐ Pass ☐ Fail | | |
| 5 | Book | Click `Book Appointment` | Appointment confirmed; confirmation notification sent | ☐ Pass ☐ Fail | | |
| 6 | View upcoming appointments | Navigate to `My Appointments` | New appointment shown | ☐ Pass ☐ Fail | | |

---

## TC-DON-005: Cancel Appointment

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | View appointments | Click `My Appointments` | Appointment list loads | ☐ Pass ☐ Fail | | |
| 2 | Cancel appointment | Click `Cancel` on the appointment from TC-DON-004 | Cancellation confirmation dialog appears | ☐ Pass ☐ Fail | | |
| 3 | Confirm cancellation | Click `Confirm Cancel` | Appointment cancelled; notification sent to branch | ☐ Pass ☐ Fail | | |
| 4 | Verify cancellation | Check appointment list | Appointment shows as `Cancelled` | ☐ Pass ☐ Fail | | |

---

## TC-DON-006: View and Download Donor Card

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Donor Card | Click `My Donor Card` | Digital donor card displayed | ☐ Pass ☐ Fail | | |
| 2 | Verify card content | Review card details | Name, donor ID, blood group, total donations, loyalty tier shown | ☐ Pass ☐ Fail | | |
| 3 | Download card | Click `Download PDF` | PDF donor card downloads | ☐ Pass ☐ Fail | | |
| 4 | View QR code | Check for QR code on card | QR code present linking to donor profile (for blood bank scan) | ☐ Pass ☐ Fail | | |

---

## TC-DON-007: Update Profile

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Profile | Click `My Profile` | Profile edit page opens | ☐ Pass ☐ Fail | | |
| 2 | Update phone number | Change phone to `+1-555-0401` | Field updated | ☐ Pass ☐ Fail | | |
| 3 | Update address | Change address | Field updated | ☐ Pass ☐ Fail | | |
| 4 | Save | Click `Save Changes` | Profile updated with confirmation | ☐ Pass ☐ Fail | | |
| 5 | Verify no blood group change | Try to change blood group | Field not editable by donor (requires staff) | ☐ Pass ☐ Fail | | |

---

## TC-DON-008: View Deferral Status

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Eligibility | Click `My Eligibility` | Eligibility status shown | ☐ Pass ☐ Fail | | |
| 2 | Check deferral (if applicable) | Review any active deferrals | If deferred: reason and end date shown; next eligible date shown | ☐ Pass ☐ Fail | | |
| 3 | Attempt appointment while deferred | Try to book appointment during deferral period | Warning shown: "You are currently deferred until {date}" | ☐ Pass ☐ Fail | | |

---

## TC-DON-009: Notification Preferences

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Preferences | Click `Preferences -> Notifications` | Notification preference settings shown | ☐ Pass ☐ Fail | | |
| 2 | Toggle notifications | Enable `Appointment Reminder`, `Next Eligible Date`, disable `Camp Announcements` | Preferences saved | ☐ Pass ☐ Fail | | |
| 3 | Verify preferences applied | Check preference display | Settings match what was set | ☐ Pass ☐ Fail | | |

---

## TC-DON-010: GDPR — Export My Data

| # | Step | Action | Expected Result | Result | Defect ID | Notes |
|---|---|---|---|---|---|---|
| 1 | Navigate to Privacy | Click `Privacy -> Export My Data` | Data export request page opens | ☐ Pass ☐ Fail | | |
| 2 | Request export | Click `Request Data Export` | Export requested; notification: "Your data export will be ready within 24 hours" | ☐ Pass ☐ Fail | | |
| 3 | Download export | (Simulated) Click `Download` when ready | JSON or CSV export downloads with all personal data | ☐ Pass ☐ Fail | | |
| 4 | Verify completeness | Review exported data | Name, contact, donation history, deferrals, consents, appointments included | ☐ Pass ☐ Fail | | |

---

## Summary

| Scenario | Result | Defect ID |
|---|---|---|
| TC-DON-001: Self-Registration | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-002: Login and Donor Dashboard | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-003: View Donation History | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-004: Book Donation Appointment | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-005: Cancel Appointment | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-006: View and Download Donor Card | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-007: Update Profile | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-008: View Deferral Status | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-009: Notification Preferences | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |
| TC-DON-010: GDPR — Export My Data | ☐ Pass ☐ Fail ☐ Partial ☐ Skip | |

**Total Passed**: _____ / 10
**Total Failed**: _____
**Total Defects Raised**: _____

**Tester Sign-off**: _________________________ Date: _____________
**Observer Sign-off**: _________________________ Date: _____________
