# Runbook: Data Corruption or Data Integrity Issue

**Last Updated**: 2026-04-22
**Severity**: P1 (confirmed corruption in production data)
**Response Owner**: Technical Lead (L3) + DBA + On-call Engineer (L2)
**Escalation**: Immediately escalate to L3 and L4 — do NOT attempt to fix data without approval

> ⚠️ **CRITICAL**: Data corruption in a blood bank system can have life-threatening consequences (e.g., incorrect blood group, wrong crossmatch result). Never modify production data without Technical Lead and Clinical Lead sign-off. For any corruption affecting active transfusion records, immediately notify the Clinical Lead.

---

## Trigger

This runbook is activated when:
- A user reports data that doesn't match expectations (wrong blood group, incorrect unit status, missing records)
- Automated integrity checks fail (Grafana alert: `BloodBankDataIntegrityError`)
- Audit log gaps detected (missing expected entries)
- Two audit log entries conflict (e.g., unit marked `ISSUED` but no corresponding crossmatch)
- Post-migration validation detects orphaned or inconsistent records
- A double-click or race condition creates duplicate critical records

---

## IMMEDIATE ACTIONS (First 10 Minutes)

### 1. Stop All Writes to Affected Data (If Corruption Is Active)

If the corruption is ongoing or caused by a running process:

```bash
# Put the affected service into maintenance mode (disables write endpoints)
# This requires a custom maintenance flag in application config:
kubectl patch configmap <SERVICE_NAME>-config -n bloodbank-prod \
  --patch '{"data":{"maintenance.mode":"true"}}'
kubectl rollout restart deployment/<SERVICE_NAME> -n bloodbank-prod
```

If you cannot isolate the service, escalate to L3 immediately for a decision to take the service offline.

### 2. Escalate Immediately

```
Phone: Technical Lead — [see PagerDuty]
Phone: DBA — [see PagerDuty]
Slack: @tech-lead @dba in #bloodbank-security-incident (private)
If transfusion/crossmatch records affected: Also call Clinical Lead immediately
```

### 3. Snapshot the Database (Before Any Changes)

```bash
# Take an immediate point-in-time snapshot before any recovery actions
# AWS RDS:
aws rds create-db-snapshot \
  --db-instance-identifier bloodbank-prod \
  --db-snapshot-identifier incident-$(date +%Y%m%d%H%M%S)

# Or PostgreSQL manual dump:
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  pg_dump -U bloodbank bloodbank_db -F c \
  -f /var/lib/postgresql/data/incident-snapshot-$(date +%Y%m%d%H%M%S).dump

# Copy snapshot to S3
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  aws s3 cp /var/lib/postgresql/data/incident-snapshot-*.dump \
  s3://bloodbank-backups/incidents/
```

---

## Step 1 — Identify the Scope of Corruption (≤ 20 minutes)

### 1a. Blood Unit Integrity Checks

```sql
-- Units with impossible status transitions
SELECT id, unit_number, status, created_at, updated_at, branch_id
FROM blood_units
WHERE status = 'ISSUED'
  AND id NOT IN (SELECT blood_unit_id FROM blood_issues WHERE status = 'COMPLETED')
ORDER BY updated_at DESC
LIMIT 50;

-- Expired units still marked as AVAILABLE
SELECT id, unit_number, expiry_date, status
FROM blood_units
WHERE status = 'AVAILABLE'
  AND expiry_date < now()
ORDER BY expiry_date ASC
LIMIT 50;

-- Units with duplicate unit_number within same branch
SELECT unit_number, branch_id, COUNT(*) as duplicates
FROM blood_units
GROUP BY unit_number, branch_id
HAVING COUNT(*) > 1;
```

### 1b. Crossmatch and Transfusion Integrity

