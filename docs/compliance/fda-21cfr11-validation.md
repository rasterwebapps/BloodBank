# FDA 21 CFR Part 11 Compliance Validation

**Last Updated**: 2026-04-21
**Milestone Issues**: M9-027, M9-028
**Status**: 🟡 PENDING VALIDATION

---

## Overview

This document validates BloodBank's compliance with FDA 21 CFR Part 11 — Electronic Records; Electronic Signatures. FDA 21 CFR Part 11 applies to electronic records and signatures used in blood banking under 21 CFR Part 606 (Current Good Manufacturing Practice for Blood and Blood Components).

Key requirements:
- **Subpart B (§11.10)**: Controls for closed systems
- **Subpart B (§11.30)**: Controls for open systems
- **Subpart C (§11.50 – §11.70)**: Electronic signature requirements

---

## M9-027: Electronic Signatures Verification

### 1. Electronic Signature Implementation

BloodBank uses electronic signatures in the following critical workflows:

| Workflow | Signing Role(s) | Signature Type | Table | Status |
|---|---|---|---|---|
| Blood unit release (QC approval) | LAB_TECHNICIAN + DOCTOR (dual-sign) | E-Sign | `digital_signatures` | ☐ |
| Crossmatch authorization | DOCTOR | E-Sign | `digital_signatures` | ☐ |
| Transfusion order | DOCTOR | E-Sign | `digital_signatures` | ☐ |
| Donor consent signature | DONOR / RECEPTIONIST | E-Sign | `donor_consents.signature` | ☐ |
| Hemovigilance report | DOCTOR + BRANCH_MANAGER | E-Sign (dual) | `hemovigilance_reports` | ☐ |
| SOP acknowledgment | All clinical staff | E-Sign | `sop_documents` | ☐ |
| Regulatory deviation report | BRANCH_MANAGER + AUDITOR | E-Sign (dual) | `deviations` | ☐ |
| Chain-of-custody handoff | Releasing + Receiving staff | E-Sign (bi-party) | `chain_of_custody` | ☐ |

### 2. Signature Attribute Requirements (§11.50)

Each electronic signature record must contain:

| Required Attribute | Field | Status |
|---|---|---|
| Full name of signer | `digital_signatures.signer_name` | ☐ |
| Date and time of signing | `digital_signatures.signed_at` (UTC timestamptz) | ☐ |
| Meaning of signature (intent) | `digital_signatures.signature_meaning` (e.g., "APPROVED", "WITNESSED") | ☐ |
| Link to signed record | `digital_signatures.entity_type` + `entity_id` | ☐ |
| Signature manifest / hash | `digital_signatures.content_hash` (SHA-256 of signed content) | ☐ |
| Signature method | `digital_signatures.method` (PASSWORD_REAUTH, BIOMETRIC, PKI) | ☐ |

**Signature schema verification:**

```sql
-- Verify digital_signatures table has all required columns
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'digital_signatures'
ORDER BY ordinal_position;

-- Verify all blood unit releases have dual signatures
SELECT bu.id, COUNT(ds.id) AS signature_count
FROM blood_units bu
LEFT JOIN digital_signatures ds ON ds.entity_type = 'blood_unit'
  AND ds.entity_id = bu.id
  AND ds.signature_meaning IN ('QC_APPROVED', 'DOCTOR_RELEASED')
WHERE bu.status = 'RELEASED'
GROUP BY bu.id
HAVING COUNT(ds.id) < 2;
-- Expected: 0 rows (all released units must have 2 signatures)
```

### 3. Signature Integrity Controls (§11.70)

| Control | Implementation | Status |
|---|---|---|
| Signature linked to record | `content_hash` = SHA-256(entity content at time of signing) | ☐ |
| Tamper detection | Re-hash verification on read; alert if hash mismatch | ☐ |
| Signature non-repudiation | `signer_id` linked to Keycloak user with verified identity | ☐ |
| Re-authentication required | Password re-entry required before signing (§11.200(a)(1)) | ☐ |
| Signature unique to individual | Keycloak user credentials non-shareable (enforced by MFA) | ☐ |

