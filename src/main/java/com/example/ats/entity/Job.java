package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.ExperienceLevel;
import com.example.ats.enums.JobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a job posting in the ATS system.
 *
 * <p>Maps to the {@code jobs} table. A Job is the central business object —
 * candidates apply to jobs, and the entire recruitment pipeline is driven
 * by job applications.
 *
 * <h3>Relationships</h3>
 *
 * <h4>Job → Company (Many-to-One, OWNING side)</h4>
 * <ul>
 *   <li>Many jobs belong to one company. The FK {@code company_id} is in
 *       the {@code jobs} table, making Job the owning side.</li>
 *   <li>{@code FetchType.LAZY} — company details are only loaded when
 *       explicitly accessed via {@code job.getCompany()}. Without LAZY,
 *       every job list query would JOIN the companies table unnecessarily.</li>
 *   <li>{@code optional = false} — every job must have a company.
 *       Tells Hibernate to use INNER JOIN (not LEFT OUTER JOIN) when fetching.</li>
 * </ul>
 *
 * <h4>Job → User (Many-to-One, OWNING side)</h4>
 * <ul>
 *   <li>Many jobs can be created by one user (recruiter). FK {@code created_by_user_id}
 *       in the {@code jobs} table.</li>
 *   <li>{@code FetchType.LAZY} for the same reasons as the company relationship.</li>
 *   <li>Note: This is a BUSINESS FK (who owns this job) — distinct from the
 *       {@code created_by} STRING field inherited from BaseEntity (which is the
 *       email string populated by JPA Auditing for general change tracking).</li>
 * </ul>
 *
 * <h3>Salary Design Note</h3>
 * <p>{@code BigDecimal} is used for salary fields. NEVER use {@code float} or
 * {@code double} for monetary amounts — floating-point arithmetic introduces
 * rounding errors that are unacceptable in financial data.
 * {@code BigDecimal} maps to PostgreSQL {@code NUMERIC(15, 2)}.
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>No {@code List<JobApplication> applications} collection mapped here —
 *       applications are queried via {@code JobApplicationRepository}.</li>
 *   <li>Using {@code LocalDate} (not {@code LocalDateTime}) for deadline — job
 *       deadlines are calendar-day concepts, not time-of-day specific.</li>
 *   <li>Using {@code BigDecimal} for salary — monetary precision guaranteed.</li>
 * </ul>
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"company", "createdByUser"})
public class Job extends BaseEntity {

    // ─── Relationships (Many-to-One) ─────────────────────────────────────

    /**
     * The company that posted this job.
     *
     * <p>{@code FetchType.LAZY} — avoids loading company data on every job query.
     * {@code optional = false} — every job must belong to a company (NOT NULL FK).
     *
     * <p><strong>Common Mistake Avoided:</strong> Not using {@code FetchType.EAGER}
     * on ManyToOne relationships. While LAZY is not the default for ManyToOne
     * (EAGER is Hibernate's default), always set it explicitly to LAZY so that
     * the behavior is clear and not dependent on Hibernate's defaults changing.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * The recruiter or admin who created this job posting.
     *
     * <p>This is the business-level ownership FK (distinct from the audit
     * {@code created_by} string field in BaseEntity). It enables queries like
     * "show me all jobs created by recruiter X" and enforces that a job has
     * a real, identifiable owner in the system.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    // ─── Job Details ──────────────────────────────────────────────────────

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 20)
    private ExperienceLevel experienceLevel;

    // ─── Compensation ─────────────────────────────────────────────────────

    /**
     * Minimum salary offered. Maps to PostgreSQL {@code NUMERIC(15, 2)}.
     * Using {@code BigDecimal} for exact decimal representation — never float/double
     * for monetary values due to binary floating-point imprecision.
     */
    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    /**
     * ISO 4217 currency code (e.g., "USD", "VND", "EUR").
     * Defaults to "USD" to match the application.yml default.
     */
    @Column(name = "currency", length = 10)
    private String currency = "USD";

    // ─── Status and Deadline ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status = JobStatus.DRAFT;

    /**
     * Application deadline. {@code LocalDate} (not {@code LocalDateTime}) because
     * job deadlines are whole-day boundaries, not time-of-day specific moments.
     * Maps to PostgreSQL {@code DATE} column.
     */
    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;
}
