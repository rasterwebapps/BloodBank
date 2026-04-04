-- =============================================
-- V7: Inventory Tables
-- blood_units, blood_components, component_processing, component_labels,
-- pooled_components, storage_locations, stock_transfers, unit_disposals, unit_reservations
-- =============================================

-- Storage Locations (fridges, freezers, platelet agitators, etc.)
CREATE TABLE storage_locations (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    location_code       VARCHAR(50)     NOT NULL,
    location_name       VARCHAR(200)    NOT NULL,
    location_type       VARCHAR(30)     NOT NULL, -- REFRIGERATOR, FREEZER, PLATELET_AGITATOR, QUARANTINE, SHELF
    temperature_min     DECIMAL(5,2),
    temperature_max     DECIMAL(5,2),
    capacity            INT,
    current_count       INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, FULL, MAINTENANCE, INACTIVE
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (branch_id, location_code)
);

-- Blood Units (the parent unit from a collection)
CREATE TABLE blood_units (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    collection_id       UUID            NOT NULL REFERENCES collections(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    unit_number         VARCHAR(30)     NOT NULL UNIQUE,
    blood_group_id      UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    rh_factor           VARCHAR(10)     NOT NULL, -- POSITIVE, NEGATIVE
    volume_ml           INT,
    collection_date     TIMESTAMPTZ     NOT NULL,
    expiry_date         TIMESTAMPTZ     NOT NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'QUARANTINE', -- QUARANTINE, AVAILABLE, RESERVED, ISSUED, TRANSFUSED, DISCARDED, EXPIRED, RECALLED
    storage_location_id UUID            REFERENCES storage_locations(id) ON DELETE RESTRICT,
    tti_status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, NEGATIVE, POSITIVE, INDETERMINATE
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Blood Components (derived from blood_units: RBC, FFP, Platelets, etc.)
CREATE TABLE blood_components (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    blood_unit_id           UUID            NOT NULL REFERENCES blood_units(id) ON DELETE RESTRICT,
    component_type_id       UUID            NOT NULL REFERENCES component_types(id) ON DELETE RESTRICT,
    component_number        VARCHAR(30)     NOT NULL UNIQUE,
    blood_group_id          UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    volume_ml               INT,
    weight_grams            DECIMAL(7,2),
    preparation_date        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    expiry_date             TIMESTAMPTZ     NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'QUARANTINE', -- QUARANTINE, AVAILABLE, RESERVED, ISSUED, TRANSFUSED, DISCARDED, EXPIRED, POOLED
    storage_location_id     UUID            REFERENCES storage_locations(id) ON DELETE RESTRICT,
    irradiated              BOOLEAN         NOT NULL DEFAULT FALSE,
    leukoreduced            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Component Processing (tracks processing steps)
CREATE TABLE component_processing (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    component_id            UUID            NOT NULL REFERENCES blood_components(id) ON DELETE RESTRICT,
    process_type            VARCHAR(50)     NOT NULL, -- SEPARATION, LEUKOREDUCTION, IRRADIATION, PATHOGEN_INACTIVATION, WASHING, VOLUME_REDUCTION
    process_date            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    processed_by            VARCHAR(255),
    equipment_used          VARCHAR(200),
    parameters              TEXT,           -- JSON or key-value details
    result                  VARCHAR(20)     NOT NULL, -- SUCCESS, FAILED
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Component Labels
CREATE TABLE component_labels (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    component_id        UUID            NOT NULL REFERENCES blood_components(id) ON DELETE RESTRICT,
    label_type          VARCHAR(30)     NOT NULL, -- ISBT128, CUSTOM, BARCODE, QR
    label_data          TEXT            NOT NULL,
    printed_at          TIMESTAMPTZ,
    printed_by          VARCHAR(255),
    reprint_count       INT             NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Pooled Components (e.g., pooled platelets from multiple donors)
CREATE TABLE pooled_components (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    pool_number             VARCHAR(30)     NOT NULL UNIQUE,
    component_type_id       UUID            NOT NULL REFERENCES component_types(id) ON DELETE RESTRICT,
    blood_group_id          UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    total_volume_ml         INT,
    number_of_units         INT             NOT NULL,
    preparation_date        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    expiry_date             TIMESTAMPTZ     NOT NULL,
    status                  VARCHAR(30)     NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, RESERVED, ISSUED, DISCARDED, EXPIRED
    storage_location_id     UUID            REFERENCES storage_locations(id) ON DELETE RESTRICT,
    prepared_by             VARCHAR(255),
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Stock Transfers (between branches)
CREATE TABLE stock_transfers (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT, -- source branch
    source_branch_id        UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    destination_branch_id   UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    transfer_number         VARCHAR(30)     NOT NULL UNIQUE,
    component_id            UUID            REFERENCES blood_components(id) ON DELETE RESTRICT,
    pooled_component_id     UUID            REFERENCES pooled_components(id) ON DELETE RESTRICT,
    request_date            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    shipped_date            TIMESTAMPTZ,
    received_date           TIMESTAMPTZ,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, APPROVED, SHIPPED, IN_TRANSIT, RECEIVED, REJECTED, CANCELLED
    requested_by            VARCHAR(255),
    approved_by             VARCHAR(255),
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Unit Disposals (reasons for discarding blood units/components)
CREATE TABLE unit_disposals (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    blood_unit_id       UUID            REFERENCES blood_units(id) ON DELETE RESTRICT,
    component_id        UUID            REFERENCES blood_components(id) ON DELETE RESTRICT,
    disposal_reason     VARCHAR(50)     NOT NULL, -- EXPIRED, TTI_POSITIVE, DAMAGED, CONTAMINATED, QC_FAILURE, RECALLED, OTHER
    disposal_date       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    disposed_by         VARCHAR(255),
    authorization_by    VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Unit Reservations (temporary hold for crossmatch or request)
CREATE TABLE unit_reservations (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    component_id        UUID            NOT NULL REFERENCES blood_components(id) ON DELETE RESTRICT,
    reserved_for        VARCHAR(255)    NOT NULL, -- patient name or request reference
    reservation_date    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    expiry_date         TIMESTAMPTZ     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, RELEASED, ISSUED, EXPIRED, CANCELLED
    reserved_by         VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);
