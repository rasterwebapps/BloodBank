# GDPR Compliance Validation

**Last Updated**: 2026-04-21
**Milestone Issues**: M9-024, M9-025, M9-026
**Status**: 🟡 PENDING VALIDATION

---

## Overview

This document validates BloodBank's compliance with the General Data Protection Regulation (EU) 2016/679 (GDPR). BloodBank processes personal data of donors and patients across EU member states and is therefore subject to GDPR as a data controller. Special category data (Article 9 — health data, genetic data, blood type) is processed with explicit consent and strict access controls.

---

## Lawful Basis for Processing

| Data Category | Lawful Basis | Article | Notes |
|---|---|---|---|
| Donor identity & contact | Explicit consent (Article 6(1)(a)) | Art. 6 | Consent captured at registration |
| Donor health history | Vital interests + explicit consent (Art. 9(2)(c) + (a)) | Art. 9 | Required for blood safety |
| Blood testing results | Health care provision (Art. 9(2)(h)) | Art. 9 | Medical necessity |
| Transfusion records | Health care provision + legal obligation | Art. 9 | Traceability requirement |
| Hemovigilance reports | Public health (Art. 9(2)(i)) | Art. 9 | WHO/AABB mandatory |
| Staff access audit logs | Legitimate interest (Art. 6(1)(f)) | Art. 6 | Security monitoring |
| Billing records | Legal obligation (Art. 6(1)(c)) | Art. 6 | Financial/tax records |

---

## M9-024: Consent Management Workflow Verification

### 1. Consent Capture

| Checkpoint | Implementation | Status | Evidence |
|---|---|---|---|
| Granular consent types | Separate consent flags per purpose (donation, research, marketing) | ☐ | `donor_consents` table |
| Consent version tracking | `consent_version` field links to `consent_templates` | ☐ | `shared-libs/db-migration/` |
| Timestamp of consent | `consented_at TIMESTAMPTZ` stored in UTC | ☐ | `donor_consents.consented_at` |
| Digital signature | Electronic signature (FDA 21 CFR Part 11 compliant) | ☐ | `digital_signatures` table |
| Consent at registration | RECEPTIONIST / DONOR role: consent form presented before data entry | ☐ | `frontend/bloodbank-ui/` |
| Re-consent on policy change | System triggers re-consent when `consent_templates` version increments | ☐ | `backend/donor-service/` |
| Consent withdrawal | Donor portal: "Withdraw Consent" button triggers anonymization workflow | ☐ | `frontend/bloodbank-ui/features/donor/` |

**Consent verification query:**

```sql
-- Verify all donors have valid consent records
SELECT COUNT(*) AS donors_without_consent
FROM donors d
LEFT JOIN donor_consents dc ON d.id = dc.donor_id
  AND dc.consent_type = 'DONATION'
  AND dc.is_active = true
WHERE dc.id IS NULL
  AND d.deleted_at IS NULL;
-- Expected: 0

-- Verify consent version coverage
SELECT ct.version, COUNT(dc.id) AS consented_count
FROM consent_templates ct
LEFT JOIN donor_consents dc ON dc.consent_version = ct.version AND dc.is_active = true
GROUP BY ct.version
ORDER BY ct.version DESC;
```

---

### 2. Consent Management API

| Endpoint | Role | Action | Status |
|---|---|---|---|
| `POST /api/v1/consents` | RECEPTIONIST, DONOR | Record new consent | ☐ |
| `GET /api/v1/consents/{donorId}` | RECEPTIONIST, DONOR, BRANCH_ADMIN | View consent history | ☐ |
| `PUT /api/v1/consents/{id}/withdraw` | DONOR, BRANCH_ADMIN | Withdraw consent | ☐ |
| `GET /api/v1/consent-templates/current` | PUBLIC (pre-auth) | Retrieve current consent text | ☐ |

---

## M9-025: Data Erasure / Anonymization Workflow

GDPR Article 17 — Right to Erasure ("Right to be Forgotten"). Note: For blood safety traceability, full deletion is not possible for transfused blood units. Anonymization (pseudonymization) is applied instead, preserving traceability while removing personal identifiers.

### 3. Erasure Request Workflow

```
Donor submits erasure request
           │
           ▼
    Eligibility check
    ┌──────────────────────────────────┐
    │ Can erase:                       │
    │  - Non-transfused donor data     │
    │  - Marketing/research consents   │
    │                                  │
    │ Cannot erase (legal obligation): │
    │  - Linked blood units (issued)   │
    │  - Transfusion records           │
    │  - Hemovigilance reports         │
    │  - Financial records (7 years)   │
    └──────────────────────────────────┘
           │
           ▼
    Anonymization applied to erasable fields
    (name → "ANONYMIZED", email/phone → null,
     dob → year-only, address → null)
           │
           ▼
    Consent records marked withdrawn
           │
           ▼
    Audit log entry (ERASURE_REQUEST, ANONYMIZATION_APPLIED)
           │
           ▼
    Confirmation sent to requestor
```

### 4. Anonymization Coverage Checklist

| Table | Fields Anonymized | Retained Fields | Status |
|---|---|---|---|
| `donors` | first_name, last_name, email, phone, address, national_id | id, blood_group, branch_id, created_at | ☐ |
| `donor_health_records` | medical_history, medications, allergies | id, donor_id, blood_group, deferral_status | ☐ |
| `donor_consents` | All rows soft-deleted / withdrawn | id, donor_id, consent_type (for audit) | ☐ |
| `collections` | No PII in collection records | All fields retained for traceability | ☐ |
| `notifications` | Recipient email/phone nulled | id, notification_type, sent_at | ☐ |
| `documents` | Document file deleted from MinIO | id, document_type, created_at (audit) | ☐ |

