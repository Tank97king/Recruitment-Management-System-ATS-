-- =============================================================================
--  ATS System — Versioned Migration V1
--  Author  : ATS Dev Team
--  Purpose : Create roles table and seed initial default roles.
-- =============================================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Seed initial roles (ADMIN and RECRUITER)
INSERT INTO roles (id, name, description, created_at, updated_at, created_by, updated_by)
VALUES 
('d144e590-7d72-460d-83ab-e8b0b92a2a0a', 'ADMIN', 'Full system access', NOW(), NOW(), 'system', 'system'),
('c3b313d4-bcf2-4752-b88a-2cbfebcd5d8d', 'RECRUITER', 'Recruitment operations access', NOW(), NOW(), 'system', 'system');
