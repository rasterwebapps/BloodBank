# Quick Reference — Reception & Phlebotomy

**Roles**: Receptionist · Phlebotomist
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

**Forgot password?** Click _Forgot password_ on the login page or contact your Branch Admin.

---

## Receptionist Tasks

### Register a New Donor

**Menu**: Donors → Register New Donor

1. Click **+ Register Donor**
2. Fill in mandatory fields: First name, Last name, Date of birth, Blood group, Phone number
3. Add email address (optional but recommended for notifications)
4. Click **Check Eligibility** — the system checks deferral history automatically
5. If eligible: click **Save** — a donor ID is generated
6. Print the **Donor Card** using the Print icon

> ⚠ If the system shows **DEFERRED**: do NOT proceed with collection. Show the deferral reason to the donor and refer to your supervisor.

### Search for an Existing Donor

**Menu**: Donors → Search

- Search by: name, phone number, donor ID, or national ID
- Use **Advanced Search** for blood group or registration date filters
- Click the donor card to view full history

### Schedule a Donation Appointment

**Menu**: Donors → Appointments → New Appointment

1. Search and select the donor
2. Choose date, time slot, and collection room
3. Click **Confirm** — donor receives an SMS/email confirmation automatically

### Register a Walk-In Donation

**Menu**: Collections → Walk-In

1. Search for the donor (or register new)
2. Select **Walk-In** and assign to an available phlebotomist
3. The system creates a collection session and assigns a queue number

---

## Phlebotomist Tasks

### Start a Collection Session

**Menu**: Collections → My Queue

1. Click **Start** on the next donor in your queue
2. Confirm donor identity: ask for name + date of birth, match to screen
3. Conduct the mini-physical — enter vitals (weight, haemoglobin, blood pressure)
4. If vitals are within range, click **Proceed to Collection**
5. If any vital is out of range, click **Defer Today** and select a reason

### Record the Collection

1. Enter the **unit code** (scan the bag barcode or type manually)
2. Select the bag type (Whole Blood, etc.)
3. Enter actual volume collected (mL)
4. Click **Complete Collection**
5. Affix the label printed by the system to the collection bag
6. Place the bag in the refrigerated transport box

### Record an Adverse Reaction

**Menu**: Collections → Active Sessions → [session] → Adverse Reaction

1. Click **Record Adverse Reaction**
2. Select reaction type (Vasovagal, Haematoma, Citrate Reaction, etc.)
3. Enter severity (Mild / Moderate / Severe) and actions taken
4. Click **Save** — a notification is sent to the Doctor automatically

> ⚠ For **Severe** reactions: call the Doctor first, then record in the system.

---

## Common Statuses

| Status | Meaning |
|---|---|
| ELIGIBLE | Donor can donate today |
| DEFERRED — TEMPORARY | Donor cannot donate until the date shown |
| DEFERRED — PERMANENT | Donor is permanently deferred — do not collect |
| COLLECTION IN PROGRESS | Session is currently open |
| COLLECTION COMPLETE | Bag sent to lab |

---

## Keyboard Shortcuts

| Action | Shortcut |
|---|---|
| New donor | `Alt + N` |
| Search donors | `Alt + S` |
| My queue | `Alt + Q` |
| Print label | `Ctrl + P` |

---

## Getting Help

| Issue | Action |
|---|---|
| System is slow | Check internet connection. Refresh (F5). |
| Cannot find donor | Try searching by phone number or ID |
| Donor eligibility unclear | Ask the Doctor or Branch Manager |
| System is down | Call on-call engineer: see support contact sheet |
