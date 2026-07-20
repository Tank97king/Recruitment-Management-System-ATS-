package com.example.ats.enums;

/**
 * Type of employment contract offered by a job posting.
 *
 * <p>Used to classify job postings and enable filtering on the job search endpoint.
 */
public enum EmploymentType {

    /** Traditional permanent salaried position, typically 40 hours/week. */
    FULL_TIME,

    /** Reduced hours, typically fewer than 30 hours per week. */
    PART_TIME,

    /** Fixed-term or project-based engagement with a defined end date. */
    CONTRACT,

    /** Student or graduate training position, often with stipend. */
    INTERNSHIP,

    /** No office requirement; candidate can work from any location. */
    REMOTE
}
