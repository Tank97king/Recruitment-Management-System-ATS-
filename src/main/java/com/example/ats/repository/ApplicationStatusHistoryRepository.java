package com.example.ats.repository;

import com.example.ats.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link ApplicationStatusHistory} entity.
 *
 * <p>History records are append-only — this repository only supports
 * INSERT (via {@code save()}) and SELECT operations. No UPDATE or DELETE
 * operations should be called on existing history records.
 */
@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {

    /**
     * Retrieves the complete status change history for a specific application,
     * ordered from oldest to newest.
     *
     * <p>Used to populate the "Application Timeline" view on the application detail page
     * and the "Activity Feed" on the recruiter dashboard.
     *
     * @param applicationId the application's UUID
     * @return ordered list of all history entries for this application
     */
    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtAsc(UUID applicationId);

    /**
     * Retrieves the most recent status change entry for an application.
     * Used to display the "last updated" status on the application card.
     *
     * @param applicationId the application's UUID
     * @return the most recent history entry, or empty if none exists
     */
    java.util.Optional<ApplicationStatusHistory> findTopByApplicationIdOrderByChangedAtDesc(UUID applicationId);
}
