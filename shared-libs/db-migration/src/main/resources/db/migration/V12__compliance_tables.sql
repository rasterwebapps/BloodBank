-- =============================================
-- V12: Compliance Tables
-- regulatory_frameworks, sop_documents, licenses, deviations, recall_records
-- =============================================

-- Regulatory Frameworks
CREATE TABLE regulatory_frameworks (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    framework_code      VARCHAR(50)     NOT NULL UNIQUE,
    framework_name      VARCHAR(200)    NOT NULL,
    authority_name      VARCHAR(200),
    country_id          UUID            REFERENCES countries(id) ON DELETE RESTRICT,
    description         TEXT,
    effective_date      DATE,
    version_number      VARCHAR(20),
    document_url        VARCHAR(500),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- SOP Documents (Standard Operating Procedures)
CREATE TABLE sop_documents (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    sop_code            VARCHAR(50)     NOT NULL UNIQUE,
    sop_title           VARCHAR(200)    NOT NULL,
    category            VARCHAR(50)     NOT NULL, -- COLLECTION, TESTING, PROCESSING, STORAGE, DISTRIBUTION, TRANSFUSION, QUALITY, SAFETY
    framework_id        UUID            REFERENCES regulatory_frameworks(id) ON DELETE RESTRICT,
    version_number      VARCHAR(20)     NOT NULL DEFAULT '1.0',
    effective_date      DATE            NOT NULL,
    review_date         DATE,
    approved_by         VARCHAR(255),
    approved_at         TIMESTAMPTZ,
    document_url        VARCHAR(500),
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT', -- DRAFT, REVIEW, APPROVED, SUPERSEDED, RETIRED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Licenses (branch and equipment licenses)
CREATE TABLE licenses (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    license_type        VARCHAR(50)     NOT NULL, -- BLOOD_BANK, LABORATORY, STORAGE, TRANSPORT, EQUIPMENT
    license_number      VARCHAR(100)    NOT NULL,
    issuing_authority   VARCHAR(200)    NOT NULL,
    issue_date          DATE            NOT NULL,
    expiry_date         DATE            NOT NULL,
    renewal_date        DATE,
    scope               TEXT,
    document_url        VARCHAR(500),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, SUSPENDED, REVOKED, PENDING_RENEWAL
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Deviations (non-conformances, errors, near-misses)
CREATE TABLE deviations (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    deviation_number        VARCHAR(30)     NOT NULL UNIQUE,
    deviation_type          VARCHAR(30)     NOT NULL, -- NON_CONFORMANCE, ERROR, NEAR_MISS, COMPLAINT, ADVERSE_EVENT
    severity                VARCHAR(20)     NOT NULL, -- MINOR, MAJOR, CRITICAL
    category                VARCHAR(50)     NOT NULL, -- COLLECTION, TESTING, PROCESSING, STORAGE, DISTRIBUTION, TRANSFUSION, DOCUMENTATION, OTHER
    title                   VARCHAR(200)    NOT NULL,
    description             TEXT            NOT NULL,
    detected_date           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    detected_by             VARCHAR(255),
    root_cause              TEXT,
    corrective_action       TEXT,
    preventive_action       TEXT,
    sop_reference_id        UUID            REFERENCES sop_documents(id) ON DELETE RESTRICT,
    closure_date            TIMESTAMPTZ,
    closed_by               VARCHAR(255),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'OPEN', -- OPEN, UNDER_INVESTIGATION, CORRECTIVE_ACTION, CLOSED, REOPENED
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Recall Records
CREATE TABLE recall_records (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    recall_number           VARCHAR(30)     NOT NULL UNIQUE,
    recall_type             VARCHAR(30)     NOT NULL, -- PRODUCT_RECALL, DONOR_RECALL, LOOK_BACK
    recall_reason           VARCHAR(500)    NOT NULL,
    severity                VARCHAR(20)     NOT NULL, -- CLASS_I, CLASS_II, CLASS_III
    initiated_date          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    initiated_by            VARCHAR(255),
    affected_units_count    INT             NOT NULL DEFAULT 0,
    units_recovered         INT             NOT NULL DEFAULT 0,
    units_transfused        INT             NOT NULL DEFAULT 0,
    notification_sent       BOOLEAN         NOT NULL DEFAULT FALSE,
    lookback_investigation_id UUID          REFERENCES lookback_investigations(id) ON DELETE RESTRICT,
    closure_date            TIMESTAMPTZ,
    closed_by               VARCHAR(255),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'INITIATED', -- INITIATED, IN_PROGRESS, COMPLETED, CLOSED
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
