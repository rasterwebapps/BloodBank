# Ongoing Compliance

**Last Updated**: 2026-04-22
**Milestone Issues**: M13-012, M13-013, M13-014, M13-015, M13-016
**Effective From**: Post worldwide launch (M12)
**Status**: 🔴 NOT STARTED

---

## Overview

This document defines the recurring compliance activities required to maintain BloodBank's compliance posture after production launch. BloodBank operates under HIPAA, GDPR, FDA 21 CFR Part 11, AABB Standards, and WHO Guidelines.

| Cadence | Activity | Owner | Issue |
|---|---|---|---|
| Quarterly | Access review (dormant accounts > 90 days) | Security Lead | M13-012 |
| Annual | HIPAA compliance audit | HIPAA Privacy + Security Officers | M13-013 |
| Annual | GDPR data processing review | Data Protection Officer | M13-014 |
| Ongoing | Regulatory update monitoring | Compliance Lead | M13-015 |
| Continuous | SOP version management | Quality Manager | M13-016 |

---

## M13-012: Quarterly Access Review

### Schedule

Quarterly, aligned with the security assessment cycle:
- **Q1**: March (Week 3)
- **Q2**: June (Week 3)
- **Q3**: September (Week 3)
- **Q4**: December (Week 3)

Duration: 2–3 days (automated report Day 1, manual review Day 2, remediation Day 3).

### Scope

The quarterly access review covers all 16 Keycloak roles across all branches:

| Review Area | Description |
|---|---|
| Dormant accounts | Users with zero logins in the past 90 days |
| Orphaned accounts | Accounts of former employees still active |
| Excessive privileges | Users with roles beyond their job function |
| Shared accounts | Accounts used by more than one person |
| Service accounts | Non-human accounts with privileged roles |
| Admin accounts | SUPER_ADMIN and SYSTEM_ADMIN — stringent review |
| DONOR accounts | Self-registered donors who have not donated in 24 months |

### Dormant Account Identification

#### Keycloak Query (Admin REST API)

```bash
# Get all users not logged in since 90 days ago
# (timestamp = current epoch - 90 days in milliseconds)
CUTOFF=$(date -d '90 days ago' +%s)000

curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  "https://keycloak.bloodbank.example.com/admin/realms/bloodbank/users?max=1000" \
  | jq --argjson cutoff "$CUTOFF" \
       '[.[] | select(.lastLogin != null and .lastLogin < $cutoff) | {id, username, email, lastLogin, enabled}]'
```

#### Dormant Account Decision Matrix

| Account Type | Dormant Period | Action |
|---|---|---|
| Clinical roles (DOCTOR, NURSE, LAB_TECHNICIAN, PHLEBOTOMIST) | > 90 days | Notify manager, disable if no response in 5 business days |
| Admin roles (BRANCH_ADMIN, BRANCH_MANAGER, REGIONAL_ADMIN) | > 90 days | Notify HR, disable immediately, investigate |
| SUPER_ADMIN / SYSTEM_ADMIN | > 30 days | Disable, escalate to CISO |
| RECEPTIONIST, BILLING_CLERK, CAMP_COORDINATOR | > 90 days | Notify manager, disable if no response in 5 business days |
| HOSPITAL_USER | > 90 days | Notify hospital IT contact, disable if no response |
| DONOR | > 24 months no donation | Archive (retain data, disable login) |
| AUDITOR | > 90 days | Notify Compliance Lead, disable if no response |

### Access Review Checklist

#### Step 1: Generate Access Report

- [ ] Export all active user accounts from Keycloak
- [ ] Export last-login timestamps for all accounts
- [ ] Export role assignments for all accounts
- [ ] Cross-reference with HR active employee list
- [ ] Cross-reference with hospital partner contact list

#### Step 2: Identify Anomalies

