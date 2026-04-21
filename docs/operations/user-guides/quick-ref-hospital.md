# Quick Reference — Hospital User

**Role**: Hospital User
**Last Updated**: 2026-04-21
**Portal**: Hospital Portal → `https://bloodbank.example.com/hospital`

> This guide covers all tasks available to Hospital Users in the BloodBank Hospital Portal.

---

## Logging In

1. Go to `https://bloodbank.example.com/hospital`
2. Click **Sign in**
3. Enter your **hospital staff email** and **password**
4. If MFA is enabled, enter the 6-digit code from your authenticator app

> Your account is scoped to your hospital. You can only submit requests and view orders for your own hospital.

---

## Submitting a Blood Request

**Menu**: Requests → New Request

1. Click **+ New Blood Request**
2. Fill in the mandatory fields:
   - Patient name and ID (internal hospital ID)
   - Blood group required
   - Component type (Whole Blood, Red Cell Concentrate, Fresh Frozen Plasma, Platelets, Cryoprecipitate)
   - Quantity (units)
   - Clinical indication (e.g., Surgical, Trauma, Anaemia, Haematological)
   - Required by date/time
   - Urgency: **Routine** / **Urgent** / **Emergency**
3. Attach supporting documents if required (e.g., patient consent, doctor order)
4. Click **Submit Request**
5. The request is sent to the blood bank for processing

> ⚠ **Emergency requests** (life-threatening): phone the blood bank directly AND submit the request in the system simultaneously. Do not wait for system confirmation.

---

## Tracking Request Status

**Menu**: Requests → My Requests

| Status | Meaning |
|---|---|
| SUBMITTED | Request received, awaiting blood bank review |
| IN PROCESS | Blood bank is matching and preparing units |
| DISPATCHED | Units on their way to your hospital |
| DELIVERED | Units delivered, awaiting your confirmation |
| COMPLETED | You have confirmed receipt |
| REJECTED | Request rejected — see reason in Notes |

### Confirm Receipt of Units

1. When status shows **DISPATCHED**, verify the physical delivery
2. Click the request and click **Confirm Receipt**
3. Enter the delivery date/time and person who received
4. Click **Save** — this closes the request loop and updates the blood bank inventory

---

## Crossmatch Requests

**Menu**: Crossmatch → New Request

For planned transfusions requiring pre-transfusion compatibility testing:

1. Click **+ New Crossmatch Request**
2. Enter patient details: name, hospital ID, blood group
3. Attach the patient blood sample ID (to be sent to the blood bank)
4. Select urgency: Routine (2 h TAT) / Urgent (30 min TAT)
5. Click **Submit**

The blood bank Lab Technician will run the crossmatch and update the result in the system. You will receive a notification when results are ready.

---

## Viewing Stock Availability

**Menu**: Stock → Availability

You can view real-time stock levels for the blood bank branches contracted to your hospital:

- Blood group and component breakdown
- Estimated availability (units)
- Next expected batch (if applicable)

> Stock levels are indicative. Final allocation is at the blood bank's discretion.

---

## Submitting Feedback

**Menu**: Feedback → New Feedback

1. Select the request or transfusion episode
2. Enter feedback type: Positive / Issue / Complaint
3. Describe the issue in the text field
4. Rate the service (1–5 stars)
5. Click **Submit**

> Feedback is reviewed by the blood bank Branch Manager and Regional Admin.

---

## Downloading Records

**Menu**: Reports → Download

Available for download:
- All blood requests (date range, CSV / PDF)
- Crossmatch results (PDF)
- Delivery confirmations (PDF)

Records are retained for 7 years as required by HIPAA.

---

## Getting Help

| Issue | Action |
|---|---|
| Cannot submit a request | Ensure all mandatory fields are completed |
| Request status not updating | Refresh the page; contact blood bank if unchanged after 30 min |
| Cannot confirm receipt | Check the request is in DISPATCHED status |
| Urgent query | Phone the blood bank directly using the contact number on your contract |
| System unavailable | Phone the blood bank emergency line immediately for urgent needs |

---

## Blood Bank Contact Details

| Contact | Details |
|---|---|
| Blood bank main number | _TBD_ |
| Emergency line (24/7) | _TBD_ |
| Email | bloodbank@example.com |
| Support portal | `https://bloodbank.example.com/support` |