**Anonymization verification:**

```sql
-- Verify anonymization was applied
SELECT id, first_name, last_name, email, phone
FROM donors
WHERE gdpr_erasure_requested_at IS NOT NULL;
-- Expected: first_name = 'ANONYMIZED', last_name = 'ANONYMIZED',
--           email = NULL, phone = NULL

-- Confirm blood unit traceability preserved post-anonymization
SELECT bu.id, bu.blood_group, bu.status, bu.donor_id
FROM blood_units bu
JOIN donors d ON bu.donor_id = d.id
WHERE d.gdpr_erasure_requested_at IS NOT NULL
  AND bu.status IN ('ISSUED', 'TRANSFUSED');
-- blood_group and traceability chain must remain intact
```

---

### 5. Erasure Request API

| Endpoint | Role | Action | Status |
|---|---|---|---|
| `POST /api/v1/gdpr/erasure-requests` | DONOR, BRANCH_ADMIN | Submit erasure request | ☐ |
| `GET /api/v1/gdpr/erasure-requests/{id}` | BRANCH_ADMIN, AUDITOR | Check request status | ☐ |
| `POST /api/v1/gdpr/erasure-requests/{id}/process` | SYSTEM_ADMIN | Execute anonymization | ☐ |
| `GET /api/v1/gdpr/erasure-requests` | AUDITOR | List all erasure requests | ☐ |

**SLA**: Erasure requests must be processed within 30 days (GDPR Article 12(3)).

---

## M9-026: Data Portability Export Verification

GDPR Article 20 — Right to Data Portability. Donors may request a machine-readable export of their personal data.

### 6. Data Portability Coverage

| Data Category | Export Format | Tables Included | Status |
|---|---|---|---|
| Personal identity | JSON / CSV | `donors` | ☐ |
| Health history | JSON | `donor_health_records`, `donor_deferrals` | ☐ |
| Donation history | JSON | `collections`, `collection_samples` | ☐ |
| Lab results (own blood) | JSON | `test_orders`, `test_results` | ☐ |
| Consent history | JSON | `donor_consents` | ☐ |
| Loyalty points | JSON | `donor_loyalty` | ☐ |
| Notification history | JSON | `notifications` (donor-targeted) | ☐ |
| Documents | ZIP (original files from MinIO) | `documents` | ☐ |

### 7. Data Export API

| Endpoint | Role | Action | Status |
|---|---|---|---|
| `POST /api/v1/gdpr/data-export-requests` | DONOR, BRANCH_ADMIN | Request data export | ☐ |
| `GET /api/v1/gdpr/data-export-requests/{id}/status` | DONOR, BRANCH_ADMIN | Check export status | ☐ |
| `GET /api/v1/gdpr/data-export-requests/{id}/download` | DONOR | Download ZIP (time-limited URL) | ☐ |

**SLA**: Export must be available within 30 days (GDPR Article 12(3)).

**Security requirements for export:**
- Download link is a pre-signed MinIO URL, expires in 24 hours
- Link sent to verified email address only (Keycloak-verified)
- Export download logged in `audit_logs` (action = DATA_PORTABILITY_EXPORT)
- ZIP file encrypted with recipient's email-derived key

**Export verification test:**

```bash
# Trigger a test export for a test donor
curl -X POST https://api.bloodbank.example.com/api/v1/gdpr/data-export-requests \
  -H "Authorization: Bearer $DONOR_TOKEN" \
  -H "Content-Type: application/json"

# Verify export contains all required data categories
unzip donor-export-{id}.zip
ls -la
# Expected files: identity.json, health-history.json,
#                 donation-history.json, lab-results.json,
#                 consents.json, documents/
```

---

### 8. Data Protection Impact Assessment (DPIA) Status

| Processing Activity | DPIA Required | DPIA Completed | Review Date |
|---|---|---|---|
| Donor health data processing | Yes (Art. 35 — special category) | ☐ | |
| Lab results storage | Yes | ☐ | |
| Transfusion linking (recipient) | Yes | ☐ | |
| Third-party data sharing (hospitals) | Yes | ☐ | |
| Automated profiling (deferral system) | Yes | ☐ | |

---

### 9. Data Retention Policy

| Data Category | Retention Period | Legal Basis | Action at Expiry |
|---|---|---|---|
| Donor personal data (active) | Duration of relationship + 5 years | Legitimate interest | Anonymization |
| Blood unit traceability | 30 years | AABB/WHO mandatory | Anonymize donor PII, retain unit |
| Lab results | 10 years | Medical records law | Archive + anonymize PII |
| Audit logs | 7 years | HIPAA / legal | Archive to cold storage |
| Financial records (invoices) | 7 years | Tax law | Archive |
| Consent records | 5 years post-withdrawal | Legal accountability | Archive |
| GDPR erasure request records | 3 years | Accountability (Art. 5(2)) | Delete |

---

## Sign-off

| Reviewer | Role | Date | Signature |
|---|---|---|---|
| | Data Protection Officer (DPO) | | |
| | Lead Developer | | |
| | Legal Counsel | | |
| | QA Lead | | |

**Validation Result**: ☐ PASS &nbsp;&nbsp; ☐ FAIL &nbsp;&nbsp; ☐ CONDITIONAL PASS

**Notes**:

---

*Reference: Regulation (EU) 2016/679 — General Data Protection Regulation*
