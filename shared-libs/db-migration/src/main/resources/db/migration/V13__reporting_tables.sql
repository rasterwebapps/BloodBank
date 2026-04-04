-- =============================================
-- V13: Reporting Tables
-- audit_logs (append-only), digital_signatures, chain_of_custody,
-- report_metadata, report_schedules, dashboard_widgets
-- =============================================

-- Audit Logs (APPEND-ONLY — no updates or deletes allowed)
CREATE TABLE audit_logs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    entity_type         VARCHAR(100)    NOT NULL,
    entity_id           UUID            NOT NULL,
    action              VARCHAR(20)     NOT NULL, -- CREATE, UPDATE, DELETE, READ, LOGIN, LOGOUT, EXPORT
    actor_id            VARCHAR(255)    NOT NULL,
    actor_name          VARCHAR(200),
    actor_role          VARCHAR(50),
    actor_ip            VARCHAR(45),
    old_values          TEXT,           -- JSON of previous state
    new_values          TEXT,           -- JSON of new state
    description         VARCHAR(500),
    timestamp           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Trigger to prevent UPDATE on audit_logs (append-only)
CREATE OR REPLACE FUNCTION prevent_audit_log_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs table is append-only. UPDATE and DELETE operations are not permitted.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_logs_prevent_update
    BEFORE UPDATE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_log_modification();

CREATE TRIGGER audit_logs_prevent_delete
    BEFORE DELETE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION prevent_audit_log_modification();

-- Digital Signatures (FDA 21 CFR Part 11 compliance)
CREATE TABLE digital_signatures (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    entity_type         VARCHAR(100)    NOT NULL,
    entity_id           UUID            NOT NULL,
    signer_id           VARCHAR(255)    NOT NULL,
    signer_name         VARCHAR(200)    NOT NULL,
    signer_role         VARCHAR(50),
    signature_meaning   VARCHAR(100)    NOT NULL, -- AUTHORED, REVIEWED, APPROVED, VERIFIED, WITNESSED
    signature_hash      VARCHAR(512)    NOT NULL,
    signature_algorithm VARCHAR(50)     NOT NULL DEFAULT 'SHA-256',
    signed_at           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    ip_address          VARCHAR(45),
    is_valid            BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Chain of Custody (vein-to-vein traceability)
CREATE TABLE chain_of_custody (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    entity_type         VARCHAR(50)     NOT NULL, -- BLOOD_UNIT, COMPONENT, SAMPLE
    entity_id           UUID            NOT NULL,
    custody_event       VARCHAR(50)     NOT NULL, -- COLLECTED, RECEIVED, STORED, PROCESSED, TESTED, RELEASED, ISSUED, TRANSPORTED, TRANSFUSED, DISPOSED
    from_location       VARCHAR(200),
    to_location         VARCHAR(200),
    handled_by          VARCHAR(255)    NOT NULL,
    temperature         DECIMAL(5,2),
    event_time          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Report Metadata
CREATE TABLE report_metadata (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    report_code         VARCHAR(50)     NOT NULL UNIQUE,
    report_name         VARCHAR(200)    NOT NULL,
    report_type         VARCHAR(30)     NOT NULL, -- OPERATIONAL, STATISTICAL, REGULATORY, FINANCIAL, AUDIT, CUSTOM
    description         VARCHAR(500),
    query_definition    TEXT,
    parameters          TEXT,           -- JSON parameter definitions
    output_format       VARCHAR(20)     NOT NULL DEFAULT 'PDF', -- PDF, EXCEL, CSV, HTML
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Report Schedules
CREATE TABLE report_schedules (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    report_id           UUID            NOT NULL REFERENCES report_metadata(id) ON DELETE RESTRICT,
    schedule_name       VARCHAR(200)    NOT NULL,
    cron_expression     VARCHAR(100)    NOT NULL,
    recipients          TEXT,           -- comma-separated email addresses
    parameters          TEXT,           -- JSON runtime parameters
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    last_run_at         TIMESTAMPTZ,
    next_run_at         TIMESTAMPTZ,
    last_run_status     VARCHAR(20),    -- SUCCESS, FAILED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Dashboard Widgets
CREATE TABLE dashboard_widgets (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    widget_code         VARCHAR(50)     NOT NULL,
    widget_name         VARCHAR(200)    NOT NULL,
    widget_type         VARCHAR(30)     NOT NULL, -- CHART, TABLE, COUNTER, GAUGE, MAP, LIST
    data_source         VARCHAR(100)    NOT NULL,
    query_definition    TEXT,
    display_config      TEXT,           -- JSON display configuration
    refresh_interval_sec INT            NOT NULL DEFAULT 300,
    role_access         VARCHAR(500),   -- comma-separated roles
    sort_order          INT             NOT NULL DEFAULT 0,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);
