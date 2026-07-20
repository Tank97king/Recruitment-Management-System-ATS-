package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.InterviewRecommendation;
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
 * JPA entity representing structured post-interview feedback.
 *
 * <p>Maps to the {@code interview_feedback} table. Exactly one feedback
 * record can exist per interview — enforced by the UNIQUE constraint on
 * {@code interview_id} in the database schema.
 *
 * <h3>Relationship</h3>
 *
 * <h4>InterviewFeedback → Interview (One-to-One, OWNING side)</h4>
 * <ul>
 *   <li>This entity holds the FK {@code interview_id} in its table,
 *       making it the <em>owning</em> side of the One-to-One relationship.</li>
 *   <li>The owning side always carries {@code @JoinColumn}.</li>
 *   <li>The inverse side ({@link Interview#feedback}) carries {@code mappedBy}.</li>
 *   <li>{@code unique = true} on the {@code @JoinColumn} enforces the one-to-one
 *       cardinality at the JPA layer (complementing the DB unique constraint).</li>
 * </ul>
 *
 * <h4>InterviewFeedback → User (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Tracks who submitted this feedback. FK {@code submitted_by_user_id}.</li>
 * </ul>
 *
 * <h3>Rating Design Note</h3>
 * <p>Ratings are stored as {@code Integer} (not {@code int} primitive) because:
 * <ul>
 *   <li>Technical/communication/cultural fit ratings are optional (nullable).</li>
 *   <li>Only {@code overall_rating} is required. The others can be NULL if the
 *       interviewer didn't assess that dimension.</li>
 *   <li>Primitives cannot be null — they default to 0, which would create
 *       false "zero rating" data instead of "not rated".</li>
 * </ul>
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>The FK is correctly placed on this (feedback) table, NOT on the
 *       interview table. The table that holds the FK is always the owning side.</li>
 *   <li>Using wrapper Integer (not int) for nullable rating fields to
 *       accurately represent "not provided" vs "rated zero".</li>
 * </ul>
 */
@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"interview", "submittedByUser"})
public class InterviewFeedback extends BaseEntity {

    // ─── Relationships ────────────────────────────────────────────────────

    /**
     * The interview this feedback belongs to.
     *
     * <p>This is the <em>owning</em> side: this table holds the FK {@code interview_id}.
     * {@code unique = true} enforces one-to-one at the JPA layer.
     * {@code optional = false} — every feedback must link to an interview.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    /** The user who authored and submitted this feedback. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedByUser;

    // ─── Structured Ratings ───────────────────────────────────────────────

    /**
     * Overall assessment score from 1 (poor) to 5 (excellent).
     * Required — every feedback must have an overall rating.
     */
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    /**
     * Technical skills rating (1–5). Nullable — some interviews (e.g., cultural fit)
     * do not assess technical skills.
     * Using wrapper {@code Integer} (not {@code int}) to allow null.
     */
    @Column(name = "technical_rating")
    private Integer technicalRating;

    /**
     * Communication and interpersonal skills rating (1–5). Optional.
     */
    @Column(name = "communication_rating")
    private Integer communicationRating;

    /**
     * Cultural fit alignment rating (1–5). Optional.
     */
    @Column(name = "cultural_fit_rating")
    private Integer culturalFitRating;

    /**
     * Structured hiring recommendation from the interviewer.
     * This is the most actionable field — it directly feeds the decision-making process.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 20)
    private InterviewRecommendation recommendation;

    /**
     * Free-form detailed feedback notes. No length limit.
     * May include specific strengths, weaknesses, and examples from the interview.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * When this feedback was submitted.
     * Semantically more meaningful than {@code createdAt} for this entity.
     */
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();
}
