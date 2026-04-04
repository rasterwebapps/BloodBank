-- =============================================
-- V8: Transfusion Tables
-- crossmatch_requests, crossmatch_results, blood_issues, emergency_issues,
-- transfusions, transfusion_reactions, hemovigilance_reports, lookback_investigations
-- =============================================

-- Crossmatch Requests
CREATE TABLE crossmatch_requests (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    request_number          VARCHAR(30)     NOT NULL UNIQUE,
    patient_name            VARCHAR(200)    NOT NULL,
    patient_id              VARCHAR(50),
    patient_blood_group_id  UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    hospital_id             UUID,           -- FK added in V9
    requesting_doctor       VARCHAR(200),
    clinical_diagnosis      TEXT,
    icd_code_id             UUID            REFERENCES icd_codes(id) ON DELETE RESTRICT,
    units_requested         INT             NOT NULL DEFAULT 1,
    component_type_id       UUID            NOT NULL REFERENCES component_types(id) ON DELETE RESTRICT,
    priority                VARCHAR(20)     NOT NULL DEFAULT 'ROUTINE', -- ROUTINE, URGENT, EMERGENCY
    required_by             TIMESTAMPTZ,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Crossmatch Results
CREATE TABLE crossmatch_results (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    crossmatch_request_id   UUID            NOT NULL REFERENCES crossmatch_requests(id) ON DELETE RESTRICT,
    component_id            UUID            NOT NULL REFERENCES blood_components(id) ON DELETE RESTRICT,
    crossmatch_method       VARCHAR(50)     NOT NULL, -- IMMEDIATE_SPIN, ANTIGLOBULIN, ELECTRONIC, GEL_CARD
    result                  VARCHAR(20)     NOT NULL, -- COMPATIBLE, INCOMPATIBLE, INDETERMINATE
    performed_by            VARCHAR(255),
    verified_by             VARCHAR(255),
    performed_at            TIMESTAMPTZ,
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Blood Issues (release of blood to patient / hospital)
CREATE TABLE blood_issues (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    issue_number            VARCHAR(30)     NOT NULL UNIQUE,
    crossmatch_request_id   UUID            REFERENCES crossmatch_requests(id) ON DELETE RESTRICT,
    component_id            UUID            NOT NULL REFERENCES blood_components(id) ON DELETE RESTRICT,
    patient_name            VARCHAR(200)    NOT NULL,
    patient_id              VARCHAR(50),
    hospital_id             UUID,           -- FK added in V9
    issued_to               VARCHAR(200),
    issued_by               VARCHAR(255),
    issue_date              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    return_date             TIMESTAMPTZ,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ISSUED', -- ISSUED, TRANSFUSED, RETURNED, WASTED
    return_reason           VARCHAR(255),
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Emergency Issues (blood issued before crossmatch — uncrossmatched emergency)
CREATE TABLE emergency_issues (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    blood_issue_id          UUID            NOT NULL REFERENCES blood_issues(id) ON DELETE RESTRICT,
    emergency_type          VARCHAR(30)     NOT NULL, -- MASSIVE_HEMORRHAGE, TRAUMA, OBSTETRIC, OTHER
    authorization_by        VARCHAR(255)    NOT NULL,
    authorization_time      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    clinical_justification  TEXT            NOT NULL,
    post_crossmatch_done    BOOLEAN         NOT NULL DEFAULT FALSE,
    post_crossmatch_result  VARCHAR(20),   -- COMPATIBLE, INCOMPATIBLE
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Transfusions
CREATE TABLE transfusions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    blood_issue_id          UUID            NOT NULL REFERENCES blood_issues(id) ON DELETE RESTRICT,
    patient_name            VARCHAR(200)    NOT NULL,
    patient_id              VARCHAR(50),
    hospital_id             UUID,           -- FK added in V9
    transfusion_start       TIMESTAMPTZ,
    transfusion_end         TIMESTAMPTZ,
    volume_transfused_ml    INT,
    administered_by         VARCHAR(255),
    verified_by             VARCHAR(255),
    pre_vital_signs         TEXT,           -- JSON: BP, pulse, temp
    post_vital_signs        TEXT,           -- JSON: BP, pulse, temp
    status                  VARCHAR(20)     NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, STOPPED, REACTION_OCCURRED
    outcome                 VARCHAR(20),    -- SUCCESSFUL, REACTION, INCOMPLETE
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Transfusion Reactions
CREATE TABLE transfusion_reactions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    transfusion_id          UUID            NOT NULL REFERENCES transfusions(id) ON DELETE RESTRICT,
    reaction_type_id        UUID            NOT NULL REFERENCES reaction_types(id) ON DELETE RESTRICT,
    onset_time              TIMESTAMPTZ     NOT NULL,
    symptoms                TEXT,
    severity                VARCHAR(20)     NOT NULL, -- MILD, MODERATE, SEVERE, FATAL
    treatment_given         TEXT,
    outcome                 VARCHAR(30)     NOT NULL, -- RESOLVED, HOSPITALIZED, PERMANENT_DAMAGE, DEATH
    reported_by             VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Hemovigilance Reports (formal adverse event reports)
CREATE TABLE hemovigilance_reports (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    transfusion_reaction_id UUID            NOT NULL REFERENCES transfusion_reactions(id) ON DELETE RESTRICT,
    report_number           VARCHAR(30)     NOT NULL UNIQUE,
    report_date             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    imputability            VARCHAR(20),    -- DEFINITE, PROBABLE, POSSIBLE, UNLIKELY, EXCLUDED
    reporter_name           VARCHAR(200),
    reporter_designation    VARCHAR(100),
    investigation_summary   TEXT,
    corrective_actions      TEXT,
    reported_to_authority   BOOLEAN         NOT NULL DEFAULT FALSE,
    authority_report_date   TIMESTAMPTZ,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'OPEN', -- OPEN, UNDER_INVESTIGATION, CLOSED
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Lookback Investigations (tracing recipients when donor later found positive)
CREATE TABLE lookback_investigations (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id                UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    trigger_test_result_id  UUID            REFERENCES test_results(id) ON DELETE RESTRICT,
    investigation_number    VARCHAR(30)     NOT NULL UNIQUE,
    investigation_date      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    infection_type          VARCHAR(50)     NOT NULL, -- HIV, HBV, HCV, SYPHILIS, OTHER
    affected_units_count    INT             NOT NULL DEFAULT 0,
    recipients_traced       INT             NOT NULL DEFAULT 0,
    recipients_notified     INT             NOT NULL DEFAULT 0,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'INITIATED', -- INITIATED, IN_PROGRESS, COMPLETED, CLOSED
    findings                TEXT,
    corrective_actions      TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
