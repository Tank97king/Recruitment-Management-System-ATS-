-- =============================================================================
--  ATS System — Versioned Migration V4
--  Author  : ATS Dev Team
--  Purpose : Create jobs table and status index.
-- =============================================================================

CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    employment_type VARCHAR(50) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    salary_min NUMERIC(19, 2),
    salary_max NUMERIC(19, 2),
    deadline DATE,
    status VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id)
);

-- Index for job status lookups
CREATE INDEX idx_jobs_status ON jobs(status);
