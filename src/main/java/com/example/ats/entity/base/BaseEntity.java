package com.example.ats.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base class for all JPA entities in the ATS system.
 *
 * <p>Provides three categories of common fields to every entity:
 * <ol>
 *   <li><strong>Identity:</strong> UUID primary key generated at the Java level.</li>
 *   <li><strong>Temporal auditing:</strong> {@code createdAt} and {@code updatedAt}
 *       populated automatically by Spring Data JPA Auditing.</li>
 *   <li><strong>User auditing:</strong> {@code createdBy} and {@code updatedBy}
 *       populated by the {@link com.example.ats.config.JpaAuditingConfig#auditorProvider()}
 *       bean via the {@code SecurityContext}.</li>
 * </ol>
 *
 * <h3>Key Design Decisions</h3>
 *
 * <h4>1. {@code @MappedSuperclass} — NOT {@code @Entity}</h4>
 * <p>{@code @MappedSuperclass} instructs Hibernate to inline these columns into
 * each subclass's own table. Using {@code @Entity} on the superclass would trigger
 * inheritance table strategies (SINGLE_TABLE, JOINED, TABLE_PER_CLASS), creating
 * either one giant table or a separate {@code base_entity} table — neither is desired.
 *
 * <h4>2. UUID with {@code GenerationType.UUID}</h4>
 * <p>Available since Hibernate 6 (Spring Boot 3). Generates UUIDs in the JVM
 * using {@code java.util.UUID.randomUUID()} before INSERT, not via a database
 * sequence. This means the ID is available immediately after object construction —
 * critical for correct {@code equals/hashCode} behaviour.
 *
 * <h4>3. {@code EqualsAndHashCode} based on UUID id only</h4>
 * <p>JPA entities placed in {@code Set} collections (e.g., {@code Set<Role>}) require
 * a stable, non-null equals/hashCode. Since UUID is pre-assigned at construction,
 * using it for equality is safe and correct. Never base equals/hashCode on
 * auto-incremented {@code SEQUENCE} IDs — they are null before the first flush!
 *
 * <h4>4. {@code @EntityListeners(AuditingEntityListener.class)}</h4>
 * <p>Registers the Spring Data JPA lifecycle listener that intercepts
 * {@code @PrePersist} and {@code @PreUpdate} JPA events to populate the
 * four auditing fields automatically.
 *
 * <h4>5. {@code updatable = false} on {@code createdAt} and {@code createdBy}</h4>
 * <p>Prevents these fields from being accidentally overwritten on UPDATE operations.
 * The "created" timestamp and creator must never change after the row is inserted.
 *
 * <h4>6. {@code Instant} instead of {@code LocalDateTime}</h4>
 * <p>{@code Instant} represents a UTC point in time and maps to PostgreSQL's
 * {@code TIMESTAMPTZ} (timestamp with time zone). {@code LocalDateTime} has no
 * timezone awareness and can cause subtle bugs in multi-timezone deployments.
 *
 * @see com.example.ats.config.JpaAuditingConfig
 * @see com.example.ats.AtsApplication
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ─── Identity ──────────────────────────────────────────────────────────

    /**
     * Surrogate primary key.
     *
     * <p>{@code GenerationType.UUID} (Hibernate 6+) generates UUIDs in the JVM
     * layer using {@link java.util.UUID#randomUUID()} before INSERT, making the
     * ID available immediately after object construction.
     *
     * <p>{@code updatable = false} prevents accidentally overwriting the primary key
     * in an UPDATE statement — the PK must never change for a given row.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // ─── Temporal Auditing ────────────────────────────────────────────────

    /**
     * Timestamp of the INSERT operation.
     * Populated once by {@link AuditingEntityListener} and never updated.
     * Maps to PostgreSQL {@code TIMESTAMPTZ}.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the most recent UPDATE operation.
     * Populated at INSERT (same as {@code createdAt}) and updated on every
     * subsequent UPDATE by {@link AuditingEntityListener}.
     * Maps to PostgreSQL {@code TIMESTAMPTZ}.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ─── User Auditing ────────────────────────────────────────────────────

    /**
     * Email address of the user who created this record.
     * Populated by {@link com.example.ats.config.JpaAuditingConfig#auditorProvider()}.
     * Never updated after the initial INSERT.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 255)
    private String createdBy;

    /**
     * Email address of the user who last modified this record.
     * Updated on every UPDATE by the auditing infrastructure.
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 255)
    private String updatedBy;
}
