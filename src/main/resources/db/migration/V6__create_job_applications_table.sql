-- =============================================================================
--  ATS System — Versioned Migration V6
--  Author  : ATS Dev Team
--  Purpose : Create job_applications, application_status_history tables, and status index.
-- =============================================================================

CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL,
    job_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    cover_letter TEXT,
    applied_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_job_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id),
    CONSTRAINT fk_job_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id),
    CONSTRAINT uq_job_applications_candidate_job UNIQUE (candidate_id, job_id)
);

CREATE TABLE application_status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    changed_by_user_id UUID NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    previous_stage VARCHAR(30),
    new_stage VARCHAR(30) NOT NULL,
    note TEXT,
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_app_history_application FOREIGN KEY (application_id) REFERENCES job_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id)
);

-- Index for application status checks
CREATE INDEX idx_job_applications_status ON job_applications(status);
