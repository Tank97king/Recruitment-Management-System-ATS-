package com.example.ats.repository;

import com.example.ats.entity.Interview;
import com.example.ats.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.ats.enums.InterviewType;

/**
 * Spring Data JPA repository for the {@link Interview} entity.
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    /**
     * Finds an active (non-deleted) interview by ID.
     *
     * @param id the interview UUID
     * @return the interview if found and active
     */
    Optional<Interview> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all active interviews for a specific job application.
     * Used to list interview rounds on the application details page.
     *
     * @param applicationId the job application UUID
     * @return list of active interviews
     */
    List<Interview> findByApplicationIdAndIsDeletedFalseOrderByScheduledAtAsc(UUID applicationId);

    /**
     * Finds all upcoming scheduled interviews within a specific time range.
     * Useful for calendar views or sending daily reminders.
     *
     * @param start  start of the time range
     * @param end    end of the time range
     * @param status status of the interview (usually SCHEDULED)
     * @return list of interviews in the range
     */
    List<Interview> findByScheduledAtBetweenAndStatusAndIsDeletedFalse(
            Instant start,
            Instant end,
            InterviewStatus status
    );

    /**
     * Finds upcoming scheduled interviews where the user is listed in interviewerNames.
     * Since interviewerNames is a comma-separated text string, we use LIKE for search.
     *
     * @param interviewerName the name/email of the interviewer
     * @param status          status of the interview (usually SCHEDULED)
     * @param pageable        pagination and sorting
     * @return page of interviews
     */
    @Query("""
            SELECT i FROM Interview i
            WHERE i.isDeleted = false
              AND i.status = :status
              AND LOWER(i.interviewerNames) LIKE LOWER(CONCAT('%', :interviewerName, '%'))
            """)
    Page<Interview> findUpcomingByInterviewer(
            @Param("interviewerName") String interviewerName,
            @Param("status") InterviewStatus status,
            Pageable pageable
    );

    /**
     * Retrieves a paginated list of active interviews with optional search filters.
     *
     * @param candidateId     optional candidate ID filter
     * @param companyId       optional company ID filter
     * @param interviewStatus optional status filter
     * @param interviewType   optional type filter
     * @param startDate       optional start of date range filter
     * @param endDate         optional end of date range filter
     * @param pageable        pagination and sorting
     * @return page of matching active interviews
     */
    @Query("""
            SELECT i FROM Interview i
            WHERE i.isDeleted = false
              AND (:candidateId IS NULL     OR i.application.candidate.id = :candidateId)
              AND (:companyId IS NULL       OR i.application.job.company.id = :companyId)
              AND (:interviewStatus IS NULL OR i.status = :interviewStatus)
              AND (:interviewType IS NULL   OR i.interviewType = :interviewType)
              AND (:startDate IS NULL       OR i.scheduledAt >= :startDate)
              AND (:endDate IS NULL         OR i.scheduledAt < :endDate)
            """)
    Page<Interview> findAllActiveWithFilters(
            @Param("candidateId") UUID candidateId,
            @Param("companyId") UUID companyId,
            @Param("interviewStatus") InterviewStatus interviewStatus,
            @Param("interviewType") InterviewType interviewType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Counts all non-deleted interviews.
     */
    long countByIsDeletedFalse();
}
