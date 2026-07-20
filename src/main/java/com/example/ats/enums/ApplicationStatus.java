package com.example.ats.enums;

/**
 * Coarse-grained lifecycle status of a job application.
 *
 * <p>Provides a high-level overview of where an application stands in the
 * hiring process. This is the status visible to all stakeholders (recruiters,
 * hiring managers, executives).
 *
 * <p>For fine-grained pipeline position tracking, see {@link PipelineStage}.
 *
 * <p>Typical flow:
 * <pre>
 *   APPLIED → REVIEWING → INTERVIEW → OFFER → HIRED
 *                │              │        │
 *                └──────────────┴────────┴──► REJECTED
 *                                             WITHDRAWN (at any stage)
 * </pre>
 */
public enum ApplicationStatus {

    /**
     * Application has been submitted. Awaiting initial HR review.
     * Default state when an application is first created.
     */
    APPLIED,

    /**
     * A recruiter is actively reviewing the candidate's profile and resume.
     */
    REVIEWING,

    /**
     * Candidate has been shortlisted and is in the interview process.
     * One or more interviews have been scheduled or completed.
     */
    INTERVIEW,

    /**
     * A formal offer of employment has been extended to the candidate.
     */
    OFFER,

    /**
     * Candidate accepted the offer. Hiring process is complete.
     * This is a terminal success state.
     */
    HIRED,

    /**
     * Candidate was not selected at some stage of the process.
     * This is a terminal rejection state.
     */
    REJECTED,

    /**
     * Candidate voluntarily withdrew from consideration.
     * This is a terminal withdrawal state.
     */
    WITHDRAWN
}
