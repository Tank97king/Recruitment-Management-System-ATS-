package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.PipelineStage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a candidate's application for a specific job.
 *
 * <p>Maps to the {@code job_applications} table. This is the central workflow
 * entity of the ATS — every recruitment activity (pipeline stage changes,
 * interviews, status history) is anchored to a JobApplication.
 *
 * <p>BusinessRule: One candidate can have at most ONE active application per job.
 * Enforced via a partial unique index in Flyway: {@code UNIQUE(job_id, candidate_id)
 * WHERE is_deleted = FALSE}.
 *
 * <h3>Relationships</h3>
 *
 * <h4>JobApplication → Job (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Many applications belong to one job. FK {@code job_id} in this table.</li>
 *   <li>{@code FetchType.LAZY} — avoids loading full Job for every application query.</li>
 *   <li>{@code optional = false} — every application must reference a job.</li>
 * </ul>
 *
 * <h4>JobApplication → Candidate (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Many applications belong to one candidate. FK {@code candidate_id} in this table.</li>
 *   <li>{@code FetchType.LAZY} — avoids loading full Candidate for every application query.</li>
 * </ul>
 *
 * <h4>JobApplication → ApplicationStatusHistory (One-to-Many)</h4>
 * <ul>
 *   <li>One application has many status history entries (audit log).</li>
 *   <li>{@code CascadeType.ALL} — history is fully owned by the application.</li>
 *   <li>{@code orphanRemoval = true} — removing a history entry from the list
 *       deletes the DB row (though in practice, history is never removed).</li>
 *   <li>{@code List} (not Set) — history entries are ordered and may contain
 *       the same status at different timestamps. Set semantics (uniqueness)
 *       would be incorrect here.</li>
 * </ul>
 *
 * <h4>JobApplication → Interview (One-to-Many, NOT mapped)</h4>
 * <p>Interviews are queried via {@code InterviewRepository.findByApplicationId()}.
 * Not mapping the collection here keeps the entity lean.
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>Using two separate status fields ({@code status} + {@code pipelineStage})
 *       rather than trying to infer one from the other — they serve different
 *       stakeholder views and must be independently settable.</li>
 *   <li>Using {@code List} (not Set) for the ordered history collection —
 *       Set would deduplicate based on equals(), losing entries.</li>
 * </ul>
 */
@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"job", "candidate", "statusHistories"})
public class JobApplication extends BaseEntity {

    // ─── Relationships (Many-to-One) ─────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    // ─── Application Content ─────────────────────────────────────────────

    /** Optional cover letter or recruiter notes at time of application. */
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    /** Internal recruiter notes about this application (not shared with candidate). */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ─── Status Tracking ─────────────────────────────────────────────────

    /**
     * Coarse-grained application lifecycle status.
     * Provides a high-level view for stakeholders.
     * For granular funnel tracking, see {@link #pipelineStage}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /**
     * Fine-grained position in the recruitment pipeline.
     * Powers the Kanban board endpoint.
     * Tracks exactly which step of the hiring funnel the candidate is in.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_stage", nullable = false, length = 30)
    private PipelineStage pipelineStage = PipelineStage.SCREENING;

    /** Explanation provided when status is set to REJECTED. Optional. */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * When the application was submitted. Distinct from {@code createdAt} in BaseEntity:
     * {@code createdAt} is the technical record creation timestamp (set by JPA auditing);
     * {@code appliedAt} is the business-level application submission timestamp.
     * For most cases they are equal, but explicit separation allows future flexibility
     * (e.g., back-dating an imported application).
     */
    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    // ─── Relationships (One-to-Many) ─────────────────────────────────────

    /**
     * Immutable audit trail of every status and pipeline stage change.
     *
     * <p>{@code CascadeType.ALL} + {@code orphanRemoval = true}: history entries
     * are owned by this application. {@code List} is used (not Set) because
     * history is ordered by time and must preserve all entries including
     * duplicates (same status changed twice by different users at different times).
     *
     * <p>In practice, history entries are never removed — this is an append-only log.
     * The orphanRemoval is present for theoretical correctness.
     */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ApplicationStatusHistory> statusHistories = new ArrayList<>();

    // ─── Convenience Methods ──────────────────────────────────────────────

    /**
     * Appends a status history entry to this application's audit log.
     * Synchronizes both sides of the bidirectional relationship.
     *
     * @param history the history entry to append
     */
    public void addStatusHistory(ApplicationStatusHistory history) {
        this.statusHistories.add(history);
        history.setApplication(this);
    }
}
