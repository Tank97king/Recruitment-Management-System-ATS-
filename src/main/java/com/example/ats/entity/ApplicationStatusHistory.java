package com.example.ats.entity;

import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.PipelineStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a single immutable entry in the application audit log.
 *
 * <p>Maps to the {@code application_status_history} table. Every time an
 * application's {@code status} or {@code pipelineStage} changes, a new row
 * is appended to this table. This provides a complete, traceable history of
 * all recruitment decisions.
 *
 * <h3>Why NOT extending BaseEntity?</h3>
 * <p>This entity intentionally does NOT extend {@link com.example.ats.entity.base.BaseEntity}
 * because:
 * <ul>
 *   <li>History records are <em>immutable</em> — they must never be updated.
 *       BaseEntity includes {@code updatedAt} and {@code updatedBy}, which
 *       would semantically imply mutability.</li>
 *   <li>The semantically correct timestamp here is {@code changedAt}, not
 *       {@code createdAt}. Using a custom field is more readable in queries
 *       and dashboards (e.g., "show events after changedAt > ?").</li>
 *   <li>There are no auditing fields needed — the change record IS the audit.</li>
 * </ul>
 *
 * <h3>Relationship</h3>
 *
 * <h4>ApplicationStatusHistory → JobApplication (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Many history entries belong to one application. FK {@code application_id}.</li>
 *   <li>{@code FetchType.LAZY} — never eager-load the application when fetching history.</li>
 * </ul>
 *
 * <h4>ApplicationStatusHistory → User (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Each entry records who made the change. FK {@code changed_by_user_id}.</li>
 * </ul>
 *
 * <h3>Append-Only Rule</h3>
 * <p>No UPDATE operations should ever be issued against this table.
 * Services should only call {@code repository.save(new ApplicationStatusHistory(...))}
 * and never call any update method on existing history records.
 */
@Entity
@Table(name = "application_status_history")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"application", "changedByUser"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // ─── Relationships ────────────────────────────────────────────────────

    /** The application this history entry belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, updatable = false)
    private JobApplication application;

    /** The user who triggered the status/stage change. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_user_id", nullable = false, updatable = false)
    private User changedByUser;

    // ─── Change Details ───────────────────────────────────────────────────

    /** Application status before this change. Null for the initial entry. */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20, updatable = false)
    private ApplicationStatus previousStatus;

    /** Application status after this change. */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20, updatable = false)
    private ApplicationStatus newStatus;

    /** Pipeline stage before this change. Null for the initial entry. */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_stage", length = 30, updatable = false)
    private PipelineStage previousStage;

    /** Pipeline stage after this change. */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_stage", nullable = false, length = 30, updatable = false)
    private PipelineStage newStage;

    /** Optional context note explaining why the change was made. */
    @Column(name = "note", columnDefinition = "TEXT", updatable = false)
    private String note;

    /**
     * When this status change occurred.
     * Using a business-semantics timestamp ({@code changedAt}) rather than
     * {@code createdAt} for clarity in activity feeds and audit queries.
     */
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt = Instant.now();
}
