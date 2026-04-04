-- =============================================
-- V14: Document Tables
-- documents, document_versions
-- =============================================

-- Documents
CREATE TABLE documents (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    document_code       VARCHAR(50)     NOT NULL UNIQUE,
    document_name       VARCHAR(200)    NOT NULL,
    document_type       VARCHAR(50)     NOT NULL, -- DONOR_CONSENT, LAB_REPORT, BLOOD_REQUEST, INVOICE, SOP, LICENSE, CERTIFICATE, PHOTO, OTHER
    entity_type         VARCHAR(100),   -- related entity type (e.g., donors, collections)
    entity_id           UUID,           -- related entity ID
    mime_type           VARCHAR(100)    NOT NULL,
    file_size_bytes     BIGINT,
    storage_path        VARCHAR(500)    NOT NULL, -- MinIO object key
    storage_bucket      VARCHAR(100)    NOT NULL DEFAULT 'bloodbank-documents',
    description         VARCHAR(500),
    tags                VARCHAR(500),   -- comma-separated tags
    is_confidential     BOOLEAN         NOT NULL DEFAULT FALSE,
    uploaded_by         VARCHAR(255),
    current_version     INT             NOT NULL DEFAULT 1,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ARCHIVED, DELETED
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Document Versions
CREATE TABLE document_versions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            REFERENCES branches(id) ON DELETE RESTRICT,
    document_id         UUID            NOT NULL REFERENCES documents(id) ON DELETE RESTRICT,
    version_number      INT             NOT NULL,
    storage_path        VARCHAR(500)    NOT NULL,
    file_size_bytes     BIGINT,
    mime_type           VARCHAR(100)    NOT NULL,
    change_description  VARCHAR(500),
    uploaded_by         VARCHAR(255),
    uploaded_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (document_id, version_number)
);
