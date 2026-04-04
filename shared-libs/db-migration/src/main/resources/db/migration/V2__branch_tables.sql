-- =============================================
-- V2: Branch Tables
-- branches, branch_operating_hours, branch_equipment, branch_regions
-- =============================================

-- Branches
CREATE TABLE branches (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_code         VARCHAR(20)     NOT NULL UNIQUE,
    branch_name         VARCHAR(200)    NOT NULL,
    branch_type         VARCHAR(30)     NOT NULL, -- BLOOD_CENTER, HOSPITAL_BANK, COLLECTION_CENTER, MOBILE_UNIT
    address_line1       VARCHAR(255)    NOT NULL,
    address_line2       VARCHAR(255),
    city_id             UUID            REFERENCES cities(id) ON DELETE RESTRICT,
    postal_code         VARCHAR(20),
    phone               VARCHAR(20),
    email               VARCHAR(255),
    license_number      VARCHAR(100),
    license_expiry      DATE,
    latitude            DECIMAL(10,7),
    longitude           DECIMAL(10,7),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    parent_branch_id    UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Branch Operating Hours
CREATE TABLE branch_operating_hours (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    day_of_week     VARCHAR(10)     NOT NULL, -- MONDAY, TUESDAY, ...
    open_time       TIME            NOT NULL,
    close_time      TIME            NOT NULL,
    is_closed       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (branch_id, day_of_week)
);

-- Branch Equipment
CREATE TABLE branch_equipment (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    equipment_name          VARCHAR(200)    NOT NULL,
    equipment_type          VARCHAR(50)     NOT NULL, -- CENTRIFUGE, BLOOD_MIXER, REFRIGERATOR, FREEZER, PLATELET_AGITATOR
    serial_number           VARCHAR(100),
    manufacturer            VARCHAR(200),
    model                   VARCHAR(100),
    purchase_date           DATE,
    last_maintenance_date   DATE,
    next_maintenance_date   DATE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'OPERATIONAL', -- OPERATIONAL, UNDER_MAINTENANCE, OUT_OF_SERVICE
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Branch Regions (many-to-many: branches serving regions)
CREATE TABLE branch_regions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    region_id       UUID            NOT NULL REFERENCES regions(id) ON DELETE RESTRICT,
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (branch_id, region_id)
);