- [ ] Flag all accounts with no login in > 90 days
- [ ] Flag all accounts of employees who have left (check against HR termination records)
- [ ] Flag any SUPER_ADMIN or SYSTEM_ADMIN accounts not in the approved admin register
- [ ] Flag any accounts with multiple roles that exceed normal job function
- [ ] Flag any shared accounts (same email domain, different usernames)
- [ ] Flag any service accounts without documented ownership

#### Step 3: Remediation Actions

For each flagged account:

```
□ Notify the account's line manager or HR (email template below)
□ Wait 5 business days for response
□ If confirmed active (legitimate dormancy: leave, training):
    → Document in access register, set next review date
□ If no response or confirmed inactive:
    → Disable account in Keycloak
    → Record action in audit log
    → Notify Security Lead
□ If suspected former employee:
    → Disable immediately
    → Escalate to Security Lead for investigation
    → Check audit logs for any access during dormant period
```

#### Step 4: Privilege Review

For a random sample of 20% of active accounts:

- [ ] Verify assigned roles match job title in HR system
- [ ] Verify branch assignment matches physical branch of employment
- [ ] Confirm no unnecessary additional roles beyond minimum required

#### Step 5: Report and Sign-off

- [ ] Dormant accounts disabled: __ accounts
- [ ] Orphaned accounts disabled: __ accounts
- [ ] Privilege anomalies corrected: __ accounts
- [ ] Findings documented in quarterly access review report

### Notification Email Template

```
Subject: BloodBank System Access Review — Action Required

Dear [Manager Name],

As part of our quarterly access review, the following account has been 
identified as dormant (no login in the past 90 days):

  Username: [username]
  Name: [full name]
  Role: [role]
  Last Login: [date]
  Branch: [branch name]

Please confirm by [DATE + 5 BUSINESS DAYS] whether this account should:
  (a) Remain active — provide expected return-to-use date
  (b) Be disabled — user has left or no longer requires access

If we do not receive a response, the account will be disabled on [DATE].

Reply to: security@bloodbank.example.com

BloodBank Security Team
```

### Quarterly Access Review Report Template

```markdown
## Quarterly Access Review Report — Q[N] [YEAR]

**Date**: [DATE]
**Performed by**: [NAME]
**Total accounts reviewed**: [N]

### Summary

| Category | Found | Disabled | Retained (documented) |
|---|---|---|---|
| Dormant (> 90 days) | | | |
| Orphaned (former employee) | | | |
| Excessive privileges | | | |
| Shared accounts | | | |
| Admin accounts reviewed | | | |

### Disabled Accounts Log

| Username | Role | Branch | Last Login | Reason | Disabled By | Date |
|---|---|---|---|---|---|---|
| | | | | | | |

### Privilege Corrections

| Username | Previous Roles | Corrected Roles | Justification |
|---|---|---|---|
| | | | |

### Exceptions (Documented Dormancy)

| Username | Role | Last Login | Justification | Review Date |
|---|---|---|---|---|
| | | | | |

### Sign-off

Security Lead: _____________ Date: _____________
HIPAA Privacy Officer: _____________ Date: _____________
```

---

## M13-013: Annual HIPAA Compliance Audit

### Schedule

Once per year, targeting **Q1** (January–February). Completed before the annual GDPR review.

The audit covers all controls defined in `docs/compliance/hipaa-validation.md` and assesses whether those controls remain operational throughout the year.

### HIPAA Annual Audit Checklist

#### Administrative Safeguards (45 CFR § 164.308)

| Control | Requirement | Verified | Evidence |
|---|---|---|---|
| Security Officer designated | HIPAA Security Officer role assigned in writing | ☐ | HR file |
| Privacy Officer designated | HIPAA Privacy Officer role assigned in writing | ☐ | HR file |
| Risk analysis performed | Annual risk assessment completed | ☐ | Risk assessment report |
| Risk management plan | Identified risks have mitigation plans | ☐ | Risk management plan |
| Sanction policy | Policy for workforce members who violate HIPAA | ☐ | HR policy |
| Workforce training | All workforce members trained on HIPAA annually | ☐ | Training records |
| Access management | Procedures for granting and revoking PHI access | ☐ | Keycloak access review |
| Security incident procedures | Incident response plan documented and tested | ☐ | `docs/operations/incident-response.md` |
| Contingency plan | DR plan documented and tested quarterly | ☐ | DR drill reports |
| Business Associate Agreements | BAA on file for all third-party PHI processors | ☐ | Contract register |
| Evaluation | Annual technical and non-technical evaluation | ☐ | This document |

