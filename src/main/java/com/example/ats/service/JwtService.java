package com.example.ats.service;

import com.example.ats.config.JwtProperties;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service handling JWT access token generation, parsing, and signing.
 *
 * <p>Uses io.jsonwebtoken (JJWT) version 0.12.x APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey cachedKey;

    /**
     * Resolves the signing key from configured JwtProperties.
     * Supports both raw text keys and Base64-encoded keys seamlessly.
     *
     * @return the HMAC-SHA signing SecretKey
     */
    private SecretKey getSigningKey() {
        if (cachedKey != null) {
            return cachedKey;
        }

        String secret = jwtProperties.getSecret();
        byte[] keyBytes;
        try {
            // Attempt to decode as Base64 (recommended for production keys)
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                // If decoded key is too short, fallback to raw string bytes
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            // If secret is not valid Base64, treat as raw text bytes
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        // HMAC-SHA256 requires at least 256 bits (32 bytes)
        if (keyBytes.length < 32) {
            log.warn("JWT secret key is too short ({} bytes). Minimum required is 32 bytes for HMAC-SHA256.", keyBytes.length);
        }

        this.cachedKey = Keys.hmacShaKeyFor(keyBytes);
        return this.cachedKey;
    }

    /**
     * Generates a signed JWT access token for the authenticated user.
     *
     * <p>Includes claims:
     * <ul>
     *   <li>{@code sub} (subject): user's email</li>
     *   <li>{@code userId}: user's UUID</li>
     *   <li>{@code email}: user's email</li>
     *   <li>{@code role}: user's primary role name (e.g., RECRUITER or ADMIN)</li>
     * </ul>
     *
     * @param user the authenticated User entity
     * @return the signed compact JWT token string
     */
    public String generateAccessToken(User user) {
        log.debug("Generating access token for user: {}", user.getEmail());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        
        // Map roles to list of strings
        String rolesStr = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.joining(","));
        claims.put("role", rolesStr);

        long expirationMs = jwtProperties.getAccessTokenExpirationMs();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .header()
                    .add("typ", "JWT")
                .and()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Gets token expiration duration in seconds.
     *
     * @return expiration time in seconds (e.g., 3600 for 1 hour)
     */
    public long getExpirationInSeconds() {
        return jwtProperties.getAccessTokenExpirationMs() / 1000;
    }

    /**
     * Extracts all claims from a JWT access token.
     * Throws JwtException if the token is invalid or expired.
     *
     * @param token the compact JWT token string
     * @return the parsed Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the subject (user email) from the token.
     *
     * @param token the JWT token
     * @return the user's email address
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the userId claim from the token.
     *
     * @param token the JWT token
     * @return the user UUID string
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Extracts the comma-separated role name claim from the token.
     *
     * @param token the JWT token
     * @return the role name string (e.g., "RECRUITER" or "ADMIN")
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Validates whether the token is structurally valid, signatures are correct, and not expired.
     *
     * @param token the JWT token
     * @return true if token is fully valid
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates the token against the loaded UserDetails.
     * Checks that the email matches and the token is not expired.
     *
     * @param token       the JWT token
     * @param userDetails Spring Security UserDetails representation of the user
     * @return true if token is valid and matches the user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed against user: {}", e.getMessage());
            return false;
        }
    }
}
