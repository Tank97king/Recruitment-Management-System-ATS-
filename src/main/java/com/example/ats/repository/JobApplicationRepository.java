package com.example.ats.repository;

import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.PipelineStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link JobApplication} entity.
 *
 * <p>This is the most query-intensive repository in the system. All recruitment
 * workflow operations — Kanban boards, pipeline analytics, application history —
 * flow through this repository.
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    /**
     * Finds a non-deleted application by ID.
     *
     * @param id the application's UUID
     * @return the application if found and not soft-deleted
     */
    Optional<JobApplication> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks whether an active application already exists for a given
     * candidate+job combination. Enforces the business rule of one active
     * application per candidate per job.
     *
     * @param jobId       the job's UUID
     * @param candidateId the candidate's UUID
     * @return true if an active application already exists
     */
    boolean existsByJobIdAndCandidateIdAndIsDeletedFalse(UUID jobId, UUID candidateId);

    /**
     * Retrieves a paginated list of active applications for a specific job.
     * Supports filtering by status and pipeline stage (for Kanban column).
     *
     * @param jobId         the job's UUID
     * @param status        filter by application status; null = any
     * @param pipelineStage filter by pipeline stage; null = any
     * @param pageable      pagination and sort
     * @return a page of matching applications
     */
    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.job.id = :jobId
              AND a.isDeleted = false
              AND (:status IS NULL        OR a.status = :status)
              AND (:pipelineStage IS NULL OR a.pipelineStage = :pipelineStage)
            """)
    Page<JobApplication> findAllByJobWithFilters(
            @Param("jobId") UUID jobId,
            @Param("status") ApplicationStatus status,
            @Param("pipelineStage") PipelineStage pipelineStage,
            Pageable pageable
    );

    /**
     * Retrieves all applications submitted by a specific candidate.
     * Used on the candidate profile page to display application history.
     *
     * @param candidateId the candidate's UUID
     * @param pageable    pagination and sort
     * @return a page of the candidate's applications
     */
    Page<JobApplication> findByCandidateIdAndIsDeletedFalse(UUID candidateId, Pageable pageable);

    /**
     * Counts applications grouped by pipeline stage for a specific job.
     * Used to generate hiring funnel analytics (bar chart / funnel chart data).
     *
     * <p>Returns a list of Object[] where each element is
     * {@code [PipelineStage stage, Long count]}.
     *
     * @param jobId the job's UUID
     * @return list of stage-count pairs
     */
    @Query("""
            SELECT a.pipelineStage, COUNT(a)
            FROM JobApplication a
            WHERE a.job.id = :jobId
              AND a.isDeleted = false
            GROUP BY a.pipelineStage
            """)
    List<Object[]> countByPipelineStageForJob(@Param("jobId") UUID jobId);

    /**
     * Counts applications per status for dashboard statistics.
     * Used by the admin dashboard to show the overall application funnel.
     *
     * @return list of [ApplicationStatus, Long count] pairs
     */
    @Query("""
            SELECT a.status, COUNT(a)
            FROM JobApplication a
            WHERE a.isDeleted = false
            GROUP BY a.status
            """)
    List<Object[]> countByStatusForDashboard();

    /**
     * Checks whether any active (non-deleted) application exists for a candidate.
     *
     * @param candidateId the candidate's UUID
     * @return true if an active application exists
     */
    boolean existsByCandidateIdAndIsDeletedFalse(UUID candidateId);

    /**
     * Retrieves a paginated list of active job applications with filtering.
     *
     * @param candidateId optional filter by candidate
     * @param jobId       optional filter by job
     * @param companyId   optional filter by company
     * @param status      optional filter by status
     * @param pageable    pagination and sorting
     * @return page of matching active job applications
     */
    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.isDeleted = false
              AND (:candidateId IS NULL OR a.candidate.id = :candidateId)
              AND (:jobId IS NULL       OR a.job.id = :jobId)
              AND (:companyId IS NULL   OR a.job.company.id = :companyId)
              AND (:status IS NULL      OR a.status = :status)
            """)
    Page<JobApplication> findAllActiveWithFilters(
            @Param("candidateId") UUID candidateId,
            @Param("jobId") UUID jobId,
            @Param("companyId") UUID companyId,
            @Param("status") ApplicationStatus status,
            Pageable pageable
    );

    /**
     * Finds active applications for the pipeline view filtered by optional jobId, companyId, and recruiterId.
     */
    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.isDeleted = false
              AND (:jobId IS NULL       OR a.job.id = :jobId)
              AND (:companyId IS NULL   OR a.job.company.id = :companyId)
              AND (:recruiterId IS NULL OR a.job.createdByUser.id = :recruiterId)
            """)
    List<JobApplication> findActiveApplicationsForPipeline(
            @Param("jobId") UUID jobId,
            @Param("companyId") UUID companyId,
            @Param("recruiterId") UUID recruiterId
    );

    /**
     * Counts active applications grouped by status for pipeline summary.
     */
    @Query("""
            SELECT a.status, COUNT(a)
            FROM JobApplication a
            WHERE a.isDeleted = false
              AND (:jobId IS NULL       OR a.job.id = :jobId)
              AND (:companyId IS NULL   OR a.job.company.id = :companyId)
              AND (:recruiterId IS NULL OR a.job.createdByUser.id = :recruiterId)
            GROUP BY a.status
            """)
    List<Object[]> countByStatusForPipeline(
            @Param("jobId") UUID jobId,
            @Param("companyId") UUID companyId,
            @Param("recruiterId") UUID recruiterId
    );

    /**
     * Counts all non-deleted job applications.
     */
    long countByIsDeletedFalse();
}
