-- =============================================================================
--  ATS System — Versioned Migration V3
--  Author  : ATS Dev Team
--  Purpose : Create companies table and company name index.
-- =============================================================================

CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),
    headquarters_location VARCHAR(255),
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Index for company name searches
CREATE INDEX idx_companies_name ON companies(name);
