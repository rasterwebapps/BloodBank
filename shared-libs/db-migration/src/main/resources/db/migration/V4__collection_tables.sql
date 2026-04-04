-- =============================================
-- V4: Collection Tables
-- collections, collection_adverse_reactions, collection_samples
-- =============================================

-- Collections (Blood Donations)
CREATE TABLE collections (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    donor_id                UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    health_record_id        UUID            REFERENCES donor_health_records(id) ON DELETE RESTRICT,
    collection_number       VARCHAR(30)     NOT NULL UNIQUE,
    collection_date         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    collection_type         VARCHAR(30)     NOT NULL, -- WHOLE_BLOOD, APHERESIS_PLATELET, APHERESIS_PLASMA, APHERESIS_RBC, AUTOLOGOUS
    donation_type           VARCHAR(30)     NOT NULL DEFAULT 'VOLUNTARY', -- VOLUNTARY, REPLACEMENT, DIRECTED, AUTOLOGOUS
    volume_ml               INT,
    bag_type                VARCHAR(50),    -- SINGLE, DOUBLE, TRIPLE, QUADRUPLE
    bag_lot_number          VARCHAR(100),
    phlebotomist_id         VARCHAR(255),
    start_time              TIMESTAMPTZ,
    end_time                TIMESTAMPTZ,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, INCOMPLETE, DISCARDED
    notes                   TEXT,
    camp_collection_id      UUID,           -- FK added in V5
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Collection Adverse Reactions
CREATE TABLE collection_adverse_reactions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    collection_id       UUID            NOT NULL REFERENCES collections(id) ON DELETE RESTRICT,
    reaction_type       VARCHAR(50)     NOT NULL, -- VASOVAGAL, HEMATOMA, NERVE_INJURY, ARTERIAL_PUNCTURE, DELAYED_BLEEDING, OTHER
    severity            VARCHAR(20)     NOT NULL, -- MILD, MODERATE, SEVERE
    onset_time          TIMESTAMPTZ,
    description         TEXT,
    treatment_given     TEXT,
    outcome             VARCHAR(30),    -- RESOLVED, REFERRED, HOSPITALIZED
    reported_by         VARCHAR(255),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Collection Samples (tubes taken for testing)
CREATE TABLE collection_samples (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    collection_id       UUID            NOT NULL REFERENCES collections(id) ON DELETE RESTRICT,
    sample_number       VARCHAR(30)     NOT NULL UNIQUE,
    sample_type         VARCHAR(30)     NOT NULL, -- EDTA, SERUM, CLOTTED, CITRATE
    collected_at        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    status              VARCHAR(20)     NOT NULL DEFAULT 'COLLECTED', -- COLLECTED, SENT_TO_LAB, TESTED, DISCARDED
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);
