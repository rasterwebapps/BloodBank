# /project:add-flyway-migration

Create a new Flyway migration script.

## Arguments

- `$ARGUMENTS` should be: a description of the migration (e.g., "add_donor_preferences_table")

## Steps

1. Read CLAUDE.md for Flyway migration rules
2. Check existing migrations in `shared-libs/db-migration/src/main/resources/db/migration/`
3. Determine the next version number (after V20, use V21, V22, etc.)
4. Create the migration file: `V{N}__{description}.sql`
5. Include all required columns:
   - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
   - `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
   - `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
   - `created_by VARCHAR(255)`
   - `updated_by VARCHAR(255)`
   - `version BIGINT NOT NULL DEFAULT 0`
6. If branch-scoped: add `branch_id UUID NOT NULL REFERENCES branches(id)`
7. Add appropriate indexes (branch_id, status, created_at, composites)
8. Use PostgreSQL 17 compatible syntax
9. Use `TIMESTAMPTZ` for all timestamp columns
10. Use `VARCHAR` for enum columns (matching `@Enumerated(EnumType.STRING)`)