#### Physical Safeguards (45 CFR § 164.310)

| Control | Requirement | Verified | Evidence |
|---|---|---|---|
| Facility access controls | Data centre access restricted to authorized personnel | ☐ | Cloud provider SOC 2 report |
| Workstation use policy | Policies for workstations that access PHI | ☐ | IT policy |
| Workstation security | Physical safeguards for workstations (lock screen, encryption) | ☐ | IT audit |
| Device and media controls | Policies for disposal and re-use of media containing PHI | ☐ | IT policy |

#### Technical Safeguards (45 CFR § 164.312)

| Control | Requirement | Verified | Evidence |
|---|---|---|---|
| Unique user identification | Each user has unique Keycloak account | ☐ | Keycloak audit |
| Emergency access procedure | Break-glass access procedure documented | ☐ | Runbook |
| Automatic logoff | Session timeout ≤ 30 minutes idle in Keycloak | ☐ | Keycloak config |
| Encryption at rest | AES-256 on all PHI storage (PostgreSQL, MinIO, Redis) | ☐ | Infrastructure config |
| Encryption in transit | TLS 1.3 for all PHI transmission | ☐ | `openssl s_client` test |
| Audit controls | All PHI access logged in `audit_logs` | ☐ | DB query |
| Integrity controls | Hash chain on audit logs, immutable records | ☐ | DB config |
| Transmission security | HTTPS enforced (HSTS), no plaintext PHI transmission | ☐ | HSTS header check |

#### Audit Trail Verification

```sql
-- Verify audit log coverage: every donor read generates an audit entry
SELECT COUNT(DISTINCT d.id) AS donors_accessed_today,
       COUNT(DISTINCT al.entity_id) AS donors_in_audit_today
FROM donors d
JOIN audit_logs al ON al.entity_id = d.id::text
                   AND al.action = 'READ'
                   AND al.created_at >= CURRENT_DATE;

-- Verify audit log immutability: no UPDATE or DELETE privilege
SELECT grantee, privilege_type
FROM information_schema.role_table_grants
WHERE table_name = 'audit_logs'
  AND privilege_type IN ('UPDATE', 'DELETE');
-- Expected: 0 rows

-- Verify 7-year retention — audit logs from 7+ years ago exist (or policy declared)
SELECT MIN(created_at) AS oldest_audit_entry FROM audit_logs;
```

#### Breach Notification Readiness

- [ ] Breach notification templates reviewed and up to date
- [ ] HHS/OCR contact information current
- [ ] 60-day notification window tracked in compliance calendar
- [ ] All BAs notified within 60 days of any discovered breach (if applicable)
- [ ] Breach log reviewed — any incidents in past year requiring notification?

#### HIPAA Training Records Review

- [ ] All workforce members completed annual HIPAA training
- [ ] Training completion documented and dated
- [ ] New hires received HIPAA training within 30 days of start
- [ ] Training materials updated to reflect current regulatory guidance

### Annual HIPAA Audit Report Template

