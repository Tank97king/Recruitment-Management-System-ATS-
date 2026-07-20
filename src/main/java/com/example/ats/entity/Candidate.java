package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a job candidate profile.
 *
 * <p>Maps to the {@code candidates} table. Candidate profiles are created
 * and maintained by Recruiters. There is no self-service candidate portal
 * in this system — candidates are managed by HR staff on behalf of job seekers.
 *
 * <h3>Relationships</h3>
 *
 * <h4>Candidate → CandidateTag (One-to-Many, OWNING via CandidateTag FK)</h4>
 * <ul>
 *   <li>A candidate can have many skill/label tags.</li>
 *   <li>{@code CascadeType.ALL} — when a candidate is persisted, their tags
 *       are persisted too. When deleted, tags are deleted too. Tags are
 *       entirely owned by the candidate.</li>
 *   <li>{@code orphanRemoval = true} — if a CandidateTag is removed from the
 *       {@code tags} collection, Hibernate automatically issues a DELETE for that
 *       orphaned tag row. Without this, you would need to call
 *       {@code candidateTagRepository.delete(tag)} manually.</li>
 *   <li>{@code FetchType.LAZY} — tags are loaded on demand; not on every candidate query.</li>
 * </ul>
 *
 * <h4>Candidate ← JobApplication (One-to-Many, NOT mapped)</h4>
 * <p>We do NOT map {@code List<JobApplication> applications} here. Applications
 * are queried via {@code JobApplicationRepository.findByCandidateId()}.
 * This avoids loading all applications when fetching a candidate for non-application contexts.
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>Using {@code orphanRemoval = true} on the tags collection ensures no
 *       orphaned CandidateTag rows survive when tags are removed from the list.</li>
 *   <li>Initializing the list to an empty {@code ArrayList} prevents NPE
 *       before the entity is ever saved.</li>
 * </ul>
 */
@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"tags"})
public class Candidate extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "highest_education", length = 255)
    private String highestEducation;

    @Column(name = "location", length = 255)
    private String location;

    /**
     * Total professional experience in years.
     * {@code Short} (wrapper) not {@code short} (primitive) because it is optional.
     * Always use wrapper types for nullable numeric fields.
     */
    @Column(name = "years_of_experience")
    private Short yearsOfExperience;

    @Column(name = "current_company", length = 255)
    private String currentCompany;

    @Column(name = "current_title", length = 255)
    private String currentTitle;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // ─── Resume File ─────────────────────────────────────────────────────

    /**
     * Server filesystem path to the stored resume file.
     * Null when no resume has been uploaded yet.
     */
    @Column(name = "resume_path", length = 1000)
    private String resumePath;

    /** Original filename of the uploaded resume (for download Content-Disposition). */
    @Column(name = "resume_original_name", length = 255)
    private String resumeOriginalName;

    /** Timestamp of the most recent resume upload. */
    @Column(name = "resume_uploaded_at")
    private java.time.Instant resumeUploadedAt;

    // ─── Soft Delete ─────────────────────────────────────────────────────

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    // ─── Relationships ────────────────────────────────────────────────────

    /**
     * Skill and label tags associated with this candidate.
     *
     * <p><strong>Key design choices:</strong>
     * <ul>
     *   <li>{@code CascadeType.ALL} — tag lifecycle is fully owned by the candidate.</li>
     *   <li>{@code orphanRemoval = true} — removing a tag from this list triggers
     *       automatic DELETE. Without this, you get orphaned rows in candidate_tags.</li>
     *   <li>{@code FetchType.LAZY} — tags are only loaded when accessed.</li>
     *   <li>Initialized to empty list — prevents NPE on new candidate objects.</li>
     * </ul>
     */
    @OneToMany(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CandidateTag> tags = new ArrayList<>();

    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private CandidateCv cv;

    // ─── Convenience Methods ──────────────────────────────────────────────

    /**
     * Adds a tag to this candidate, synchronizing both sides of the relationship.
     *
     * <p>Always use this method rather than calling {@code candidate.getTags().add(tag)}
     * directly, because we must also set the back-reference ({@code tag.setCandidate(this)})
     * to keep the bidirectional association consistent in memory.
     *
     * @param tag the tag to add
     */
    public void addTag(CandidateTag tag) {
        this.tags.add(tag);
        tag.setCandidate(this);
    }

    /**
     * Removes a tag from this candidate.
     * {@code orphanRemoval = true} will automatically delete the removed tag from the DB.
     *
     * @param tag the tag to remove
     */
    public void removeTag(CandidateTag tag) {
        this.tags.remove(tag);
        tag.setCandidate(null);
    }
}
