-- =============================================================================
--  ATS System — Versioned Migration V7
--  Author  : ATS Dev Team
--  Purpose : Create interviews, interview_feedback tables, and date_time index.
-- =============================================================================

CREATE TABLE interviews (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    date_time TIMESTAMPTZ NOT NULL,
    type VARCHAR(50) NOT NULL,
    interviewer_name VARCHAR(255) NOT NULL,
    interviewer_email VARCHAR(255) NOT NULL,
    meeting_link VARCHAR(1000),
    location VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES job_applications (id)
);

CREATE TABLE interview_feedback (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL UNIQUE,
    submitted_by_user_id UUID NOT NULL,
    overall_rating INTEGER NOT NULL,
    technical_rating INTEGER,
    communication_rating INTEGER,
    cultural_fit_rating INTEGER,
    recommendation VARCHAR(20) NOT NULL,
    notes TEXT,
    submitted_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_feedback_interview FOREIGN KEY (interview_id) REFERENCES interviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_user FOREIGN KEY (submitted_by_user_id) REFERENCES users (id)
);

-- Index for interview scheduling lookups
CREATE INDEX idx_interviews_date_time ON interviews(date_time);