```markdown
## Annual HIPAA Compliance Audit — [YEAR]

**Date**: [DATE]
**Performed by**: [HIPAA Security Officer]
**Period covered**: [YEAR-1 full year]

### Compliance Summary

| Safeguard Category | Controls | Compliant | Non-compliant | N/A |
|---|---|---|---|---|
| Administrative | 11 | | | |
| Physical | 4 | | | |
| Technical | 8 | | | |
| **Total** | **23** | | | |

**Overall HIPAA Compliance**: ✅ COMPLIANT / ⚠️ CONDITIONALLY COMPLIANT / ❌ NON-COMPLIANT

### Breaches Reported This Year

| # | Date | Nature | Individuals Affected | HHS Notified? | Status |
|---|---|---|---|---|---|
| None | — | — | 0 | — | — |

### Non-Compliant Items

| # | Control | Gap | Risk Level | Remediation | Due |
|---|---|---|---|---|---|
| 1 | | | | | |

### Sign-off

HIPAA Privacy Officer: _____________ Date: _____________
HIPAA Security Officer: _____________ Date: _____________
Executive Sponsor: _____________ Date: _____________
```

---

## M13-014: Annual GDPR Data Processing Review

### Schedule

Once per year, targeting **Q2** (April–May). Completed after the HIPAA audit so findings can be cross-referenced.

This review assesses all controls defined in `docs/compliance/gdpr-validation.md` for continued operational effectiveness.

### Annual GDPR Review Checklist

#### Records of Processing Activities (ROPA) — Article 30

- [ ] ROPA document reviewed and updated to reflect any new data flows
- [ ] New third-party processors (sub-processors) added to ROPA
- [ ] Removed processors removed from ROPA
- [ ] Data retention schedules verified and enforced
- [ ] Cross-border data transfer records updated (SCCs, adequacy decisions)

#### Lawful Basis and Consent

- [ ] Lawful basis for each data processing activity confirmed valid
- [ ] Consent versions reviewed — are they still granular, specific, and informed?
- [ ] Consent withdrawal rate reviewed — any patterns indicating issues?
- [ ] Re-consent triggered for donors where consent version has changed

```sql
-- Donors with outdated consent versions
SELECT d.id, d.first_name, d.last_name, dc.consent_version,
       (SELECT MAX(version) FROM consent_templates WHERE type = 'DONATION') AS current_version
FROM donors d
JOIN donor_consents dc ON dc.donor_id = d.id AND dc.consent_type = 'DONATION'
WHERE dc.consent_version < (SELECT MAX(version) FROM consent_templates WHERE type = 'DONATION');
```

#### Data Subject Rights

- [ ] Right of access (SAR) requests received and fulfilled within 30 days — log reviewed
- [ ] Right to erasure requests: anonymization workflow verified
- [ ] Right to data portability: export function tested for common file format
- [ ] Right to rectification: correction workflow functional
- [ ] Right to restriction: restriction flag mechanism verified
- [ ] Right to object: objection workflow documented

```sql
-- Review SAR (Subject Access Request) fulfilment log
SELECT request_type, COUNT(*) AS requests,
       AVG(EXTRACT(DAY FROM fulfilled_at - requested_at)) AS avg_days_to_fulfil
FROM dsar_log
WHERE requested_at >= NOW() - INTERVAL '1 year'
GROUP BY request_type;
-- All avg_days_to_fulfil must be ≤ 30

-- Review anonymization log
SELECT COUNT(*) AS anonymized_this_year
FROM audit_logs
WHERE action = 'ANONYMIZE'
  AND created_at >= NOW() - INTERVAL '1 year';
```

#### Data Minimisation and Retention

- [ ] Data minimisation: no unnecessary PHI fields collected in recent releases
- [ ] Retention schedule enforced: automatic purge jobs running correctly

```sql
-- Verify data purge/anonymization is running on schedule
-- Donors who donated >7 years ago should be anonymized (if retention period is 7 years)
SELECT COUNT(*) AS due_for_anonymization
FROM donors d
WHERE d.last_donation_date < NOW() - INTERVAL '7 years'
  AND d.anonymized_at IS NULL;
```

#### Third-Party Processor Review

- [ ] List of all data processors reviewed against contracts
- [ ] DPA (Data Processing Agreement) on file for each processor
- [ ] Sub-processor notifications received from main processors (where applicable)
- [ ] Processor security posture reviewed (SOC 2 / ISO 27001 certificates current)

