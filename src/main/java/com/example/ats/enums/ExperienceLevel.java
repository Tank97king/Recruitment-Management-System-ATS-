package com.example.ats.enums;

/**
 * Required professional experience level for a job posting.
 *
 * <p>Used to classify job postings and match them against candidates'
 * years of experience. Enables filtering on both the job search and
 * candidate search endpoints.
 */
public enum ExperienceLevel {

    /** No prior professional experience required. Suitable for fresh graduates. */
    ENTRY,

    /** 0–2 years of relevant professional experience. */
    JUNIOR,

    /** 2–5 years of relevant professional experience. */
    MID,

    /** 5–10 years of professional experience; expected to work independently. */
    SENIOR,

    /** 10+ years; includes leadership responsibilities and system design ownership. */
    LEAD
}
