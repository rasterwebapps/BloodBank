# Data Migration Guide

**Last Updated**: 2026-04-21
**Milestone**: M10 — Pilot Deployment (1 Branch)
**Issue**: M10-002
**Status**: 🔴 NOT STARTED

---

## Overview

This guide covers the end-to-end data migration process for importing historical data from a pilot branch legacy system into the BloodBank production database. Migration is performed as a one-time bulk import during the scheduled maintenance window.

**Database**: PostgreSQL 17 — `bloodbank_db`
**Flyway migrations**: managed by `shared-libs/db-migration/`

---

## 1. Pre-Migration Requirements

### Tools Required

| Tool | Version | Purpose |
|---|---|---|
| `psql` | 17+ | Direct DB access |
| `pg_dump` / `pg_restore` | 17+ | Backup and restore |
| Python | 3.11+ | Data transformation scripts |
| `csvkit` | 1.3+ | CSV inspection and validation |

### Access Requirements

- Production DB write access (migration service account only)
- Read access to legacy export files on secure transfer server
- Keycloak admin console access (for creating user accounts)

### Backup Before Migration

```bash
# Take a full backup immediately before migration starts
pg_dump \
  --host=$PROD_DB_HOST \
  --port=5432 \
  --username=$PROD_DB_USER \
  --dbname=bloodbank_db \
  --format=custom \
  --file=/backups/pre-migration-$(date +%Y%m%d-%H%M%S).dump

# Verify backup
pg_restore --list /backups/pre-migration-*.dump | head -20
```

---

## 2. Historical Donor Data Migration

### 2.1 Legacy Export Format

The legacy system exports donor data as CSV with the following columns:

```
legacy_id, first_name, last_name, date_of_birth, gender,
blood_group, email, phone, address, city, registration_date,
last_donation_date, deferral_status, deferral_reason, deferral_end_date
```

### 2.2 Transformation Rules

| Legacy Field | Target Table / Column | Transformation |
|---|---|---|
| `legacy_id` | `donors.legacy_id` (VARCHAR) | Store as-is for traceability |
| `blood_group` | `donors.blood_group` | Map to `BloodGroupEnum`: `A+` → `A_POSITIVE`, etc. |
| `deferral_status` | `donor_deferrals` row | If `DEFERRED`, create deferral record |
| `registration_date` | `donors.created_at` | Parse as `TIMESTAMP WITH TIME ZONE` (assume branch TZ) |
| `last_donation_date` | `collections.collection_date` | Create synthetic collection record if date present |

### 2.3 Blood Group Mapping

| Legacy Value | BloodGroupEnum Value |
|---|---|
| `A+` | `A_POSITIVE` |
| `A-` | `A_NEGATIVE` |
| `B+` | `B_POSITIVE` |
| `B-` | `B_NEGATIVE` |
| `AB+` | `AB_POSITIVE` |
| `AB-` | `AB_NEGATIVE` |
| `O+` | `O_POSITIVE` |
| `O-` | `O_NEGATIVE` |

### 2.4 Import Steps

```bash
# Step 1: Copy legacy export to migration server
scp legacy-donors.csv migration@prod-server:/tmp/migration/

# Step 2: Inspect the file
csvstat /tmp/migration/legacy-donors.csv

# Step 3: Run transformation script
python3 scripts/transform-donors.py \
  --input /tmp/migration/legacy-donors.csv \
  --output /tmp/migration/donors-transformed.sql \
  --branch-id <PILOT_BRANCH_UUID>

# Step 4: Review first 20 lines of generated SQL
head -40 /tmp/migration/donors-transformed.sql

# Step 5: Dry run (transaction rollback)
psql -h $PROD_DB_HOST -U $MIGRATION_USER -d bloodbank_db \
  -c "BEGIN;" \
  -f /tmp/migration/donors-transformed.sql \
  -c "SELECT COUNT(*) FROM donors WHERE branch_id = '<PILOT_BRANCH_UUID>';" \
  -c "ROLLBACK;"

# Step 6: Execute for real
psql -h $PROD_DB_HOST -U $MIGRATION_USER -d bloodbank_db \
  -c "BEGIN;" \
  -f /tmp/migration/donors-transformed.sql \
  -c "COMMIT;"
```

### 2.5 Donor Deferral Migration

Deferred donors must have a corresponding row in `donor_deferrals`:

