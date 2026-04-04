-- =============================================
-- V9: Hospital Tables
-- hospitals, hospital_contracts, hospital_requests, hospital_feedback
-- =============================================

-- Hospitals
CREATE TABLE hospitals (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    hospital_code       VARCHAR(30)     NOT NULL UNIQUE,
    hospital_name       VARCHAR(200)    NOT NULL,
    hospital_type       VARCHAR(30)     NOT NULL, -- GOVERNMENT, PRIVATE, MILITARY, NGO, TEACHING
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    city_id             UUID            REFERENCES cities(id) ON DELETE RESTRICT,
    postal_code         VARCHAR(20),
    phone               VARCHAR(20),
    email               VARCHAR(255),
    contact_person      VARCHAR(200),
    license_number      VARCHAR(100),
    bed_count           INT,
    has_blood_bank      BOOLEAN         NOT NULL DEFAULT FALSE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Hospital Contracts
CREATE TABLE hospital_contracts (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    hospital_id         UUID            NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    contract_number     VARCHAR(50)     NOT NULL UNIQUE,
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    discount_percentage DECIMAL(5,2)    NOT NULL DEFAULT 0,
    payment_terms_days  INT             NOT NULL DEFAULT 30,
    credit_limit        DECIMAL(12,2),
    auto_renew          BOOLEAN         NOT NULL DEFAULT FALSE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, TERMINATED, PENDING
    terms_document_url  VARCHAR(500),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Hospital Requests (blood requests from hospitals)
CREATE TABLE hospital_requests (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    hospital_id             UUID            NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    request_number          VARCHAR(30)     NOT NULL UNIQUE,
    patient_name            VARCHAR(200)    NOT NULL,
    patient_id              VARCHAR(50),
    patient_blood_group_id  UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    component_type_id       UUID            NOT NULL REFERENCES component_types(id) ON DELETE RESTRICT,
    units_requested         INT             NOT NULL DEFAULT 1,
    priority                VARCHAR(20)     NOT NULL DEFAULT 'ROUTINE', -- ROUTINE, URGENT, EMERGENCY
    required_by             TIMESTAMPTZ,
    clinical_indication     TEXT,
    icd_code_id             UUID            REFERENCES icd_codes(id) ON DELETE RESTRICT,
    requesting_doctor       VARCHAR(200),
    doctor_license          VARCHAR(50),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PARTIALLY_FULFILLED, FULFILLED, REJECTED, CANCELLED
    units_fulfilled         INT             NOT NULL DEFAULT 0,
    rejection_reason        VARCHAR(500),
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Hospital Feedback
CREATE TABLE hospital_feedback (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    hospital_id         UUID            NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    request_id          UUID            REFERENCES hospital_requests(id) ON DELETE RESTRICT,
    feedback_date       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    rating              INT             CHECK (rating BETWEEN 1 AND 5),
    category            VARCHAR(30)     NOT NULL, -- SERVICE_QUALITY, DELIVERY_TIME, PRODUCT_QUALITY, COMMUNICATION, OTHER
    comments            TEXT,
    response            TEXT,
    responded_by        VARCHAR(255),
    responded_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Add FK from crossmatch_requests to hospitals
ALTER TABLE crossmatch_requests
    ADD CONSTRAINT fk_crossmatch_requests_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE RESTRICT;

-- Add FK from blood_issues to hospitals
ALTER TABLE blood_issues
    ADD CONSTRAINT fk_blood_issues_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE RESTRICT;

-- Add FK from transfusions to hospitals
ALTER TABLE transfusions
    ADD CONSTRAINT fk_transfusions_hospital
    FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE RESTRICT;
