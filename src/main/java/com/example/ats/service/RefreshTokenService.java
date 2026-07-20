package com.example.ats.service;

import com.example.ats.entity.RefreshToken;
import com.example.ats.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Service contract managing JWT refresh tokens.
 *
 * <p>Handles generating, saving, validating, retrieving, and cleaning up refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Generates a new unique refresh token for a user and saves it to the database.
     * Revokes any existing active refresh tokens for the user (single active session enforcement).
     *
     * @param user the user requesting the token
     * @return the generated RefreshToken entity
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Finds a refresh token by its raw token string.
     *
     * @param token the token string
     * @return an Optional containing the RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Validates whether a refresh token is valid (neither expired nor revoked).
     * If expired, deletes it from the database and throws an exception.
     *
     * @param token the RefreshToken entity to validate
     * @return the validated RefreshToken entity
     * @throws org.springframework.security.core.AuthenticationException if expired or revoked
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Deletes all expired or revoked refresh tokens from the database.
     * Used by cron schedules to keep database size in check.
     */
    void deleteExpiredTokens();

    /**
     * Revokes and deletes all refresh tokens belonging to a user.
     *
     * @param userId the user UUID
     */
    void deleteUserTokens(UUID userId);
}
