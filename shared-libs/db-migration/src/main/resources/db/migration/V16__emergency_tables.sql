-- =============================================
-- V16: Emergency Tables
-- emergency_requests, disaster_events, donor_mobilizations
-- =============================================

-- Emergency Requests (urgent blood needs)
CREATE TABLE emergency_requests (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    request_number          VARCHAR(30)     NOT NULL UNIQUE,
    hospital_id             UUID            REFERENCES hospitals(id) ON DELETE RESTRICT,
    blood_group_id          UUID            NOT NULL REFERENCES blood_groups(id) ON DELETE RESTRICT,
    component_type_id       UUID            NOT NULL REFERENCES component_types(id) ON DELETE RESTRICT,
    units_needed            INT             NOT NULL,
    units_fulfilled         INT             NOT NULL DEFAULT 0,
    priority                VARCHAR(20)     NOT NULL DEFAULT 'EMERGENCY', -- EMERGENCY, CRITICAL, MASS_CASUALTY
    patient_name            VARCHAR(200),
    clinical_summary        TEXT,
    requesting_doctor       VARCHAR(200),
    required_by             TIMESTAMPTZ     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'OPEN', -- OPEN, PARTIALLY_FULFILLED, FULFILLED, CANCELLED, EXPIRED
    broadcast_sent          BOOLEAN         NOT NULL DEFAULT FALSE,
    disaster_event_id       UUID,           -- FK added below
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Disaster Events
CREATE TABLE disaster_events (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    event_code              VARCHAR(30)     NOT NULL UNIQUE,
    event_name              VARCHAR(200)    NOT NULL,
    event_type              VARCHAR(30)     NOT NULL, -- NATURAL_DISASTER, MASS_CASUALTY, EPIDEMIC, CONFLICT, INDUSTRIAL_ACCIDENT, OTHER
    severity                VARCHAR(20)     NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    location_description    VARCHAR(500),
    city_id                 UUID            REFERENCES cities(id) ON DELETE RESTRICT,
    start_date              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    end_date                TIMESTAMPTZ,
    estimated_casualties    INT,
    blood_units_needed      INT,
    coordinator_name        VARCHAR(200),
    coordinator_contact     VARCHAR(100),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ESCALATED, CONTROLLED, CLOSED
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Add FK from emergency_requests to disaster_events
ALTER TABLE emergency_requests
    ADD CONSTRAINT fk_emergency_requests_disaster_event
    FOREIGN KEY (disaster_event_id) REFERENCES disaster_events(id) ON DELETE RESTRICT;

-- Donor Mobilizations (emergency donor call-ups)
CREATE TABLE donor_mobilizations (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    disaster_event_id       UUID            REFERENCES disaster_events(id) ON DELETE RESTRICT,
    emergency_request_id    UUID            REFERENCES emergency_requests(id) ON DELETE RESTRICT,
    donor_id                UUID            NOT NULL REFERENCES donors(id) ON DELETE RESTRICT,
    contact_method          VARCHAR(20)     NOT NULL, -- SMS, PHONE, EMAIL, PUSH, WHATSAPP
    contacted_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    response                VARCHAR(20),    -- ACCEPTED, DECLINED, NO_RESPONSE
    response_at             TIMESTAMPTZ,
    scheduled_donation_time TIMESTAMPTZ,
    donation_completed      BOOLEAN         NOT NULL DEFAULT FALSE,
    collection_id           UUID            REFERENCES collections(id) ON DELETE RESTRICT,
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
