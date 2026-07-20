package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * JPA entity representing a server-side JWT refresh token record.
 *
 * <p>Maps to the {@code refresh_tokens} table. Storing refresh tokens
 * in the database enables server-side revocation — the key feature that
 * pure stateless JWT cannot support. When a user logs out, the corresponding
 * refresh token row is marked as revoked ({@code isRevoked = true}).
 *
 * <h3>Why Server-Side Refresh Tokens?</h3>
 * <p>A pure JWT access token cannot be invalidated before its expiry because
 * there is no server-side state to check against. By storing refresh tokens:
 * <ul>
 *   <li>Logout actually invalidates the token — the next refresh attempt fails.</li>
 *   <li>Compromised tokens can be revoked by an admin.</li>
 *   <li>All sessions for a user can be invalidated at once (e.g., after a password change).</li>
 * </ul>
 *
 * <h3>Relationship</h3>
 *
 * <h4>RefreshToken → User (Many-to-One, OWNING)</h4>
 * <ul>
 *   <li>Many refresh tokens can belong to one user (one per active device/session).</li>
 *   <li>FK {@code user_id} in this table — RefreshToken is the owning side.</li>
 *   <li>{@code FetchType.LAZY} — load user details only when needed for token validation.</li>
 *   <li>At the DB level, {@code ON DELETE CASCADE} removes tokens when the user is deleted.</li>
 * </ul>
 *
 * <h3>Security Notes</h3>
 * <ul>
 *   <li>The {@code token} column stores the raw UUID token string. In a high-security
 *       system, you would store {@code SHA-256(raw_token)} and compare hashes.
 *       For portfolio scope, raw token storage is acceptable.</li>
 *   <li>A background cleanup task should periodically delete expired and revoked tokens
 *       older than 30 days to prevent table bloat.</li>
 *   <li>The {@code token} column has a UNIQUE constraint — duplicate tokens cannot exist.</li>
 * </ul>
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li>Not including {@code token} in {@code toString()} — refresh tokens are
 *       sensitive credentials and must never appear in log files.</li>
 *   <li>Using {@code Instant} for {@code expiresAt} and {@code revokedAt} —
 *       timezone-safe, maps to {@code TIMESTAMPTZ}.</li>
 * </ul>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"token", "user"})
public class RefreshToken extends BaseEntity {

    // ─── Relationship ─────────────────────────────────────────────────────

    /**
     * The user who owns this refresh token.
     *
     * <p>{@code FetchType.LAZY} — token validation only needs the user's
     * ID and status, which can be loaded with a targeted query rather than
     * via this association.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ─── Token Data ───────────────────────────────────────────────────────

    /**
     * The refresh token string.
     * Excluded from {@code toString()} — it is a credential and must not be logged.
     * Unique across all rows — duplicate tokens cannot be issued.
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    /**
     * The timestamp when this token expires.
     * After this point, the token is rejected even if not explicitly revoked.
     * Default is 7 days from issuance (configured in AppProperties).
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Whether this token has been explicitly revoked.
     * Set to {@code true} on logout or when a new token is issued via rotation.
     */
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = Boolean.FALSE;

    /**
     * Timestamp when this token was revoked.
     * Null until the token is revoked. Used for cleanup queries:
     * delete tokens that have been revoked for more than 30 days.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Optional User-Agent string from the device that created the token.
     * Useful for security dashboards showing "active sessions by device".
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    // ─── Convenience Methods ──────────────────────────────────────────────

    /**
     * Checks whether this token is still valid for use.
     *
     * @return {@code true} if the token is neither revoked nor expired
     */
    public boolean isValid() {
        return !isRevoked && Instant.now().isBefore(expiresAt);
    }

    /**
     * Marks this token as revoked and records the revocation timestamp.
     */
    public void revoke() {
        this.isRevoked = Boolean.TRUE;
        this.revokedAt = Instant.now();
    }
}
