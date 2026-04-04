-- =============================================
-- V17: User Management Tables
-- user_profiles, user_branch_assignments, user_activity_logs, user_sessions, role_change_audit
-- =============================================

-- User Profiles (extends Keycloak user data)
CREATE TABLE user_profiles (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_user_id    VARCHAR(255)    NOT NULL UNIQUE,
    username            VARCHAR(100)    NOT NULL UNIQUE,
    email               VARCHAR(255)    NOT NULL,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100)    NOT NULL,
    phone               VARCHAR(20),
    designation         VARCHAR(100),
    department          VARCHAR(100),
    employee_id         VARCHAR(50),
    profile_photo_url   VARCHAR(500),
    preferred_language  VARCHAR(10)     NOT NULL DEFAULT 'en',
    timezone            VARCHAR(50)     NOT NULL DEFAULT 'UTC',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- User Branch Assignments (which branches a user can access)
CREATE TABLE user_branch_assignments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id     UUID            NOT NULL REFERENCES user_profiles(id) ON DELETE RESTRICT,
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    role_name           VARCHAR(50)     NOT NULL,
    is_primary          BOOLEAN         NOT NULL DEFAULT FALSE,
    assigned_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    assigned_by         VARCHAR(255),
    expires_at          TIMESTAMPTZ,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (user_profile_id, branch_id, role_name)
);

-- User Activity Logs (user actions tracking)
CREATE TABLE user_activity_logs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id     UUID            NOT NULL REFERENCES user_profiles(id) ON DELETE RESTRICT,
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    activity_type       VARCHAR(50)     NOT NULL, -- LOGIN, LOGOUT, PAGE_VIEW, ACTION, EXPORT, REPORT_GENERATED
    activity_detail     VARCHAR(500),
    ip_address          VARCHAR(45),
    user_agent          VARCHAR(500),
    session_id          VARCHAR(255),
    timestamp           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- User Sessions
CREATE TABLE user_sessions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id     UUID            NOT NULL REFERENCES user_profiles(id) ON DELETE RESTRICT,
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    session_token       VARCHAR(500)    NOT NULL,
    ip_address          VARCHAR(45),
    user_agent          VARCHAR(500),
    login_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    last_activity_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    logout_at           TIMESTAMPTZ,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Role Change Audit
CREATE TABLE role_change_audit (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id     UUID            NOT NULL REFERENCES user_profiles(id) ON DELETE RESTRICT,
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    previous_role       VARCHAR(50),
    new_role            VARCHAR(50)     NOT NULL,
    change_reason       VARCHAR(500),
    changed_by          VARCHAR(255)    NOT NULL,
    changed_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    approved_by         VARCHAR(255),
    approved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);
