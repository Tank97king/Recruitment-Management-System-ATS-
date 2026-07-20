-- =============================================================================
--  ATS System — Versioned Migration V5
--  Author  : ATS Dev Team
--  Purpose : Create candidates, candidate_cvs, candidate_tags tables, and email index.
-- =============================================================================

CREATE TABLE candidates (
    id UUID PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    address VARCHAR(500) NOT NULL,
    years_of_experience INTEGER NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE candidate_cvs (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_candidate_cvs_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE
);

CREATE TABLE candidate_tags (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL,
    tag VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_candidate_tags_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE,
    CONSTRAINT uq_candidate_tags_candidate_id_tag UNIQUE (candidate_id, tag)
);

-- Index for candidate email lookups
CREATE INDEX idx_candidates_email ON candidates(email);
