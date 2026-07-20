package com.example.ats.enums;

/**
 * Format/modality of an interview session.
 *
 * <p>Determines what additional scheduling information is required:
 * <ul>
 *   <li>{@code VIDEO}  → {@code meetingLink} must be provided</li>
 *   <li>{@code ONSITE} → {@code location} must be provided</li>
 *   <li>{@code PHONE} / {@code TECHNICAL} → both fields are optional</li>
 * </ul>
 */
public enum InterviewType {

    /** Online interview via video conference link. */
    ONLINE,

    /** Offline in-person interview at a physical location. */
    OFFLINE,

    /** Audio-only telephone call. No special equipment required. */
    PHONE,

    /**
     * Video conference via a platform such as Zoom, Google Meet, or Teams.
     * A {@code meetingLink} URL should be provided in the interview record.
     */
    VIDEO,

    /**
     * In-person interview at a physical office location.
     * A {@code location} address should be provided in the interview record.
     */
    ONSITE,

    /**
     * Structured technical evaluation — whiteboard coding, live pair
     * programming, system design discussion, or similar technical assessment
     * conducted during a live session (not take-home).
     */
    TECHNICAL
}
