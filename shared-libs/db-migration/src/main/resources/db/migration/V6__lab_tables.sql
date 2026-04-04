-- =============================================
-- V6: Lab Tables
-- test_orders, test_results, test_panels, lab_instruments, quality_control_records
-- =============================================

-- Test Panels (groups of tests: TTI panel, blood grouping panel, etc.)
CREATE TABLE test_panels (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    panel_code          VARCHAR(50)     NOT NULL UNIQUE,
    panel_name          VARCHAR(200)    NOT NULL,
    description         VARCHAR(500),
    test_names          TEXT,           -- comma-separated list of tests in the panel
    is_mandatory        BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Lab Instruments
CREATE TABLE lab_instruments (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    instrument_code         VARCHAR(50)     NOT NULL UNIQUE,
    instrument_name         VARCHAR(200)    NOT NULL,
    instrument_type         VARCHAR(50)     NOT NULL, -- ANALYZER, CENTRIFUGE, INCUBATOR, READER, OTHER
    manufacturer            VARCHAR(200),
    model                   VARCHAR(100),
    serial_number           VARCHAR(100),
    installation_date       DATE,
    last_calibration_date   DATE,
    next_calibration_date   DATE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, UNDER_MAINTENANCE, RETIRED
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Test Orders
CREATE TABLE test_orders (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    sample_id           UUID            NOT NULL REFERENCES collection_samples(id) ON DELETE RESTRICT,
    collection_id       UUID            NOT NULL REFERENCES collections(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    panel_id            UUID            REFERENCES test_panels(id) ON DELETE RESTRICT,
    order_number        VARCHAR(30)     NOT NULL UNIQUE,
    order_date          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    priority            VARCHAR(20)     NOT NULL DEFAULT 'ROUTINE', -- ROUTINE, URGENT, STAT
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    ordered_by          VARCHAR(255),
    completed_at        TIMESTAMPTZ,
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Test Results
CREATE TABLE test_results (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    test_order_id       UUID            NOT NULL REFERENCES test_orders(id) ON DELETE RESTRICT,
    test_name           VARCHAR(100)    NOT NULL, -- HIV, HBV, HCV, SYPHILIS, MALARIA, BLOOD_GROUP, RH_TYPE, ANTIBODY_SCREEN, etc.
    test_method         VARCHAR(100),
    result_value        VARCHAR(255),
    result_status       VARCHAR(20)     NOT NULL, -- REACTIVE, NON_REACTIVE, POSITIVE, NEGATIVE, INDETERMINATE, INVALID
    is_abnormal         BOOLEAN         NOT NULL DEFAULT FALSE,
    unit_of_measure     VARCHAR(50),
    reference_range     VARCHAR(100),
    instrument_id       UUID            REFERENCES lab_instruments(id) ON DELETE RESTRICT,
    tested_by           VARCHAR(255),
    verified_by         VARCHAR(255),
    tested_at           TIMESTAMPTZ,
    verified_at         TIMESTAMPTZ,
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Quality Control Records
CREATE TABLE quality_control_records (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    instrument_id           UUID            NOT NULL REFERENCES lab_instruments(id) ON DELETE RESTRICT,
    qc_date                 TIMESTAMPTZ     NOT NULL DEFAULT now(),
    qc_level                VARCHAR(20)     NOT NULL, -- LOW, NORMAL, HIGH
    test_name               VARCHAR(100)    NOT NULL,
    expected_value          VARCHAR(100),
    actual_value            VARCHAR(100),
    is_within_range         BOOLEAN         NOT NULL,
    corrective_action       TEXT,
    performed_by            VARCHAR(255),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, FAILED, CORRECTIVE_ACTION_TAKEN
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
