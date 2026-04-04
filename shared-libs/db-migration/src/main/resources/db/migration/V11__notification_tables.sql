-- =============================================
-- V11: Notification Tables
-- notifications, notification_templates, notification_preferences, campaigns
-- =============================================

-- Notification Templates
CREATE TABLE notification_templates (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code       VARCHAR(50)     NOT NULL UNIQUE,
    template_name       VARCHAR(200)    NOT NULL,
    channel             VARCHAR(20)     NOT NULL, -- EMAIL, SMS, PUSH, IN_APP, WHATSAPP
    subject             VARCHAR(500),
    body_template       TEXT            NOT NULL,
    variables           TEXT,           -- comma-separated list of template variables
    language            VARCHAR(10)     NOT NULL DEFAULT 'en',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Notifications
CREATE TABLE notifications (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    template_id             UUID            REFERENCES notification_templates(id) ON DELETE RESTRICT,
    recipient_type          VARCHAR(20)     NOT NULL, -- DONOR, STAFF, HOSPITAL, SYSTEM
    recipient_id            UUID,
    recipient_email         VARCHAR(255),
    recipient_phone         VARCHAR(20),
    channel                 VARCHAR(20)     NOT NULL, -- EMAIL, SMS, PUSH, IN_APP, WHATSAPP
    subject                 VARCHAR(500),
    body                    TEXT            NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, DELIVERED, FAILED, READ
    sent_at                 TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    read_at                 TIMESTAMPTZ,
    failure_reason          VARCHAR(500),
    retry_count             INT             NOT NULL DEFAULT 0,
    external_reference      VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Notification Preferences
CREATE TABLE notification_preferences (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    channel             VARCHAR(20)     NOT NULL, -- EMAIL, SMS, PUSH, IN_APP, WHATSAPP
    event_type          VARCHAR(50)     NOT NULL, -- DONATION_REMINDER, APPOINTMENT_CONFIRMATION, RESULT_READY, EMERGENCY_ALERT, STOCK_ALERT, etc.
    is_enabled          BOOLEAN         NOT NULL DEFAULT TRUE,
    quiet_hours_start   TIME,
    quiet_hours_end     TIME,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (user_id, channel, event_type)
);

-- Campaigns (bulk notification campaigns: donation drives, emergency appeals)
CREATE TABLE campaigns (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id               UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    campaign_code           VARCHAR(50)     NOT NULL UNIQUE,
    campaign_name           VARCHAR(200)    NOT NULL,
    campaign_type           VARCHAR(30)     NOT NULL, -- DONATION_DRIVE, EMERGENCY_APPEAL, AWARENESS, RECALL, REMINDER
    channel                 VARCHAR(20)     NOT NULL, -- EMAIL, SMS, PUSH, IN_APP, WHATSAPP, MULTI_CHANNEL
    target_audience         VARCHAR(50),    -- ALL_DONORS, BLOOD_GROUP_SPECIFIC, AREA_SPECIFIC, LAPSED_DONORS
    target_criteria         TEXT,           -- JSON criteria for audience selection
    template_id             UUID            REFERENCES notification_templates(id) ON DELETE RESTRICT,
    scheduled_at            TIMESTAMPTZ,
    started_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    total_recipients        INT             NOT NULL DEFAULT 0,
    sent_count              INT             NOT NULL DEFAULT 0,
    delivered_count         INT             NOT NULL DEFAULT 0,
    failed_count            INT             NOT NULL DEFAULT 0,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'DRAFT', -- DRAFT, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, PAUSED
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0
);