```sql
-- Issued blood without a corresponding completed crossmatch
SELECT bi.id AS issue_id, bi.blood_unit_id, bi.patient_id, bi.created_at
FROM blood_issues bi
LEFT JOIN crossmatch_results cr ON bi.crossmatch_request_id = cr.crossmatch_request_id
  AND cr.result = 'COMPATIBLE'
WHERE bi.status = 'ISSUED'
  AND cr.id IS NULL
ORDER BY bi.created_at DESC
LIMIT 20;

-- Transfusions without matching blood issue
SELECT t.id, t.patient_id, t.blood_unit_id, t.transfusion_date
FROM transfusions t
LEFT JOIN blood_issues bi ON t.blood_issue_id = bi.id
WHERE bi.id IS NULL
ORDER BY t.transfusion_date DESC
LIMIT 20;
```

### 1c. Donor and Collection Integrity

```sql
-- Collections without a parent donor
SELECT c.id, c.collection_date, c.donor_id
FROM collections c
LEFT JOIN donors d ON c.donor_id = d.id
WHERE d.id IS NULL
ORDER BY c.collection_date DESC
LIMIT 20;

-- Multiple collections on same day for same donor (possible duplicate)
SELECT donor_id, DATE(collection_date) AS collection_day, COUNT(*) as count
FROM collections
GROUP BY donor_id, DATE(collection_date)
HAVING COUNT(*) > 1
ORDER BY count DESC
LIMIT 20;
```

### 1d. Audit Log Gap Detection

```sql
-- Check for gaps in audit log sequence (if using sequential IDs)
-- Replace with your actual audit log structure
SELECT id, created_at, action, entity_type, entity_id, created_by
FROM audit_logs
WHERE action IN ('BLOOD_UNIT_STATUS_CHANGE', 'CROSSMATCH_RESULT', 'BLOOD_ISSUE', 'TRANSFUSION')
  AND created_at >= now() - interval '24 hours'
ORDER BY created_at DESC
LIMIT 100;
```

---

## Step 2 — Classify the Corruption

| Type | Examples | Recovery Approach |
|---|---|---|
| **Stale cache serving wrong data** | Blood group shows wrong type in UI | Flush Redis cache (Step 3a) |
| **Race condition duplicate** | Two collections created for same donor | Soft-delete duplicate (Step 3b) |
| **Failed migration leaving inconsistency** | Foreign key violation | Revert migration (Step 3c) |
| **Application bug overwriting data** | Status set to wrong value | Roll back service + restore data (Step 3d) |
| **Manual SQL error in production** | Wrong update without WHERE clause | Restore from PITR backup (Step 3e) |
| **Clinical data mismatch (blood group)** | Patient's blood group incorrect | CRITICAL — immediate clinical escalation |

---

## Step 3 — Recovery Actions

> **Every action in this section requires Technical Lead written approval (Slack message or email with timestamp).**

### 3a. Flush Redis Cache

Safe action — can be taken immediately.

```bash
# Flush all cache (entire Redis)
kubectl exec -it -n bloodbank-prod redis-0 -- redis-cli FLUSHALL

# Or flush a specific key pattern (less disruptive)
kubectl exec -it -n bloodbank-prod redis-0 -- \
  redis-cli --scan --pattern "blood_unit:*" | xargs redis-cli DEL
```

Restart the affected service after cache flush:
```bash
kubectl rollout restart deployment/<SERVICE_NAME> -n bloodbank-prod
```

### 3b. Soft-Delete Duplicate Records

Never hard-delete records in production. Use soft-delete.

```sql
-- Before any update, verify the record to be deleted is truly a duplicate:
SELECT * FROM collections WHERE id IN ('<DUPLICATE_ID_1>', '<DUPLICATE_ID_2>');

-- Soft-delete the duplicate (keep audit trail)
UPDATE collections
SET deleted_at = now(),
    updated_at = now(),
    updated_by = 'INCIDENT-RECOVERY-<INCIDENT_ID>'
WHERE id = '<DUPLICATE_ID>'
  AND branch_id = '<BRANCH_UUID>'; -- Always scope to branch for safety
```

