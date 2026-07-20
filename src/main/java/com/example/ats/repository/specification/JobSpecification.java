package com.example.ats.repository.specification;

import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.ExperienceLevel;
import com.example.ats.enums.JobStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA specifications for dynamic filtering and querying of {@link Job} entities.
 */
public class JobSpecification {

    /**
     * Builds a dynamic specification based on various filters.
     *
     * @param keyword         search keyword matching title or description (case-insensitive)
     * @param companyId       filter by company ID
     * @param location        search keyword matching location (case-insensitive)
     * @param employmentType  filter by employment type
     * @param experienceLevel filter by experience level
     * @param status          filter by job lifecycle status
     * @param salaryMin       minimum salary bound
     * @param salaryMax       maximum salary bound
     * @param deadlineFrom    start date of application deadline range
     * @param deadlineTo      end date of application deadline range
     * @return the combined JPA specification
     */
    public static Specification<Job> filterJobs(
            String keyword,
            UUID companyId,
            String location,
            EmploymentType employmentType,
            ExperienceLevel experienceLevel,
            JobStatus status,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            LocalDate deadlineFrom,
            LocalDate deadlineTo
    ) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Rule: Always exclude soft-deleted jobs
            predicates.add(builder.equal(root.get("isDeleted"), false));

            // 1. Keyword search (case-insensitive matching against title and description)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String matchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = builder.like(builder.lower(root.get("title")), matchPattern);
                Predicate descMatch = builder.like(builder.lower(root.get("description")), matchPattern);
                predicates.add(builder.or(titleMatch, descMatch));
            }

            // 2. Company filtering
            if (companyId != null) {
                predicates.add(builder.equal(root.get("company").get("id"), companyId));
            }

            // 3. Location filtering (case-insensitive substring match)
            if (location != null && !location.trim().isEmpty()) {
                String matchPattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(builder.like(builder.lower(root.get("location")), matchPattern));
            }

            // 4. Employment Type filtering
            if (employmentType != null) {
                predicates.add(builder.equal(root.get("employmentType"), employmentType));
            }

            // 5. Experience Level filtering
            if (experienceLevel != null) {
                predicates.add(builder.equal(root.get("experienceLevel"), experienceLevel));
            }

            // 6. Status filtering
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            // 7. Salary range filtering
            if (salaryMin != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin));
            }
            if (salaryMax != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("salaryMax"), salaryMax));
            }

            // 8. Deadline range filtering
            if (deadlineFrom != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("deadline"), deadlineFrom));
            }
            if (deadlineTo != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("deadline"), deadlineTo));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Constructs a specification for global job search matching title, description, or location.
     *
     * @param keyword search keyword
     * @return JPA specification for jobs
     */
    public static Specification<Job> globalSearch(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("location")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, locationMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