### 4. Password/Credential Controls for Signatures (§11.300)

| Control | Implementation | Status |
|---|---|---|
| Unique user IDs | Keycloak — no shared accounts | ☐ |
| Password complexity | Min 12 chars, upper/lower/number/symbol (Keycloak policy) | ☐ |
| Periodic password aging | 90-day rotation enforced in Keycloak | ☐ |
| Re-authentication on idle | 30-minute idle timeout → re-auth before signing | ☐ |
| Account lockout | 5 failed attempts → 30-minute lockout (Keycloak) | ☐ |
| Credential revocation | Immediate revocation via Keycloak admin → JWT blacklist | ☐ |

---

## M9-028: Immutable Audit Trail Verification

### 5. Audit Trail Requirements (§11.10(e))

FDA 21 CFR Part 11 §11.10(e) requires computer-generated, time-stamped audit trails that:
- Record date and time of operator entries and actions
- Are retained for the life of the record
- Are available for agency review
- Cannot be modified or deleted by the operator

### 6. `audit_logs` Table Structure Verification

```sql
-- Verify audit_logs schema
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'audit_logs'
ORDER BY ordinal_position;
```

**Required columns:**

| Column | Type | Nullable | Purpose |
|---|---|---|---|
| `id` | UUID | NOT NULL | Primary key |
| `occurred_at` | TIMESTAMPTZ | NOT NULL | Event timestamp (UTC, server-generated) |
| `user_id` | UUID | NOT NULL | Keycloak user performing action |
| `user_name` | VARCHAR | NOT NULL | Captured at time of action (immutable) |
| `user_roles` | VARCHAR[] | NOT NULL | Roles at time of action |
| `branch_id` | UUID | NULL | Branch context |
| `action` | VARCHAR | NOT NULL | CREATE, READ, UPDATE, DELETE, SIGN, LOGIN, etc. |
| `entity_type` | VARCHAR | NOT NULL | Table/entity name |
| `entity_id` | UUID | NOT NULL | Record identifier |
| `old_value` | JSONB | NULL | State before change (UPDATE/DELETE) |
| `new_value` | JSONB | NULL | State after change (CREATE/UPDATE) |
| `ip_address` | INET | NOT NULL | Client IP address |
| `session_id` | VARCHAR | NOT NULL | Keycloak session ID |
| `previous_hash` | VARCHAR(64) | NULL | SHA-256 of previous audit log entry |
| `current_hash` | VARCHAR(64) | NOT NULL | SHA-256 of this entry (tamper detection) |

### 7. Immutability Controls

| Control | Implementation | Verification | Status |
|---|---|---|---|
| No UPDATE allowed | DB trigger `trg_audit_logs_no_update` blocks all UPDATEs | `SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_name = 'trg_audit_logs_no_update';` | ☐ |
| No DELETE allowed | DB trigger `trg_audit_logs_no_delete` blocks all DELETEs | `SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_name = 'trg_audit_logs_no_delete';` | ☐ |
| REVOKE UPDATE/DELETE | DB role `bloodbank_app` has no UPDATE/DELETE on audit_logs | `\dp audit_logs` in psql | ☐ |
| Hash chain integrity | Each record's `previous_hash` = hash of prior record | Run integrity verification script below | ☐ |
| Server-side timestamp | `occurred_at` set by `NOW()` DB default, not client-supplied | Verify column default = `NOW()` | ☐ |

**Immutability trigger verification:**

