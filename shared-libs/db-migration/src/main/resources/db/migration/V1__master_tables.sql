-- =============================================
-- V1: Master / Reference Tables
-- blood_groups, component_types, countries, regions, cities,
-- deferral_reasons, reaction_types, icd_codes
-- =============================================

-- Blood Groups (A+, A-, B+, B-, AB+, AB-, O+, O-)
CREATE TABLE blood_groups (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name      VARCHAR(10)     NOT NULL UNIQUE,
    description     VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0
);

-- Component Types (Whole Blood, Packed RBC, FFP, Platelets, Cryoprecipitate, etc.)
CREATE TABLE component_types (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    type_code           VARCHAR(50)     NOT NULL UNIQUE,
    type_name           VARCHAR(100)    NOT NULL,
    description         VARCHAR(500),
    shelf_life_days     INT             NOT NULL,
    storage_temp_min    DECIMAL(5,2),
    storage_temp_max    DECIMAL(5,2),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Countries
CREATE TABLE countries (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code    VARCHAR(3)      NOT NULL UNIQUE,
    country_name    VARCHAR(100)    NOT NULL,
    phone_code      VARCHAR(10),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0
);

-- Regions (States / Provinces)
CREATE TABLE regions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID            NOT NULL REFERENCES countries(id) ON DELETE RESTRICT,
    region_code     VARCHAR(10)     NOT NULL,
    region_name     VARCHAR(100)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (country_id, region_code)
);

-- Cities
CREATE TABLE cities (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    region_id       UUID            NOT NULL REFERENCES regions(id) ON DELETE RESTRICT,
    city_name       VARCHAR(100)    NOT NULL,
    postal_code     VARCHAR(20),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0
);

-- Deferral Reasons
CREATE TABLE deferral_reasons (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reason_code         VARCHAR(50)     NOT NULL UNIQUE,
    reason_description  VARCHAR(500)    NOT NULL,
    deferral_type       VARCHAR(20)     NOT NULL, -- TEMPORARY, PERMANENT
    default_days        INT,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Reaction Types (transfusion reactions)
CREATE TABLE reaction_types (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reaction_code       VARCHAR(50)     NOT NULL UNIQUE,
    reaction_name       VARCHAR(100)    NOT NULL,
    severity            VARCHAR(20)     NOT NULL, -- MILD, MODERATE, SEVERE, FATAL
    description         VARCHAR(500),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- ICD Codes (International Classification of Diseases)
CREATE TABLE icd_codes (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    icd_code        VARCHAR(20)     NOT NULL UNIQUE,
    description     VARCHAR(500)    NOT NULL,
    category        VARCHAR(100),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0
);
