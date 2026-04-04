# Skill: Create Flyway Migration Script

Generate Flyway SQL migration scripts following BloodBank patterns.

## Rules

1. All migrations go in `shared-libs/db-migration/src/main/resources/db/migration/`
2. Naming: `V{N}__{description}.sql` (double underscore)
3. Target database: PostgreSQL 17
4. All tables include audit columns from `BaseEntity`
5. Branch-scoped tables include `branch_id UUID NOT NULL`
6. Use `UUID` for all primary keys (generated as `gen_random_uuid()`)
7. Use `TIMESTAMPTZ` for all date/time columns
8. All enums stored as `VARCHAR` (matching `@Enumerated(EnumType.STRING)`)
9. Append-only tables (audit_logs) have INSERT-only trigger

## Base Columns (Every Table)

```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
created_by VARCHAR(255),
updated_by VARCHAR(255),
version BIGINT NOT NULL DEFAULT 0
```

## Branch-Scoped Additional Column

```sql
branch_id UUID NOT NULL REFERENCES branches(id),
```

## Template — Branch-Scoped Table

```sql
-- V{N}__{description}.sql

CREATE TABLE {table_name} (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL REFERENCES branches(id),

    -- Domain-specific columns
    {column_name} VARCHAR(255) NOT NULL,
    {status_column} VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    {date_column} TIMESTAMPTZ,
    {fk_column} UUID REFERENCES {related_table}(id),
    {numeric_column} DECIMAL(15, 2) NOT NULL DEFAULT 0,
    {boolean_column} BOOLEAN NOT NULL DEFAULT FALSE,
    {text_column} TEXT,

    -- Audit columns
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_{table_name}_branch_id ON {table_name}(branch_id);
CREATE INDEX idx_{table_name}_status ON {table_name}({status_column});
CREATE INDEX idx_{table_name}_created_at ON {table_name}(created_at);
CREATE INDEX idx_{table_name}_branch_status ON {table_name}(branch_id, {status_column});
```

## Template — Global Table (No Branch Scoping)

```sql
CREATE TABLE {table_name} (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0
);
```

## Existing Migration Scripts (V1–V20)

| Script | Description |
|---|---|
| V1 | Base master tables (blood_groups, component_types, countries, etc.) |
| V2 | Branch tables |
| V3 | Donor tables |
| V4 | Collection tables |
| V5 | Blood camp tables |
| V6 | Lab tables |
| V7 | Inventory tables |
| V8 | Transfusion tables |
| V9 | Hospital tables |
| V10 | Billing tables |
| V11 | Notification tables |
| V12 | Compliance tables |
| V13 | Reporting tables (audit_logs append-only trigger) |
| V14 | Document tables |
| V15 | Logistics tables |
| V16 | Emergency tables |
| V17 | User management tables |
| V18 | System tables |
| V19 | All indexes |
| V20 | Seed reference data |

## Conventions

- Foreign keys: `{referenced_table_singular}_id` (e.g., `donor_id`, `branch_id`)
- Status columns: `VARCHAR(50)` with default value
- Money columns: `DECIMAL(15, 2)`
- Names: `VARCHAR(255)`
- Codes/identifiers: `VARCHAR(50)`
- Phone numbers: `VARCHAR(20)`
- Email: `VARCHAR(255)`
- Notes/descriptions: `TEXT`
- New migrations after V20 should be `V21__`, `V22__`, etc.

## Validation

- [ ] Script in `shared-libs/db-migration/src/main/resources/db/migration/`
- [ ] Correct naming: `V{N}__{description}.sql`
- [ ] All tables have audit columns (id, created_at, updated_at, etc.)
- [ ] Branch-scoped tables have `branch_id` with FK to `branches`
- [ ] Appropriate indexes created
- [ ] PostgreSQL 17 compatible syntax
- [ ] UUIDs with `gen_random_uuid()`
- [ ] `TIMESTAMPTZ` for all timestamps
