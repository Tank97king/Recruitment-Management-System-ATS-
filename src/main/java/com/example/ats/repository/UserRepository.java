package com.example.ats.repository;

import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entity.
 *
 * <p>Extends {@link JpaRepository} to inherit full CRUD operations,
 * pagination/sorting support, and batch operations without any boilerplate code.
 *
 * <h3>Naming Convention for Query Methods</h3>
 * <p>Spring Data derives queries from method names:
 * <ul>
 *   <li>{@code findBy} = SELECT WHERE</li>
 *   <li>{@code existsBy} = SELECT EXISTS WHERE</li>
 *   <li>{@code countBy} = SELECT COUNT WHERE</li>
 *   <li>{@code And}, {@code Or} = SQL AND/OR operators</li>
 *   <li>{@code IgnoreCase} = LOWER() comparison</li>
 *   <li>{@code Containing} = LIKE %value%</li>
 * </ul>
 *
 * <h3>Custom @Query</h3>
 * <p>For complex queries that cannot be expressed via method naming,
 * {@code @Query} with JPQL (Java Persistence Query Language) is used.
 * JPQL operates on entity class names and field names, NOT table/column names.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds an active, non-deleted user by their email address.
     * Used by {@code UserDetailsServiceImpl} during JWT authentication.
     *
     * @param email the user's email (case-sensitive in this query)
     * @return an Optional containing the user, or empty if not found or deleted
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Checks whether an active user with the given email already exists.
     * Used for duplicate email validation during user creation.
     *
     * @param email the email to check
     * @return true if an active user with this email exists
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * Retrieves a paginated list of active users, filterable by status and
     * searchable by name or email substring.
     *
     * <p>JPQL query using {@code LOWER()} for case-insensitive search.
     * The {@code :search} parameter is matched against firstName, lastName, and email.
     * The {@code :status} is optional — null means "any status".
     *
     * @param status   optional status filter; pass null to include all statuses
     * @param search   substring to match against name/email (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return a page of matching users
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.isDeleted = false
              AND (:status IS NULL OR u.status = :status)
              AND (:search IS NULL OR
                   LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<User> findAllActiveWithFilters(
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Counts all non-deleted users.
     */
    long countByIsDeletedFalse();
}
