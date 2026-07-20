package com.example.ats.repository;

import com.example.ats.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Candidate} entity.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID>, JpaSpecificationExecutor<Candidate> {

    /**
     * Finds a non-deleted candidate by ID.
     *
     * @param id the candidate's UUID
     * @return the candidate if active
     */
    Optional<Candidate> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks whether an active candidate with this email already exists.
     * Used to prevent duplicate candidate profiles.
     *
     * @param email the candidate's email
     * @return true if an active candidate with this email exists
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * Retrieves a paginated list of active candidates with optional filters.
     *
     * <p>Supports searching by name, email, current title/company, and location.
     * Supports filtering by experience level (years of experience range).
     *
     * @param search          keyword to match against multiple text fields
     * @param minExperience   minimum years of experience filter; null = no minimum
     * @param maxExperience   maximum years of experience filter; null = no maximum
     * @param pageable        pagination and sort
     * @return a page of matching non-deleted candidates
     */
    @Query("""
            SELECT c FROM Candidate c
            WHERE c.isDeleted = false
              AND (:minExperience IS NULL OR c.yearsOfExperience >= :minExperience)
              AND (:maxExperience IS NULL OR c.yearsOfExperience <= :maxExperience)
              AND (:search IS NULL OR
                   LOWER(c.firstName)      LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.lastName)       LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.email)          LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.currentTitle)   LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.currentCompany) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Candidate> findAllActiveWithFilters(
            @Param("search") String search,
            @Param("minExperience") Short minExperience,
            @Param("maxExperience") Short maxExperience,
            Pageable pageable
    );

    /**
     * Finds all candidates who have the specified skill tag.
     * Case-insensitive comparison via LOWER().
     *
     * @param tag      the skill tag to search for (e.g., "java", "spring boot")
     * @param pageable pagination and sort
     * @return a page of candidates with the given tag
     */
    @Query("""
            SELECT DISTINCT c FROM Candidate c
            JOIN c.tags t
            WHERE c.isDeleted = false
              AND LOWER(t.tag) = LOWER(:tag)
            """)
    Page<Candidate> findAllByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * Retrieves a paginated list of active candidates matching search query in name, email, or phone.
     *
     * @param search   substring to match against full name, email, or phone (case-insensitive)
     * @param pageable pagination and sorting
     * @return a page of matching active candidates
     */
    @Query("""
            SELECT c FROM Candidate c
            WHERE c.isDeleted = false
              AND (:search IS NULL OR
                   LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   c.phone LIKE CONCAT('%', :search, '%'))
            """)
    Page<Candidate> findAllActiveWithSearch(
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Counts all non-deleted candidates.
     */
    long countByIsDeletedFalse();

    /**
     * Counts non-deleted candidates who have uploaded a CV.
     */
    long countByCvNotNullAndIsDeletedFalse();
}
