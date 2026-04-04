-- =============================================
-- V5: Blood Camp Tables
-- blood_camps, camp_resources, camp_donors, camp_collections
-- =============================================

-- Blood Camps (mobile donation drives)
CREATE TABLE blood_camps (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    camp_code           VARCHAR(30)     NOT NULL UNIQUE,
    camp_name           VARCHAR(200)    NOT NULL,
    organizer_name      VARCHAR(200),
    organizer_contact   VARCHAR(100),
    venue_name          VARCHAR(200)    NOT NULL,
    venue_address       VARCHAR(500)    NOT NULL,
    city_id             UUID            REFERENCES cities(id) ON DELETE RESTRICT,
    latitude            DECIMAL(10,7),
    longitude           DECIMAL(10,7),
    scheduled_date      DATE            NOT NULL,
    start_time          TIMESTAMPTZ,
    end_time            TIMESTAMPTZ,
    expected_donors     INT,
    actual_donors       INT,
    total_collected     INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PLANNED', -- PLANNED, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    coordinator_id      VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Camp Resources (equipment, staff, supplies allocated to a camp)
CREATE TABLE camp_resources (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    camp_id             UUID            NOT NULL REFERENCES blood_camps(id) ON DELETE RESTRICT,
    resource_type       VARCHAR(30)     NOT NULL, -- STAFF, EQUIPMENT, SUPPLY, VEHICLE
    resource_name       VARCHAR(200)    NOT NULL,
    quantity            INT             NOT NULL DEFAULT 1,
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Camp Donors (donors registered for a camp)
CREATE TABLE camp_donors (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    camp_id             UUID            NOT NULL REFERENCES blood_camps(id) ON DELETE RESTRICT,
    donor_id            UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    registration_time   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    status              VARCHAR(20)     NOT NULL DEFAULT 'REGISTERED', -- REGISTERED, SCREENED, DONATED, DEFERRED, NO_SHOW
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (camp_id, donor_id)
);

-- Camp Collections (link between camp and collection records)
CREATE TABLE camp_collections (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    camp_id             UUID            NOT NULL REFERENCES blood_camps(id) ON DELETE RESTRICT,
    collection_id       UUID            NOT NULL REFERENCES collections(id) ON DELETE RESTRICT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (camp_id, collection_id)
);

-- Add FK from collections to camp_collections
ALTER TABLE collections
    ADD CONSTRAINT fk_collections_camp_collection
    FOREIGN KEY (camp_collection_id) REFERENCES camp_collections(id) ON DELETE RESTRICT;