**Current data processors**:

| Processor | Purpose | DPA on File | DPA Expiry | Sub-processors |
|---|---|---|---|---|
| Cloud provider (K8s/PostgreSQL hosting) | Infrastructure | ☐ | | Review with provider |
| MinIO / S3 | Document storage | ☐ | | |
| Email provider | Notifications | ☐ | | |
| SMS provider | Notifications | ☐ | | |
| Keycloak hosting (if SaaS) | Authentication | ☐ | | |

#### DPA (Data Protection Authority) Monitoring

- [ ] Check for new GDPR enforcement actions relevant to healthcare/blood banks
- [ ] Review ICO (UK) / relevant national DPA guidance updates
- [ ] Verify any cross-border transfer mechanisms are still valid (adequacy decisions, SCCs)

### Annual GDPR Review Report Template

```markdown
## Annual GDPR Data Processing Review — [YEAR]

**Date**: [DATE]
**Performed by**: [Data Protection Officer]
**Period covered**: [YEAR-1 full year]

### Summary

| Area | Status | Notes |
|---|---|---|
| ROPA | Up to date / Needs update | |
| Consent management | Compliant / Issues found | |
| Data subject rights | Compliant / Issues found | |
| Data retention | Compliant / Issues found | |
| Third-party processors | Compliant / Issues found | |
| Cross-border transfers | Compliant / N/A | |

### Data Subject Requests (Year in Review)

| Right | Requests | Fulfilled On Time | Avg Days | Complaints |
|---|---|---|---|---|
| Access (SAR) | | | | |
| Erasure | | | | |
| Portability | | | | |
| Rectification | | | | |
| Restriction | | | | |
| Object | | | | |

### Personal Data Breaches

| # | Date | Nature | Data Subjects Affected | DPA Notified? | Outcome |
|---|---|---|---|---|---|
| None | — | — | 0 | — | — |

### Actions Required

| # | Finding | Risk | Action | Owner | Due |
|---|---|---|---|---|---|
| 1 | | | | | |

### Sign-off

Data Protection Officer: _____________ Date: _____________
Legal Counsel: _____________ Date: _____________
Executive Sponsor: _____________ Date: _____________
```

---

## M13-015: Regulatory Update Monitoring

### Overview

Blood bank regulations (FDA, AABB, WHO) and data protection regulations (HIPAA, GDPR) evolve continuously. This process ensures BloodBank remains compliant with current standards.

### Regulatory Bodies to Monitor

| Body | Jurisdiction | Relevance | Monitoring Frequency |
|---|---|---|---|
| FDA (Food and Drug Administration) | USA | Blood product safety, 21 CFR Part 606, Part 11 | Monthly |
| AABB (formerly American Association of Blood Banks) | International | Blood banking standards, accreditation | Quarterly (Standards update cycle) |
| WHO (World Health Organization) | Global | Blood safety guidelines, test panel requirements | Quarterly |
| EU European Commission | EU | GDPR updates, Blood Directives (2002/98/EC) | Monthly |
| HHS / OCR (Office for Civil Rights) | USA | HIPAA guidance, enforcement actions | Monthly |
| National DPAs | Per country | GDPR implementation, local data protection | Quarterly |
| ISO / IEC | International | ISO 15189 (medical labs), ISO 27001 (security) | Annual |

### Monitoring Process

#### Monthly Review (first Monday of each month)

The Compliance Lead performs:

1. **FDA monitoring**
   - Check FDA MedWatch and Safety Communications: `https://www.fda.gov/safety/medwatch`
   - Check FDA Blood & Blood Products news: `https://www.fda.gov/vaccines-blood-biologics/blood-blood-products`
   - Check HHS OCR guidance updates: `https://www.hhs.gov/hipaa/for-professionals/guidance/index.html`
   - Check EU GDPR enforcement tracker

2. **Review all updates** for applicability to BloodBank

