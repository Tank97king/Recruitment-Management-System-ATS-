package com.example.ats.enums;

/**
 * Lifecycle status of a job posting.
 *
 * <p>Job status follows a strict state machine enforced at the service layer.
 * Only valid transitions are permitted:
 *
 * <pre>
 *   DRAFT ──► OPEN ──► CLOSED
 *     │                  ▲
 *     └──────────────────┘
 *   DRAFT ──► CANCELLED
 *   OPEN  ──► CANCELLED
 * </pre>
 *
 * <p>Invalid transitions (e.g., CLOSED → OPEN, CANCELLED → OPEN) are
 * rejected with a {@code BusinessRuleViolationException}.
 */
public enum JobStatus {

    /**
     * Job has been created but is not yet visible for applications.
     * Recruiter is still editing details. Default state on creation.
     */
    DRAFT,

    /**
     * Job is published and actively accepting applications.
     * Candidates can be submitted to this job.
     */
    OPEN,

    /**
     * Job is no longer accepting applications. The position has been
     * filled or the application deadline has passed.
     */
    CLOSED,

    /**
     * Job requisition was withdrawn before being filled.
     * Distinct from CLOSED in that no hire was made.
     */
    CANCELLED
}
