package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.CompanySize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing an employer company profile.
 *
 * <p>Maps to the {@code companies} table. Companies are independent master
 * data entities — they can exist without any jobs, and are created and
 * maintained by Admins.
 *
 * <h3>Relationships</h3>
 *
 * <h4>Company → Job (One-to-Many, UNIDIRECTIONAL via repository)</h4>
 * <ul>
 *   <li>A company can post many jobs.</li>
 *   <li>We deliberately do NOT map a {@code List<Job> jobs} collection here.
 *       Bidirectional mapping would load ALL jobs when the company is touched,
 *       causing a severe N+1 problem on company list endpoints.</li>
 *   <li>Use {@code JobRepository.findByCompanyId(companyId, pageable)} for
 *       paginated job retrieval by company.</li>
 * </ul>
 *
 * <h3>Common JPA Mistake Avoided</h3>
 * <p>Over-mapping bidirectional relationships. The rule: only map the
 * "navigation direction" your application actually needs. If you never
 * call {@code company.getJobs()}, don't map it — save the complexity and
 * the risk of accidental eager-load.
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Company extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    /**
     * Industry classification (e.g., "Technology", "Finance", "Healthcare").
     * Free-text to allow flexibility; not an enum to avoid rigid categorisation.
     */
    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    /** Full company description / about section. No length limit (TEXT column). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "headquarters_location", length = 255)
    private String headquartersLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_size", length = 20)
    private CompanySize companySize;

    /**
     * Four-digit founding year (e.g., 2018).
     * {@code Short} (wrapper) rather than {@code short} (primitive) because
     * this field is optional — null means "not provided".
     * Always use wrapper types for nullable numeric columns.
     */
    @Column(name = "founded_year")
    private Short foundedYear;

    /** Server filesystem path to the uploaded company logo file. */
    @Column(name = "logo_path", length = 1000)
    private String logoPath;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;
}