```sql
-- Create deferral records for all imported donors with deferral_status = 'DEFERRED'
INSERT INTO donor_deferrals (
    id, donor_id, branch_id, deferral_reason_id,
    deferral_date, end_date, notes,
    created_at, updated_at, created_by, updated_by, version
)
SELECT
    gen_random_uuid(),
    d.id,
    d.branch_id,
    dr.id,
    d.deferral_start_date,
    d.deferral_end_date,
    'Migrated from legacy system',
    NOW(), NOW(), 'MIGRATION', 'MIGRATION', 0
FROM donors d
JOIN deferral_reasons dr ON dr.code = d.legacy_deferral_reason
WHERE d.deferral_status = 'DEFERRED'
  AND d.branch_id = '<PILOT_BRANCH_UUID>';
```

---

## 3. Inventory Data Migration

### 3.1 Legacy Export Format

The legacy inventory export is a CSV with the following columns:

```
unit_code, blood_group, component_type, collection_date,
expiry_date, volume_ml, status, storage_location, donor_legacy_id
```

### 3.2 Transformation Rules

| Legacy Field | Target Table / Column | Transformation |
|---|---|---|
| `unit_code` | `blood_units.unit_code` | Store as-is |
| `component_type` | `blood_units.component_type_id` | Look up `component_types.code` |
| `status` | `blood_units.status` | Map: `AVAILABLE`→`AVAILABLE`, `RESERVED`→`RESERVED`, `EXPIRED`→`DISCARDED` |
| `donor_legacy_id` | `blood_units.donor_id` | Look up newly imported donor by `legacy_id` |
| `expiry_date` | `blood_units.expiry_date` | Skip units already expired at migration date |

### 3.3 Import Steps

```bash
# Step 1: Copy legacy inventory export
scp legacy-inventory.csv migration@prod-server:/tmp/migration/

# Step 2: Run transformation
python3 scripts/transform-inventory.py \
  --input /tmp/migration/legacy-inventory.csv \
  --output /tmp/migration/inventory-transformed.sql \
  --branch-id <PILOT_BRANCH_UUID>

# Step 3: Dry run
psql -h $PROD_DB_HOST -U $MIGRATION_USER -d bloodbank_db \
  -c "BEGIN;" \
  -f /tmp/migration/inventory-transformed.sql \
  -c "SELECT COUNT(*) FROM blood_units WHERE branch_id = '<PILOT_BRANCH_UUID>';" \
  -c "ROLLBACK;"

# Step 4: Execute
psql -h $PROD_DB_HOST -U $MIGRATION_USER -d bloodbank_db \
  -c "BEGIN;" \
  -f /tmp/migration/inventory-transformed.sql \
  -c "COMMIT;"
```

---

## 4. Data Validation Queries

Run all queries after migration. Every query must return 0 errors or match expected counts.

### 4.1 Record Count Validation

```sql
-- Total donors imported for the pilot branch
SELECT COUNT(*) AS donor_count
FROM donors
WHERE branch_id = '<PILOT_BRANCH_UUID>';
-- Expected: matches legacy system total ±1%

-- Total blood units imported
SELECT COUNT(*) AS unit_count, status
FROM blood_units
WHERE branch_id = '<PILOT_BRANCH_UUID>'
GROUP BY status;
-- Expected: AVAILABLE count matches legacy system exactly

-- Deferral records
SELECT COUNT(*) AS deferral_count
FROM donor_deferrals
WHERE branch_id = '<PILOT_BRANCH_UUID>';
-- Expected: matches count of legacy deferred donors
```

### 4.2 Duplicate Detection

```sql
-- Check for duplicate donor emails
SELECT email, COUNT(*) AS cnt
FROM donors
WHERE branch_id = '<PILOT_BRANCH_UUID>'
  AND email IS NOT NULL
GROUP BY email
HAVING COUNT(*) > 1;
-- Expected: 0 rows

-- Check for duplicate unit codes
SELECT unit_code, COUNT(*) AS cnt
FROM blood_units
WHERE branch_id = '<PILOT_BRANCH_UUID>'
GROUP BY unit_code
HAVING COUNT(*) > 1;
-- Expected: 0 rows
```

### 4.3 Referential Integrity

```sql
-- Blood units referencing non-existent donors
SELECT COUNT(*) AS orphan_units
FROM blood_units bu
LEFT JOIN donors d ON d.id = bu.donor_id
WHERE bu.branch_id = '<PILOT_BRANCH_UUID>'
  AND bu.donor_id IS NOT NULL
  AND d.id IS NULL;
-- Expected: 0

-- Deferrals referencing non-existent deferral reasons
SELECT COUNT(*) AS invalid_deferrals
FROM donor_deferrals dd
LEFT JOIN deferral_reasons dr ON dr.id = dd.deferral_reason_id
WHERE dd.branch_id = '<PILOT_BRANCH_UUID>'
  AND dr.id IS NULL;
-- Expected: 0
```