### 3c. Revert a Flyway Migration

```bash
# Identify the bad migration
kubectl exec -it -n bloodbank-prod postgresql-0 -- \
  psql -U bloodbank -d bloodbank_db -c "
    SELECT version, description, installed_on, success
    FROM flyway_schema_history
    ORDER BY installed_rank DESC
    LIMIT 10;"

# Revert requires running the inverse SQL manually (no Flyway undo in community edition)
# ALWAYS test the revert SQL in staging first
# Get Technical Lead and DBA approval before applying
```

### 3d. Restore Specific Records from PITR Backup

For targeted recovery without full restore:

```bash
# 1. Create a temporary database from the PITR backup
# (coordinate with DBA — this is a multi-hour operation for large databases)
pg_restore -d bloodbank_recovery -F c bloodbank_$(date +%Y%m%d).dump

# 2. Extract the affected records from the recovery database
psql -d bloodbank_recovery -c "
  SELECT * FROM blood_units WHERE id IN ('<AFFECTED_IDS>');" \
  > /tmp/recovery-data.csv

# 3. Review and approve the recovered data with Technical Lead
# 4. Import the recovered data into production with full audit trail
psql -d bloodbank_db -c "
  INSERT INTO blood_units (...)
  SELECT ... FROM staging.blood_units_recovery WHERE ...;"
```

### 3e. Full Point-in-Time Recovery (PITR)

Last resort — this rolls back ALL data to a specific point in time.

```
⚠️ REQUIRES L4 EXECUTIVE SPONSOR APPROVAL ⚠️
This will cause downtime and reverse all changes since the recovery point.
Estimated time: 30–120 minutes depending on database size.
```

```bash
# Calculate WAL position for desired recovery point
# Coordinate with DBA to configure recovery.conf:
# recovery_target_time = '2026-04-22 14:30:00 UTC'

# This is performed by the DBA following the PostgreSQL PITR procedure
# Document in incident log: who approved, target time, justification
```

---

## Step 4 — Validate Recovery

After any recovery action, run the same integrity queries from Step 1 to confirm:

```sql
-- Verify no corrupt records remain
-- Run all Step 1 queries and confirm zero rows returned

-- Verify audit log has an entry for the recovery action
SELECT * FROM audit_logs
WHERE action LIKE 'INCIDENT-RECOVERY%'
ORDER BY created_at DESC
LIMIT 10;
```

---

## Step 5 — Clinical Notification (If Clinical Records Affected)

If any of the following are affected, immediately notify the Clinical Lead (phone call):
- Blood group / type records
- Crossmatch results
- Transfusion records
- Active blood unit reservations or issues

The Clinical Lead must review all affected patient records and determine if any clinical decisions were made based on corrupted data. This may require:
- Repeat crossmatch tests
- Notification to treating physicians
- Adverse event reporting

---

## Step 6 — Post-Incident

1. **Root cause analysis**: document exactly what caused the corruption and how it was detected
2. **Impact assessment**: how many records were affected, for how long
3. **Audit trail**: verify the audit log captures all recovery actions
4. **Regulatory notification**: if patient safety was affected, notify as required by:
   - FDA MedWatch (medical device adverse event)
   - Local blood safety authority
5. **Corrective action**: code fix, migration safeguard, or process change
6. **Regression test**: add a test that would have caught this corruption
7. **Post-Incident Review** within 24 hours: `docs/operations/incident-response.md`

---

## Reference

| Resource | URL |
|---|---|
| Incident response | `docs/operations/incident-response.md` |
| HIPAA validation | `docs/compliance/hipaa-validation.md` |
| AABB standards | `docs/compliance/aabb-validation.md` |
| FDA 21 CFR Part 11 | `docs/compliance/fda-21cfr11-validation.md` |
| On-call guide | `docs/operations/on-call-guide.md` |
