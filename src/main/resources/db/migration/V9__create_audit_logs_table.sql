-- =============================================================================
--  ATS System — Versioned Migration V9
--  Author  : ATS Dev Team
--  Purpose : Create audit_logs table and timestamp index.
-- =============================================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    user_email VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255),
    description VARCHAR(1000),
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Index for temporal queries on audit history
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
