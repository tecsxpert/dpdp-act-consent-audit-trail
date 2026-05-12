-- ============================================================
-- V2 — Audit log table
-- Tracks every CREATE / UPDATE / DELETE
-- Column names match AuditLog entity exactly
-- ============================================================

CREATE TABLE audit_log (
    id              BIGSERIAL       PRIMARY KEY,
    entity_name     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT          NOT NULL,
    action          VARCHAR(50)     NOT NULL
                        CHECK (action IN (
                            'CREATE',
                            'UPDATE',
                            'DELETE'
                        )),
    changed_fields  TEXT,
    performed_by    VARCHAR(200),
    performed_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    ip_address      VARCHAR(50)
);

-- indexes matching @Index declarations in AuditLog entity
CREATE INDEX idx_audit_entity
    ON audit_log (entity_name, entity_id);

CREATE INDEX idx_audit_performed
    ON audit_log (performed_by);

CREATE INDEX idx_audit_action
    ON audit_log (action);