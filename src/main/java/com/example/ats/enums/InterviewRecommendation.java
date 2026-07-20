package com.example.ats.enums;

/**
 * Interviewer's hiring recommendation submitted as part of interview feedback.
 *
 * <p>Provides a structured 5-point Likert scale that gives the hiring team
 * a clear, comparable signal across multiple interviewers.
 *
 * <p>Used by the dashboard to aggregate feedback scores and identify consensus.
 */
public enum InterviewRecommendation {

    /** Strongly recommend hiring. Exceptional candidate; top of the applicant pool. */
    STRONG_YES,

    /** Recommend hiring. Candidate meets requirements and would be a good fit. */
    YES,

    /** No strong opinion either way. Could go with or without hiring this candidate. */
    NEUTRAL,

    /** Do not recommend hiring. Candidate does not meet key requirements. */
    NO,

    /** Strongly advise against hiring. Significant skill gaps or cultural concerns. */
    STRONG_NO
}
