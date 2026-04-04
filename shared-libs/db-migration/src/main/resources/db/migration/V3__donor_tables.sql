-- =============================================
-- V3: Donor Tables
-- donors, donor_health_records, donor_deferrals, donor_consents, donor_loyalty
-- =============================================

-- Donors
CREATE TABLE donors (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_number        VARCHAR(30)     NOT NULL UNIQUE,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100)    NOT NULL,
    date_of_birth       DATE            NOT NULL,
    gender              VARCHAR(10)     NOT NULL, -- MALE, FEMALE, OTHER
    blood_group_id      UUID            REFERENCES blood_groups(id) ON DELETE RESTRICT,
    rh_factor           VARCHAR(10),    -- POSITIVE, NEGATIVE
    email               VARCHAR(255),
    phone               VARCHAR(20),
    address_line1       VARCHAR(255),
    address_line2       VARCHAR(255),
    city_id             UUID            REFERENCES cities(id) ON DELETE RESTRICT,
    postal_code         VARCHAR(20),
    national_id         VARCHAR(50),
    nationality         VARCHAR(50),
    occupation          VARCHAR(100),
    donor_type          VARCHAR(30)     NOT NULL DEFAULT 'VOLUNTARY', -- VOLUNTARY, REPLACEMENT, AUTOLOGOUS, DIRECTED
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DEFERRED, PERMANENTLY_DEFERRED, BLACKLISTED
    last_donation_date  DATE,
    total_donations     INT             NOT NULL DEFAULT 0,
    registration_date   DATE            NOT NULL DEFAULT CURRENT_DATE,
    photo_url           VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Donor Health Records (pre-donation screening)
CREATE TABLE donor_health_records (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id                UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    screening_date          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    weight_kg               DECIMAL(5,2),
    height_cm               DECIMAL(5,2),
    blood_pressure_systolic INT,
    blood_pressure_diastolic INT,
    pulse_rate              INT,
    temperature_celsius     DECIMAL(4,2),
    hemoglobin_gdl          DECIMAL(4,2),
    is_eligible             BOOLEAN         NOT NULL DEFAULT FALSE,
    notes                   TEXT,
    screened_by             VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Donor Deferrals
CREATE TABLE donor_deferrals (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    deferral_reason_id  UUID            NOT NULL REFERENCES deferral_reasons(id) ON DELETE RESTRICT,
    deferral_type       VARCHAR(20)     NOT NULL, -- TEMPORARY, PERMANENT
    deferral_date       DATE            NOT NULL DEFAULT CURRENT_DATE,
    reinstatement_date  DATE,
    notes               TEXT,
    deferred_by         VARCHAR(255),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, REINSTATED, EXPIRED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Donor Consents (GDPR & regulatory)
CREATE TABLE donor_consents (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    consent_type        VARCHAR(50)     NOT NULL, -- DONATION, DATA_PROCESSING, RESEARCH, CONTACT, EMERGENCY_RECALL
    consent_given       BOOLEAN         NOT NULL,
    consent_date        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    expiry_date         TIMESTAMPTZ,
    consent_text        TEXT,
    signature_reference VARCHAR(255),
    ip_address          VARCHAR(45),
    revoked_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Donor Loyalty / Rewards
CREATE TABLE donor_loyalty (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    points_earned       INT             NOT NULL DEFAULT 0,
    points_redeemed     INT             NOT NULL DEFAULT 0,
    points_balance      INT             NOT NULL DEFAULT 0,
    tier                VARCHAR(20)     NOT NULL DEFAULT 'BRONZE', -- BRONZE, SILVER, GOLD, PLATINUM
    last_activity_date  TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (donor_id)
);
