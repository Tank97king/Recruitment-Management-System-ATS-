package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a single skill or label tag on a candidate profile.
 *
 * <p>Maps to the {@code candidate_tags} table. Tags are the "child" side of
 * the {@link Candidate} → {@code CandidateTag} One-to-Many relationship.
 * Each row represents exactly one tag value for one candidate.
 *
 * <h3>Relationship</h3>
 *
 * <h4>CandidateTag → Candidate (Many-to-One, OWNING side)</h4>
 * <ul>
 *   <li>Many tags belong to one candidate. The FK {@code candidate_id}
 *       is in this table, making CandidateTag the owning side.</li>
 *   <li>{@code FetchType.LAZY} — avoids loading the full candidate object
 *       when a tag is queried in isolation.</li>
 *   <li>{@code optional = false} — every tag must have a parent candidate.</li>
 * </ul>
 *
 * <h3>Uniqueness</h3>
 * <p>The combination of {@code (candidate_id, tag)} must be unique — a
 * candidate cannot have duplicate tags. This is enforced at both the
 * DB level (unique constraint in Flyway migration) and the application level
 * (service checks before adding).
 */
@Entity
@Table(
        name = "candidate_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_candidate_tags_candidate_id_tag",
                columnNames = {"candidate_id", "tag"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"candidate"})
public class CandidateTag extends BaseEntity {

    /**
     * The candidate this tag belongs to.
     *
     * <p>{@code optional = false} ensures Hibernate uses INNER JOIN (not
     * LEFT OUTER JOIN) for this association, improving query performance.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * The tag/label value (e.g., "Java", "Spring Boot", "AWS", "PostgreSQL").
     * Free-text, case-insensitive comparison handled at the repository level.
     */
    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    public CandidateTag(Candidate candidate, String tag) {
        this.candidate = candidate;
        this.tag = tag;
    }
}