3. **Log in the regulatory update register** (below)

4. **Escalate high-impact changes** to Compliance Lead and affected service team

#### Quarterly Deep Review

Each quarter (aligned with the security assessment):

- Full review of AABB Standards updates
- WHO blood safety guideline review
- National DPA enforcement actions relevant to health data processing

#### Impact Assessment

For each regulatory update:

| Step | Action |
|---|---|
| 1 | Classify: NEW requirement / CHANGED requirement / GUIDANCE (non-binding) |
| 2 | Determine applicability: Does this affect BloodBank's operations? |
| 3 | Assess impact: Software change / Process change / Documentation only |
| 4 | Assign owner: Compliance Lead / Security Lead / Backend Lead |
| 5 | Set deadline: Based on regulatory effective date |
| 6 | Create ticket and track to completion |

### Regulatory Update Register

Maintain a living log of all identified regulatory changes:

| Date | Source | Update | Applicable? | Impact | Owner | Status | Target Date |
|---|---|---|---|---|---|---|---|
| | FDA | | Yes / No | Software / Process / Doc | | 🔴 / 🟡 / ✅ | |
| | AABB | | Yes / No | | | | |
| | WHO | | Yes / No | | | | |
| | GDPR | | Yes / No | | | | |
| | HIPAA | | Yes / No | | | | |

### High-Impact Regulatory Change Response

When a change requiring a software update is identified:

1. Compliance Lead notifies Backend/Frontend Lead and Project Manager
2. Impact assessment completed within 5 business days
3. Development ticket created with regulatory reference and deadline
4. Change implemented before the regulatory effective date
5. Evidence of compliance documented (test results, audit log, screenshots)
6. `docs/compliance/` files updated to reflect new requirement

### SOP for New Blood Test Requirements (WHO / AABB)

If a new mandatory test panel is required (e.g., a new pathogen screening):

1. **Compliance Lead** confirms the requirement and effective date
2. **Flyway migration** created for new test type in `test_panels` table
3. **Lab service** updated to include new test type in collection workflow
4. **Reporting service** updated to include new test in regulatory reports
5. **Compliance documentation** updated (`docs/compliance/who-validation.md` or `aabb-validation.md`)
6. **Staff training** materials updated and distributed to lab staff

---

## M13-016: SOP Version Management Lifecycle

### Overview

Standard Operating Procedures (SOPs) govern clinical and operational workflows in BloodBank. This section defines how SOPs are created, reviewed, updated, approved, and retired.

### SOP Registry

All SOPs are registered in the SOP registry (see table format below). The Quality Manager owns the registry.

| SOP-ID | Title | Version | Status | Owner | Last Review | Next Review |
|---|---|---|---|---|---|---|
| SOP-001 | Donor Registration Workflow | 1.0 | Active | Receptionist Lead | 2026-01-01 | 2027-01-01 |
| SOP-002 | Whole Blood Collection Protocol | 1.0 | Active | Phlebotomist Lead | 2026-01-01 | 2027-01-01 |
| SOP-003 | Blood Testing and Lab Processing | 1.0 | Active | Lab Lead | 2026-01-01 | 2027-01-01 |
| SOP-004 | Crossmatch and Blood Issue | 1.0 | Active | Blood Bank Manager | 2026-01-01 | 2027-01-01 |
| SOP-005 | Transfusion Administration | 1.0 | Active | Nursing Lead | 2026-01-01 | 2027-01-01 |
| SOP-006 | Adverse Reaction Management | 1.0 | Active | Medical Director | 2026-01-01 | 2027-01-01 |
| SOP-007 | Inventory Management and Stock Control | 1.0 | Active | Inventory Manager | 2026-01-01 | 2027-01-01 |
| SOP-008 | Blood Camp Organisation | 1.0 | Active | Camp Coordinator | 2026-01-01 | 2027-01-01 |
| SOP-009 | Emergency Blood Issue Protocol | 1.0 | Active | Medical Director | 2026-01-01 | 2027-01-01 |
| SOP-010 | Hemovigilance Reporting | 1.0 | Active | Quality Manager | 2026-01-01 | 2027-01-01 |
| SOP-011 | Data Breach Response (HIPAA/GDPR) | 1.0 | Active | Compliance Lead | 2026-01-01 | 2027-01-01 |
| SOP-012 | System Downtime Emergency Procedures | 1.0 | Active | DevOps Lead | 2026-01-01 | 2027-01-01 |

