-- ============================================================
-- V1 — Initial schema
-- users, user_roles, consent_records
-- Column names match JPA entities exactly
-- ============================================================

-- ── users ────────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    full_name   VARCHAR(150)    NOT NULL,
    email       VARCHAR(200)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_user_email ON users (email);

-- ── user_roles ───────────────────────────────────────────────
CREATE TABLE user_roles (
    user_id     BIGINT          NOT NULL,
    role        VARCHAR(50)     NOT NULL,

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_role
        UNIQUE (user_id, role)
);

-- ── consent_records ──────────────────────────────────────────
CREATE TABLE consent_records (
    id                  BIGSERIAL       PRIMARY KEY,

    -- subject
    subject_name        VARCHAR(150)    NOT NULL,
    subject_email       VARCHAR(200)    NOT NULL,

    -- consent details
    purpose             VARCHAR(300)    NOT NULL,
    data_categories     VARCHAR(500),
    legal_basis         VARCHAR(100),
    status              VARCHAR(30)     NOT NULL
                            CHECK (status IN (
                                'ACTIVE',
                                'WITHDRAWN',
                                'EXPIRED',
                                'PENDING',
                                'REVOKED'
                            )),

    -- dates
    consent_date        DATE,
    expiry_date         DATE,
    withdrawal_date     DATE,
    deleted_at          DATE,

    -- AI fields
    ai_description      TEXT,
    ai_recommendations  TEXT,
    ai_report           TEXT,
    ai_processed        BOOLEAN         NOT NULL DEFAULT FALSE,

    -- soft delete
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,

    -- extra
    notes               TEXT,
    collected_by        VARCHAR(150),
    version             BIGINT          NOT NULL DEFAULT 0,

    -- audit (BaseEntity)
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- indexes matching @Index declarations in ConsentRecord entity
CREATE INDEX idx_consent_subject_email
    ON consent_records (subject_email);

CREATE INDEX idx_consent_status
    ON consent_records (status);

CREATE INDEX idx_consent_purpose
    ON consent_records (purpose);

CREATE INDEX idx_consent_deleted
    ON consent_records (deleted);

-- composite index for expiry scheduler query
CREATE INDEX idx_consent_expiry
    ON consent_records (expiry_date, status, deleted);