```sql
-- Verify UPDATE trigger exists and is enabled
SELECT trigger_name, event_manipulation, action_timing, event_object_table
FROM information_schema.triggers
WHERE event_object_table = 'audit_logs'
ORDER BY trigger_name;

-- Test immutability (should fail with exception)
BEGIN;
UPDATE audit_logs SET action = 'TAMPERED' WHERE id = (SELECT id FROM audit_logs LIMIT 1);
-- Expected: ERROR: Audit log records cannot be modified
ROLLBACK;

-- Test delete prevention (should fail)
BEGIN;
DELETE FROM audit_logs WHERE id = (SELECT id FROM audit_logs LIMIT 1);
-- Expected: ERROR: Audit log records cannot be deleted
ROLLBACK;
```

**Hash chain integrity verification:**

```sql
-- Verify hash chain integrity (spot check 1000 records)
WITH ranked AS (
  SELECT id, current_hash, previous_hash,
         LAG(current_hash) OVER (ORDER BY occurred_at, id) AS expected_previous_hash
  FROM audit_logs
  ORDER BY occurred_at, id
  LIMIT 1000
)
SELECT COUNT(*) AS chain_breaks
FROM ranked
WHERE previous_hash IS NOT NULL
  AND previous_hash != expected_previous_hash;
-- Expected: 0
```

### 8. Audit Trail Coverage — FDA 21 CFR Part 606 Events

| Regulated Event | Entity | Audit Action | Status |
|---|---|---|---|
| Blood unit creation | `blood_units` | CREATE | ☐ |
| Blood unit status change (quarantine, release, disposal) | `blood_units` | UPDATE | ☐ |
| Test result entry | `test_results` | CREATE | ☐ |
| Test result modification | `test_results` | UPDATE (with diff) | ☐ |
| Blood component processing | `component_processing` | CREATE | ☐ |
| Crossmatch initiation/result | `crossmatch_requests`, `crossmatch_results` | CREATE | ☐ |
| Blood issue to patient | `blood_issues` | CREATE | ☐ |
| Transfusion administration | `transfusions` | CREATE | ☐ |
| Adverse reaction report | `transfusion_reactions` | CREATE | ☐ |
| Electronic signature | `digital_signatures` | CREATE | ☐ |
| User login / logout | — (Keycloak events) | LOGIN, LOGOUT | ☐ |
| Permission change | — (Keycloak admin) | ROLE_ASSIGN, ROLE_REVOKE | ☐ |
| SOP acknowledgment | `sop_documents` | SIGN | ☐ |
| Deviation report | `deviations` | CREATE | ☐ |
| Recall initiation | `recall_records` | CREATE | ☐ |

### 9. Audit Log Retention (§11.10(e))

| Requirement | Implementation | Status |
|---|---|---|
| Retain for life of record | 30-year retention for blood traceability records | ☐ |
| Minimum 10 years for electronic signatures | `digital_signatures` retained 10 years minimum | ☐ |
| Available for FDA inspection | Audit log export API (AUDITOR role, encrypted) | ☐ |
| Backup and recovery | Daily encrypted backup, tested restore quarterly | ☐ |

### 10. System Validation Summary (§11.10(a))

| Validation Activity | Status | Evidence |
|---|---|---|
| Software validation testing completed | ☐ | `docs/milestones/M9-uat-compliance.md` |
| User acceptance testing (UAT) completed | ☐ | M9-001 to M9-021 |
| IQ (Installation Qualification) | ☐ | Kubernetes deployment manifests + checksums |
| OQ (Operational Qualification) | ☐ | Automated integration test suite |
| PQ (Performance Qualification) | ☐ | `docs/milestones/M8-performance-testing.md` |

---

## Sign-off

| Reviewer | Role | Date | Signature |
|---|---|---|---|
| | Quality Assurance Manager | | |
| | IT System Administrator | | |
| | Regulatory Affairs Officer | | |
| | Lead Developer | | |

**Validation Result**: ☐ PASS &nbsp;&nbsp; ☐ FAIL &nbsp;&nbsp; ☐ CONDITIONAL PASS

**Notes**:

---

*Reference: 21 CFR Part 11 — Electronic Records; Electronic Signatures*
*Reference: 21 CFR Part 606 — Current Good Manufacturing Practice for Blood and Blood Components*
