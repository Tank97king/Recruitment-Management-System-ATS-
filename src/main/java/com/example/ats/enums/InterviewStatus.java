package com.example.ats.enums;

/**
 * Lifecycle status of a scheduled interview session.
 *
 * <ul>
 *   <li>{@code SCHEDULED}  — Interview is confirmed and upcoming.</li>
 *   <li>{@code COMPLETED}  — Interview was conducted successfully.
 *       Feedback submission is now expected.</li>
 *   <li>{@code CANCELLED}  — Interview was cancelled before it occurred.
 *       A {@code cancellationReason} should be recorded.</li>
 * </ul>
 */
public enum InterviewStatus {

    /** Interview is confirmed and has not yet occurred. Default state on creation. */
    SCHEDULED,

    /** Interview was conducted. The interviewer can now submit feedback. */
    COMPLETED,

    /** Interview was cancelled. A cancellation reason should be recorded. */
    CANCELLED
}
