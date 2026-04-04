-- =============================================
-- V18: System Tables
-- system_settings, feature_flags, scheduled_jobs, tenant_configs
-- =============================================

-- System Settings (application-wide configuration)
CREATE TABLE system_settings (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key         VARCHAR(100)    NOT NULL UNIQUE,
    setting_value       TEXT            NOT NULL,
    value_type          VARCHAR(20)     NOT NULL DEFAULT 'STRING', -- STRING, INTEGER, BOOLEAN, JSON, ENCRYPTED
    category            VARCHAR(50)     NOT NULL, -- GENERAL, SECURITY, NOTIFICATION, BILLING, INVENTORY, LAB, COMPLIANCE
    description         VARCHAR(500),
    is_editable         BOOLEAN         NOT NULL DEFAULT TRUE,
    last_modified_by    VARCHAR(255),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Feature Flags
CREATE TABLE feature_flags (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_key            VARCHAR(100)    NOT NULL UNIQUE,
    flag_name           VARCHAR(200)    NOT NULL,
    description         VARCHAR(500),
    is_enabled          BOOLEAN         NOT NULL DEFAULT FALSE,
    applies_to          VARCHAR(30)     NOT NULL DEFAULT 'ALL', -- ALL, BRANCH_SPECIFIC, ROLE_SPECIFIC
    branch_ids          TEXT,           -- comma-separated branch UUIDs (when BRANCH_SPECIFIC)
    role_names          TEXT,           -- comma-separated roles (when ROLE_SPECIFIC)
    start_date          TIMESTAMPTZ,
    end_date            TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Scheduled Jobs (tracking background tasks)
CREATE TABLE scheduled_jobs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    job_name            VARCHAR(100)    NOT NULL UNIQUE,
    job_group           VARCHAR(50)     NOT NULL DEFAULT 'DEFAULT',
    job_class           VARCHAR(255)    NOT NULL,
    cron_expression     VARCHAR(100),
    description         VARCHAR(500),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    last_run_at         TIMESTAMPTZ,
    last_run_status     VARCHAR(20),    -- SUCCESS, FAILED, SKIPPED
    last_run_duration_ms BIGINT,
    next_run_at         TIMESTAMPTZ,
    failure_count       INT             NOT NULL DEFAULT 0,
    last_error_message  TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Tenant Configs (branch-specific configuration overrides)
CREATE TABLE tenant_configs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    config_key          VARCHAR(100)    NOT NULL,
    config_value        TEXT            NOT NULL,
    value_type          VARCHAR(20)     NOT NULL DEFAULT 'STRING', -- STRING, INTEGER, BOOLEAN, JSON
    description         VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (branch_id, config_key)
);