### 4.4 Data Quality Checks

```sql
-- Donors missing mandatory fields
SELECT COUNT(*) AS incomplete_donors
FROM donors
WHERE branch_id = '<PILOT_BRANCH_UUID>'
  AND (first_name IS NULL OR last_name IS NULL OR blood_group IS NULL);
-- Expected: 0

-- Blood units with invalid expiry (already expired but status = AVAILABLE)
SELECT COUNT(*) AS invalid_units
FROM blood_units
WHERE branch_id = '<PILOT_BRANCH_UUID>'
  AND status = 'AVAILABLE'
  AND expiry_date < NOW();
-- Expected: 0

-- Audit trail entries created for all imported donors
SELECT COUNT(*) AS donors_without_audit
FROM donors d
LEFT JOIN audit_logs al ON al.entity_id = d.id::TEXT
  AND al.action = 'IMPORT'
WHERE d.branch_id = '<PILOT_BRANCH_UUID>'
  AND al.id IS NULL;
-- Expected: 0
```

### 4.5 Blood Group Distribution Sanity Check

```sql
-- Blood group distribution (compare to expected population norms)
SELECT blood_group, COUNT(*) AS cnt,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 1) AS pct
FROM donors
WHERE branch_id = '<PILOT_BRANCH_UUID>'
GROUP BY blood_group
ORDER BY cnt DESC;
-- Review: O+ should be highest, AB- should be lowest
```

---

## 5. Rollback Procedure

If the go/no-go check fails, or a critical error is discovered after go-live, execute this rollback procedure:

### 5.1 Decision Criteria for Rollback

Roll back if ANY of the following:
- Validation queries show > 0.1% error rate
- Blood unit count mismatch of any amount
- Critical referential integrity violations
- Application unable to read imported records

### 5.2 Rollback Steps

```bash
# Step 1: Enable maintenance mode
kubectl set env deployment/api-gateway \
  MAINTENANCE_MODE=true \
  -n bloodbank-prod

# Step 2: Stop all writes (scale down services except api-gateway)
kubectl scale deployment --replicas=0 \
  donor-service inventory-service lab-service \
  transfusion-service hospital-service billing-service \
  -n bloodbank-prod

# Step 3: Restore database from pre-migration backup
pg_restore \
  --host=$PROD_DB_HOST \
  --port=5432 \
  --username=$PROD_DB_SUPERUSER \
  --dbname=bloodbank_db \
  --clean \
  --if-exists \
  /backups/pre-migration-<TIMESTAMP>.dump

# Step 4: Verify row counts match pre-migration state
psql -h $PROD_DB_HOST -U $PROD_DB_USER -d bloodbank_db \
  -c "SELECT schemaname, tablename, n_live_tup FROM pg_stat_user_tables ORDER BY tablename;"

# Step 5: Restart services
kubectl scale deployment --replicas=2 \
  donor-service inventory-service lab-service \
  transfusion-service hospital-service billing-service \
  -n bloodbank-prod

# Step 6: Disable maintenance mode
kubectl set env deployment/api-gateway \
  MAINTENANCE_MODE=false \
  -n bloodbank-prod

# Step 7: Verify health checks
kubectl get pods -n bloodbank-prod
curl -s https://bloodbank.example.com/actuator/health | jq .
```

### 5.3 Post-Rollback Actions

- [ ] Document the failure with timestamps and error details
- [ ] Notify Branch Management within 30 minutes of rollback decision
- [ ] Schedule root cause analysis meeting within 24 hours
- [ ] Update migration scripts to fix identified issues
- [ ] Schedule new migration window (minimum 1 week later)
- [ ] Re-run full dry-run on staging before next attempt

---

## 6. Post-Migration Cleanup

```bash
# Remove temporary migration files from server (HIPAA compliance)
rm -f /tmp/migration/legacy-donors.csv
rm -f /tmp/migration/legacy-inventory.csv
rm -f /tmp/migration/donors-transformed.sql
rm -f /tmp/migration/inventory-transformed.sql

# Revoke migration service account DB permissions
psql -h $PROD_DB_HOST -U $PROD_DB_SUPERUSER -d bloodbank_db \
  -c "REVOKE INSERT, UPDATE ON ALL TABLES IN SCHEMA public FROM migration_user;"

# Archive backup to cold storage (retain for 7 years per HIPAA)
aws s3 cp /backups/pre-migration-*.dump \
  s3://bloodbank-backups/pre-migration/ \
  --storage-class GLACIER
```