### SOP Lifecycle Stages

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  DRAFT   │───▶│  REVIEW  │───▶│APPROVED  │───▶│  ACTIVE  │───▶│ RETIRED  │
│          │    │          │    │          │    │          │    │          │
│ Author   │    │ SME peer │    │ Medical  │    │ In use   │    │ Replaced │
│ creates  │    │ review   │    │ Director │    │ by staff │    │ or       │
│ draft    │    │ + Legal  │    │ sign-off │    │          │    │ obsolete │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
      ▲                                │
      │        Version increment       │
      └────────────────────────────────┘
         (on regulatory change or annual review)
```

### SOP Review and Update Triggers

An SOP must be reviewed and potentially updated when:

| Trigger | Urgency | Review Window |
|---|---|---|
| Regulatory change (FDA, AABB, WHO, HIPAA, GDPR) | High | Before regulatory effective date |
| Patient safety event or near-miss | Critical | Within 5 business days |
| Annual scheduled review | Normal | Within the review month |
| Significant software update affecting the workflow | Normal | Before software release |
| Staff feedback identifying procedural gap | Normal | Within 30 days |
| Audit finding | High | Within 30 days |

### Annual SOP Review Cycle

All SOPs are reviewed annually. The Quality Manager schedules reviews so no more than 3 SOPs are reviewed in any given month.

#### Annual Review Schedule Template

| Month | SOPs Due for Review |
|---|---|
| January | SOP-001, SOP-002 |
| February | SOP-003, SOP-004 |
| March | SOP-005, SOP-006 |
| April | SOP-007, SOP-008 |
| May | SOP-009, SOP-010 |
| June | SOP-011, SOP-012 |
| July–December | New SOPs added during the year |

### SOP Version Control Rules

| Rule | Requirement |
|---|---|
| Version numbering | Major.Minor (e.g., 1.0, 1.1, 2.0) |
| Minor version (x.Y) | Clarifications, non-substantive changes, formatting |
| Major version (X.0) | Substantive workflow changes, new regulatory requirements |
| Effective date | All SOPs have an explicit effective date on the cover page |
| Supersession | Retiring SOP must reference the new SOP that supersedes it |
| Archive | Retired SOP versions retained for 7 years (regulatory requirement) |
| Digital signature | All approved SOPs carry digital signatures (FDA 21 CFR Part 11) |

### SOP in BloodBank System

SOPs are stored in the `documents` table (document-service) with version control:

```sql
-- Current active SOP versions
SELECT d.title, d.version, d.status, d.effective_date, d.review_date
FROM documents d
WHERE d.document_type = 'SOP'
  AND d.status = 'ACTIVE'
ORDER BY d.title;

-- SOPs due for review in next 30 days
SELECT d.title, d.version, d.review_date, d.owner
FROM documents d
WHERE d.document_type = 'SOP'
  AND d.status = 'ACTIVE'
  AND d.review_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days';

-- SOPs overdue for review
SELECT d.title, d.version, d.review_date, d.owner
FROM documents d
WHERE d.document_type = 'SOP'
  AND d.status = 'ACTIVE'
  AND d.review_date < CURRENT_DATE;
