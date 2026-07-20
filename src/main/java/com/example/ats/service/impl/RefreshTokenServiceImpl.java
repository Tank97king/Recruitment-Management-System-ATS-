package com.example.ats.service.impl;

import com.example.ats.config.JwtProperties;
import com.example.ats.entity.RefreshToken;
import com.example.ats.entity.User;
import com.example.ats.repository.RefreshTokenRepository;
import com.example.ats.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation managing JWT refresh tokens stored in PostgreSQL database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.info("Generating refresh token for user: {}", user.getEmail());

        // 1. Enforce single-session token rotation: revoke any existing active tokens for this user
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());

        // 2. Build new RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        
        // Expiration is configured in application.yml (app.jwt.refresh-token-expiration-ms)
        long expirationMs = jwtProperties.getRefreshTokenExpirationMs();
        refreshToken.setExpiresAt(Instant.now().plusMillis(expirationMs));
        
        refreshToken.setIsRevoked(false);

        // 3. Persist and return
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("New refresh token created and persisted successfully.");
        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (!token.isValid()) {
            log.warn("Refresh token is expired or revoked. Removing from database: {}", token.getToken());
            
            // Physical deletion of expired/invalid token to keep db clean
            refreshTokenRepository.delete(token);
            
            // Standard Spring Security AuthenticationException subclass representing expired credentials
            throw new CredentialsExpiredException("Refresh token was expired or revoked. Please log in again.");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteAllExpiredOrRevoked(Instant.now());
        log.info("Cron cleanup: Deleted {} expired or revoked refresh tokens.", deletedCount);
    }

    @Override
    @Transactional
    public void deleteUserTokens(UUID userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }
}
