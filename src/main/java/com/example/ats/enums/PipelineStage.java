package com.example.ats.enums;

/**
 * Fine-grained position of an application within the recruitment pipeline.
 *
 * <p>Pipeline stages represent the specific hiring funnel steps a candidate
 * moves through. This is used to power the Kanban board view
 * ({@code GET /api/v1/jobs/{id}/pipeline}) and the hiring funnel analytics.
 *
 * <p>Unlike {@link ApplicationStatus} (coarse-grained), pipeline stages
 * map directly to the physical steps in the interview process.
 *
 * <p>Standard progression:
 * <pre>
 *   SCREENING → PHONE_SCREEN → TECHNICAL_ASSESSMENT
 *       → INTERVIEW_ROUND_1 → INTERVIEW_ROUND_2 → OFFER → HIRED
 *
 *   Any stage → REJECTED (candidate not selected at this stage)
 *   Any stage → WITHDRAWN (candidate self-withdrew)
 * </pre>
 */
public enum PipelineStage {

    /**
     * Resume and profile review by HR. First filter in the funnel.
     * Default stage when an application is submitted.
     */
    SCREENING,

    /**
     * Brief introductory call (15–30 min) to assess basic fit
     * and communicate role expectations.
     */
    PHONE_SCREEN,

    /**
     * Take-home assignment, online coding challenge, or skills test
     * used to evaluate technical capability before interviews.
     */
    TECHNICAL_ASSESSMENT,

    /** First structured in-depth interview with the hiring team. */
    INTERVIEW_ROUND_1,

    /**
     * Second interview round (optional). Used when the hiring team
     * wants additional evaluation after round 1.
     */
    INTERVIEW_ROUND_2,

    /** Formal offer letter or verbal offer has been extended. */
    OFFER,

    /** Candidate accepted the offer. Hiring complete. Terminal success state. */
    HIRED,

    /** Candidate did not meet requirements at the current stage. Terminal rejection state. */
    REJECTED,

    /** Candidate voluntarily withdrew from the process. Terminal withdrawal state. */
    WITHDRAWN
}
