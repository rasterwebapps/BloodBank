-- =============================================
-- V10: Billing Tables
-- rate_master, invoices, invoice_line_items, payments, credit_notes
-- =============================================

-- Rate Master (pricing for blood components and services)
CREATE TABLE rate_master (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    component_type_id   UUID            REFERENCES component_types(id) ON DELETE RESTRICT,
    service_code        VARCHAR(50)     NOT NULL,
    service_name        VARCHAR(200)    NOT NULL,
    rate_amount         DECIMAL(12,2)   NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    tax_percentage      DECIMAL(5,2)    NOT NULL DEFAULT 0,
    effective_from      DATE            NOT NULL,
    effective_to        DATE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    UNIQUE (branch_id, service_code, effective_from)
);

-- Invoices
CREATE TABLE invoices (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    hospital_id         UUID            NOT NULL REFERENCES hospitals(id) ON DELETE RESTRICT,
    invoice_number      VARCHAR(30)     NOT NULL UNIQUE,
    invoice_date        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    due_date            DATE            NOT NULL,
    subtotal            DECIMAL(12,2)   NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12,2)   NOT NULL DEFAULT 0,
    amount_paid         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    balance_due         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT', -- DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, VOID
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Invoice Line Items
CREATE TABLE invoice_line_items (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    invoice_id          UUID            NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    blood_issue_id      UUID            REFERENCES blood_issues(id) ON DELETE RESTRICT,
    rate_id             UUID            REFERENCES rate_master(id) ON DELETE RESTRICT,
    description         VARCHAR(500)    NOT NULL,
    quantity            INT             NOT NULL DEFAULT 1,
    unit_price          DECIMAL(12,2)   NOT NULL,
    tax_percentage      DECIMAL(5,2)    NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(12,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    line_total          DECIMAL(12,2)   NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Payments
CREATE TABLE payments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    invoice_id          UUID            NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    payment_number      VARCHAR(30)     NOT NULL UNIQUE,
    payment_date        TIMESTAMPTZ     NOT NULL DEFAULT now(),
    amount              DECIMAL(12,2)   NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    payment_method      VARCHAR(30)     NOT NULL, -- CASH, BANK_TRANSFER, CHECK, CREDIT_CARD, INSURANCE, GOVERNMENT_SUBSIDY
    reference_number    VARCHAR(100),
    status              VARCHAR(20)     NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, PENDING, FAILED, REFUNDED
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Credit Notes
CREATE TABLE credit_notes (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID            NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    invoice_id          UUID            NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    credit_note_number  VARCHAR(30)     NOT NULL UNIQUE,
    credit_date         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    amount              DECIMAL(12,2)   NOT NULL,
    reason              VARCHAR(500)    NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ISSUED', -- ISSUED, APPLIED, VOID
    applied_to_invoice  UUID            REFERENCES invoices(id) ON DELETE RESTRICT,
    notes               TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0
);
