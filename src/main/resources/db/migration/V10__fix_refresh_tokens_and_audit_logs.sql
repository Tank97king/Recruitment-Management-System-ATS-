-- =============================================================================
--  ATS System — Versioned Migration V10
--  Author  : ATS Dev Team
--  Purpose : Migration fix to align refresh_tokens and audit_logs columns with JPA entities.
-- =============================================================================

DO $$
BEGIN
    -- Fix refresh_tokens table columns
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'expiry_date') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'expires_at') THEN
            ALTER TABLE refresh_tokens RENAME COLUMN expiry_date TO expires_at;
        ELSE
            ALTER TABLE refresh_tokens DROP COLUMN expiry_date;
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'revoked') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'is_revoked') THEN
            ALTER TABLE refresh_tokens RENAME COLUMN revoked TO is_revoked;
        ELSE
            ALTER TABLE refresh_tokens DROP COLUMN revoked;
        END IF;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'revoked_at') THEN
        ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMPTZ;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'device_info') THEN
        ALTER TABLE refresh_tokens ADD COLUMN device_info VARCHAR(500);
    END IF;

    -- Fix audit_logs table columns
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'timestamp') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'created_at') THEN
            ALTER TABLE audit_logs RENAME COLUMN timestamp TO created_at;
        ELSE
            ALTER TABLE audit_logs DROP COLUMN timestamp;
        END IF;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'ip_address') THEN
        ALTER TABLE audit_logs ADD COLUMN ip_address VARCHAR(45);
    END IF;
END $$;
