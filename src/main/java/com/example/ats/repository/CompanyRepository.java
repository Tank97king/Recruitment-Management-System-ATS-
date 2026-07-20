package com.example.ats.repository;

import com.example.ats.entity.Company;
import com.example.ats.enums.CompanySize;
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
 * Spring Data JPA repository for the {@link Company} entity.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    /**
     * Finds an active company by ID.
     *
     * @param id the company's UUID
     * @return the company if found and not deleted
     */
    Optional<Company> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Checks whether an active company with the given name exists.
     * Used for unique name validation during company creation.
     *
     * @param name the company name (exact match, case-sensitive at DB level)
     * @return true if a non-deleted company with this name exists
     */
    boolean existsByNameAndIsDeletedFalse(String name);

    /**
     * Retrieves a paginated list of active companies with optional filters.
     *
     * @param industry   filter by industry (exact match); null means any
     * @param companySize filter by company size enum; null means any
     * @param search     substring to match against company name (case-insensitive)
     * @param pageable   pagination and sorting
     * @return a page of matching companies
     */
    @Query("""
            SELECT c FROM Company c
            WHERE c.isDeleted = false
              AND (:industry IS NULL   OR c.industry = :industry)
              AND (:companySize IS NULL OR c.companySize = :companySize)
              AND (:search IS NULL OR
                   LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.headquartersLocation) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Company> findAllActiveWithFilters(
            @Param("industry") String industry,
            @Param("companySize") CompanySize companySize,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Retrieves a paginated list of active companies matching search query in name, email, or address.
     *
     * @param search   substring to match against company name, email, or headquartersLocation (case-insensitive)
     * @param pageable pagination and sorting
     * @return a page of matching active companies
     */
    @Query("""
            SELECT c FROM Company c
            WHERE c.isDeleted = false
              AND (:search IS NULL OR
                   LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.headquartersLocation) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Company> findAllActiveWithSearch(
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Counts all non-deleted companies.
     */
    long countByIsDeletedFalse();

    /**
     * Counts companies that have at least one open non-deleted job.
     */
    @Query("""
            SELECT COUNT(DISTINCT c)
            FROM Company c
            JOIN Job j ON j.company = c
            WHERE c.isDeleted = false
              AND j.isDeleted = false
              AND j.status = com.example.ats.enums.JobStatus.OPEN
            """)
    long countCompaniesWithActiveJobs();
}
