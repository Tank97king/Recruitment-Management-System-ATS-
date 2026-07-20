package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.InterviewStatus;
import com.example.ats.enums.InterviewType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * JPA entity representing a scheduled interview session for a job application.
 *
 * <p>Maps to the {@code interviews} table. A single application can have
 * multiple interview rounds (phone screen, technical, final). Each interview
 * is scheduled independently and can have its own feedback record.
 *
 * <h3>Relationships</h3>
 *
 * <h4>Interview → JobApplication (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Many interviews belong to one application. FK {@code application_id}.</li>
 *   <li>{@code FetchType.LAZY} — load application only when needed.</li>
 * </ul>
 *
 * <h4>Interview → User (Many-to-One, OWNING) — two FK references</h4>
 * <ul>
 *   <li>{@code createdByUser} — who scheduled the interview (mandatory).</li>
 *   <li>{@code cancelledByUser} — who cancelled it, if applicable (nullable).</li>
 *   <li>Both reference the same {@code users} table but via different FK columns.</li>
 * </ul>
 *
 * <h4>Interview → InterviewFeedback (One-to-One, INVERSE side)</h4>
 * <ul>
 *   <li>Each interview has at most one feedback record. This is the <em>inverse</em>
 *       (non-owning) side — the FK {@code interview_id} lives in the
 *       {@code interview_feedback} table, not here.</li>
 *   <li>{@code mappedBy = "interview"} points to the field on
 *       {@link InterviewFeedback} that holds the FK.</li>
 *   <li>{@code CascadeType.ALL} — deleting an interview cascades to delete its feedback.</li>
 *   <li>{@code orphanRemoval = true} — if feedback is set to null on this entity,
 *       the orphaned feedback row is automatically deleted.</li>
 *   <li>{@code FetchType.LAZY} on OneToOne — EAGER is the JPA default for OneToOne,
 *       but we explicitly set LAZY to avoid loading feedback on every interview query.
 *       Note: Hibernate may still issue a secondary SELECT for LAZY OneToOne in some cases
 *       unless bytecode enhancement is configured. For this portfolio project, LAZY is
 *       declared intentionally to signal the design intent.</li>
 * </ul>
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>Knowing which side holds the FK: the FK is in {@code interview_feedback},
 *       so InterviewFeedback is OWNING and has {@code @JoinColumn}. Interview is INVERSE
 *       and uses {@code mappedBy}. Getting this wrong causes Hibernate to not recognize
 *       the relationship and may generate extra columns or fail to join.</li>
 *   <li>Explicitly setting FetchType.LAZY on OneToOne — EAGER is the JPA spec default
 *       which would load feedback on every interview SELECT.</li>
 * </ul>
 */
@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"application", "createdByUser", "cancelledByUser", "feedback"})
public class Interview extends BaseEntity {

    // ─── Relationships (Many-to-One) ─────────────────────────────────────

    /** The job application this interview is part of. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    /** The recruiter or HR staff who scheduled this interview. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    /**
     * The user who cancelled this interview, if it was cancelled.
     * Null when the interview has not been cancelled.
     *
     * <p>{@code optional = true} (default) — this FK is nullable in the DB.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_user_id")
    private User cancelledByUser;

    // ─── Schedule Details ─────────────────────────────────────────────────

    /** Date and time when the interview is scheduled. Maps to {@code TIMESTAMPTZ}. */
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    /**
     * Expected duration of the interview in minutes.
     * Wrapper type {@code Integer} (not primitive {@code int}) because the
     * column has a default value at the DB level; the Java default is also set.
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false, length = 20)
    private InterviewType interviewType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    /**
     * Video conference URL (for {@link InterviewType#VIDEO} interviews).
     * e.g., "https://meet.google.com/abc-defg-hij"
     */
    @Column(name = "meeting_link", length = 1000)
    private String meetingLink;

    /**
     * Physical location (for {@link InterviewType#ONSITE} interviews).
     * e.g., "123 Tech Street, Floor 4, Ho Chi Minh City"
     */
    @Column(name = "location", length = 500)
    private String location;

    /**
     * Comma-separated names or emails of people participating as interviewers.
     * Free-text: more complex systems would link this to a junction table.
     * For intern-level scope, free-text is deliberately chosen.
     */
    @Column(name = "interviewer_names", columnDefinition = "TEXT")
    private String interviewerNames;

    @Column(name = "interviewer_email", length = 255)
    private String interviewerEmail;

    /** Pre-interview notes or agenda set by the recruiter. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    // ─── Relationship (One-to-One, INVERSE side) ─────────────────────────

    /**
     * Post-interview feedback submitted by the interviewer.
     *
     * <p>This is the <em>inverse</em> (non-owning) side of the OneToOne.
     * The FK {@code interview_id} lives in the {@code interview_feedback} table.
     * {@code mappedBy = "interview"} references the field name in
     * {@link InterviewFeedback#interview}.
     *
     * <p>{@code CascadeType.ALL} + {@code orphanRemoval = true}: deleting this
     * interview cascades to delete the associated feedback record. Setting
     * feedback to null on this entity will also delete the feedback row.
     *
     * <p>Will be null until feedback is submitted after the interview is COMPLETED.
     */
    @OneToOne(
            mappedBy = "interview",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private InterviewFeedback feedback;

    // ─── Convenience Methods ──────────────────────────────────────────────

    /**
     * Attaches feedback to this interview, synchronizing both sides.
     * This method must be used instead of directly setting {@code feedback.setInterview(this)}
     * to keep the in-memory object graph consistent.
     *
     * @param feedback the feedback to attach
     */
    public void setFeedback(InterviewFeedback feedback) {
        this.feedback = feedback;
        if (feedback != null) {
            feedback.setInterview(this);
        }
    }
}
