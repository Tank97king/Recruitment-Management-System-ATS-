package com.example.ats.repository;

import com.example.ats.entity.Job;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.ExperienceLevel;
import com.example.ats.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Job} entity.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    /**
     * Finds an active (non-deleted) job by ID.
     *
     * @param id the job's UUID
     * @return the job if found and not soft-deleted
     */
    Optional<Job> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Counts active jobs for a specific company.
     * Used on the company detail endpoint to show "X open positions".
     *
     * @param companyId the company's UUID
     * @param status    the job status to count (typically OPEN)
     * @return the count of matching jobs
     */
    long countByCompanyIdAndStatusAndIsDeletedFalse(UUID companyId, JobStatus status);

    /**
     * Retrieves a paginated, filterable list of active job postings.
     * Supports multi-field search by title/description keyword.
     *
     * @param status         filter by job lifecycle status; null = any
     * @param companyId      filter by company; null = any
     * @param employmentType filter by employment type; null = any
     * @param experienceLevel filter by experience level; null = any
     * @param search         keyword to match against title/description (case-insensitive)
     * @param pageable       pagination and sort configuration
     * @return a page of matching non-deleted jobs
     */
    @Query("""
            SELECT j FROM Job j
            WHERE j.isDeleted = false
              AND (:status         IS NULL OR j.status         = :status)
              AND (:companyId      IS NULL OR j.company.id     = :companyId)
              AND (:employmentType IS NULL OR j.employmentType = :employmentType)
              AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
              AND (:search IS NULL OR
                   LOWER(j.title)       LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Job> findAllActiveWithFilters(
            @Param("status") JobStatus status,
            @Param("companyId") UUID companyId,
            @Param("employmentType") EmploymentType employmentType,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Retrieves all active jobs created by a specific recruiter.
     * Used for the "My Jobs" view available to Recruiters.
     *
     * @param userId   the recruiter's UUID
     * @param pageable pagination and sort
     * @return a page of jobs created by this user
     */
    Page<Job> findByCreatedByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    /**
     * Counts all non-deleted jobs for a specific company.
     *
     * @param companyId the company's UUID
     * @return the count of non-deleted jobs
     */
    long countByCompanyIdAndIsDeletedFalse(UUID companyId);

    /**
     * Counts all non-deleted jobs.
     */
    long countByIsDeletedFalse();

    /**
     * Groups and counts non-deleted jobs by status.
     */
    @Query("""
            SELECT j.status, COUNT(j)
            FROM Job j
            WHERE j.isDeleted = false
            GROUP BY j.status
            """)
    List<Object[]> countJobStatuses();
}
