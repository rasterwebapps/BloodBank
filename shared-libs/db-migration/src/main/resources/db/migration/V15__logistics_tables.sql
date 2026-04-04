-- =============================================
-- V15: Logistics Tables
-- transport_requests, cold_chain_logs, transport_boxes, delivery_confirmations
-- =============================================

-- Transport Boxes (reusable transport containers)
CREATE TABLE transport_boxes (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    box_code            VARCHAR(50)     NOT NULL UNIQUE,
    box_type            VARCHAR(30)     NOT NULL, -- INSULATED, REFRIGERATED, FROZEN, AMBIENT
    capacity            INT,            -- number of units
    temperature_range   VARCHAR(50),    -- e.g., "2-6°C"
    status              VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, IN_USE, MAINTENANCE, RETIRED
    last_sanitized      TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Transport Requests
CREATE TABLE transport_requests (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    request_number          VARCHAR(30)     NOT NULL UNIQUE,
    source_branch_id        UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    destination_branch_id   UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    destination_hospital_id UUID            REFERENCES hospitals(id) ON DELETE RESTRICT,
    transport_box_id        UUID            REFERENCES transport_boxes(id) ON DELETE RESTRICT,
    transport_type          VARCHAR(30)     NOT NULL, -- ROUTINE, URGENT, EMERGENCY
    units_count             INT             NOT NULL DEFAULT 0,
    pickup_time             TIMESTAMPTZ,
    expected_delivery_time  TIMESTAMPTZ,
    actual_delivery_time    TIMESTAMPTZ,
    driver_name             VARCHAR(200),
    driver_contact          VARCHAR(20),
    vehicle_number          VARCHAR(50),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, APPROVED, DISPATCHED, IN_TRANSIT, DELIVERED, CANCELLED
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Cold Chain Logs (temperature monitoring during transport and storage)
CREATE TABLE cold_chain_logs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    transport_request_id UUID           REFERENCES transport_requests(id) ON DELETE RESTRICT,
    storage_location_id UUID            REFERENCES storage_locations(id) ON DELETE RESTRICT,
    transport_box_id    UUID            REFERENCES transport_boxes(id) ON DELETE RESTRICT,
    temperature         DECIMAL(5,2)    NOT NULL,
    humidity            DECIMAL(5,2),
    recorded_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    is_within_range     BOOLEAN         NOT NULL DEFAULT TRUE,
    alert_triggered     BOOLEAN         NOT NULL DEFAULT FALSE,
    recorded_by         VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Delivery Confirmations
CREATE TABLE delivery_confirmations (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    transport_request_id    UUID            NOT NULL REFERENCES transport_requests(id) ON DELETE RESTRICT,
    received_by             VARCHAR(200)    NOT NULL,
    received_at             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    condition_on_arrival    VARCHAR(30)     NOT NULL, -- GOOD, ACCEPTABLE, DAMAGED, REJECTED
    temperature_on_arrival  DECIMAL(5,2),
    units_received          INT             NOT NULL DEFAULT 0,
    units_rejected          INT             NOT NULL DEFAULT 0,
    rejection_reason        VARCHAR(500),
    signature_reference     VARCHAR(255),
    notes                   TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
