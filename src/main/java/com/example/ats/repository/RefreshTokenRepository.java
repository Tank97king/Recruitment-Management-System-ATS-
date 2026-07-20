package com.example.ats.repository;

import com.example.ats.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link RefreshToken} entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a refresh token record by the token string.
     * Excludes revoked or expired tokens at the repository query level if required,
     * or loads the entity completely to evaluate validity in Java.
     *
     * @param token the raw or hashed token string
     * @return an Optional containing the RefreshToken record
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds the active (non-revoked, non-expired) refresh token for a user.
     *
     * @param userId the user's UUID
     * @param now    the current timestamp to check expiration
     * @return the active RefreshToken if present
     */
    @Query("""
            SELECT r FROM RefreshToken r
            WHERE r.user.id = :userId
              AND r.isRevoked = false
              AND r.expiresAt > :now
            """)
    Optional<RefreshToken> findActiveByUserId(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );

    /**
     * Revokes all active refresh tokens for a user.
     * Used on logout, password change, or when forced logout is triggered.
     *
     * <p>{@code @Modifying} is required for update or delete JPQL/SQL queries.
     * {@code clearAutomatically = true} ensures the persistence context (L1 cache)
     * is cleared after updates so Hibernate doesn't serve stale cached entities.
     *
     * @param userId    the user's UUID
     * @param revokedAt the timestamp of revocation
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE RefreshToken r
            SET r.isRevoked = true, r.revokedAt = :revokedAt
            WHERE r.user.id = :userId
              AND r.isRevoked = false
            """)
    void revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("revokedAt") Instant revokedAt
    );

    /**
     * Deletes all expired or revoked refresh tokens.
     * Used by a scheduled background cleanup job to keep the table size in check.
     *
     * @param now the cutoff timestamp (usually Instant.now())
     * @return the number of deleted records
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM RefreshToken r
            WHERE r.expiresAt < :now
               OR r.isRevoked = true
            """)
    int deleteAllExpiredOrRevoked(@Param("now") Instant now);
}
