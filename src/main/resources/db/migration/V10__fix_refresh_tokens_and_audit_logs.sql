-- =============================================================================
--  ATS System — Versioned Migration V10
--  Author  : ATS Dev Team
--  Purpose : Migration fix to align refresh_tokens and audit_logs columns with JPA entities.
-- =============================================================================

-- Fix refresh_tokens table columns if created by old V8
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'expiry_date') THEN
        ALTER TABLE refresh_tokens RENAME COLUMN expiry_date TO expires_at;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'revoked') THEN
        ALTER TABLE refresh_tokens RENAME COLUMN revoked TO is_revoked;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'revoked_at') THEN
        ALTER TABLE refresh_tokens ADD COLUMN revoked_at TIMESTAMPTZ;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'device_info') THEN
        ALTER TABLE refresh_tokens ADD COLUMN device_info VARCHAR(500);
    END IF;
END $$;

-- Fix audit_logs table columns if created by old V9
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'timestamp') THEN
        ALTER TABLE audit_logs RENAME COLUMN timestamp TO created_at;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'ip_address') THEN
        ALTER TABLE audit_logs ADD COLUMN ip_address VARCHAR(45);
    END IF;
END $$;