```

### SOP Approval Workflow

All SOPs follow a structured approval workflow tracked in BloodBank's document-service:

1. **Author** creates draft — sets status to `DRAFT`
2. **Peer review** by a subject matter expert — comments resolved
3. **Quality Manager** reviews for completeness and consistency — status to `UNDER_REVIEW`
4. **Legal / Compliance review** (for regulatory SOPs) — sign-off documented
5. **Medical Director** (for clinical SOPs) or **Department Head** approval — digital signature
6. **Quality Manager** sets status to `APPROVED`, sets effective date
7. **System** auto-notifies all affected staff (via notification-service)
8. **Training** completed if new version requires re-training
9. **Previous version** archived (status to `RETIRED`)

### SOP Communication and Training

When a new SOP version is published:

- [ ] Notification sent to all staff with the relevant role (via notification-service)
- [ ] If major version (X.0): mandatory re-training required before access restored
- [ ] If minor version (x.Y): read-and-acknowledge notification sufficient
- [ ] Training completion recorded in document-service (linked to the SOP version)
- [ ] Compliance evidence: training records archived for 7 years

### SOP Compliance Dashboard Queries

```sql
-- Staff who have not acknowledged the latest SOP version
SELECT u.username, u.email, d.title AS sop, d.version
FROM users u
CROSS JOIN documents d
LEFT JOIN document_acknowledgements da
  ON da.user_id = u.id AND da.document_id = d.id AND da.version = d.version
WHERE d.document_type = 'SOP'
  AND d.status = 'ACTIVE'
  AND da.id IS NULL
  AND u.role IN ('PHLEBOTOMIST', 'LAB_TECHNICIAN', 'NURSE', 'DOCTOR')
ORDER BY d.title, u.username;

-- SOP version history for audit
SELECT d.title, dv.version, dv.status, dv.effective_date,
       dv.approved_by, dv.retired_at
FROM document_versions dv
JOIN documents d ON d.id = dv.document_id
WHERE d.document_type = 'SOP'
ORDER BY d.title, dv.version;
```

### SOP Management Compliance Checklist (Annual)

Performed as part of the annual HIPAA and internal quality audit:

- [ ] All SOPs in the registry have a review date within the past 12 months
- [ ] No SOPs are overdue for review (review_date < today)
- [ ] All clinical SOPs carry valid digital signatures from Medical Director
- [ ] All staff have acknowledged current SOP versions (no gaps in document_acknowledgements)
- [ ] Retired SOP versions accessible in archive (document-service archive)
- [ ] Regulatory SOPs reference current regulation version (e.g., AABB Standards edition)
- [ ] SOP training records auditable for past 7 years

### SOP Version Change Log

Maintain a chronological record of all SOP changes:

| Date | SOP-ID | Old Version | New Version | Change Summary | Author | Approver |
|---|---|---|---|---|---|---|
| | | | | | | |

---

## Compliance Calendar

Use this calendar to schedule and track all recurring compliance activities:

| Activity | Q1 (Jan–Mar) | Q2 (Apr–Jun) | Q3 (Jul–Sep) | Q4 (Oct–Dec) |
|---|---|---|---|---|
| Quarterly access review | March | June | September | December |
| Quarterly regulatory monitoring review | March | June | September | December |
| Annual HIPAA audit | January–February | — | — | — |
| Annual GDPR review | — | April–May | — | — |
| Annual penetration test | January–February | — | — | — |
| SOP reviews | Per schedule | Per schedule | Per schedule | Per schedule |
| AABB Standards review | — | — | — | December |
| WHO guideline review | March | June | September | December |

---

*Related documents:*
- *`docs/compliance/hipaa-validation.md` — Full HIPAA controls baseline*
- *`docs/compliance/gdpr-validation.md` — Full GDPR controls baseline*
- *`docs/compliance/fda-21cfr11-validation.md` — FDA 21 CFR Part 11 controls*
- *`docs/compliance/aabb-validation.md` — AABB Standards compliance*
- *`docs/compliance/who-validation.md` — WHO Guidelines compliance*
- *`docs/operations/ongoing-operations.md` — Quarterly security assessment and dependency updates*
- *`docs/milestones/M13-post-launch.md` — Milestone tracker*
