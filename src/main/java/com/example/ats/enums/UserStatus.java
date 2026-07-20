package com.example.ats.enums;

/**
 * Represents the activation state of a system user account.
 *
 * <p>Used by the authentication filter to reject login attempts
 * from INACTIVE accounts before password verification even occurs.
 *
 * <ul>
 *   <li>{@code ACTIVE}   — Account is enabled; login is permitted.</li>
 *   <li>{@code INACTIVE} — Account has been disabled by an Admin;
 *       login is rejected with 401 Unauthorized.</li>
 * </ul>
 */
public enum UserStatus {

    /** Account is fully operational. Default for all new accounts. */
    ACTIVE,

    /**
     * Account has been deactivated by an administrator.
     * The user's data is retained (soft-disabled, not deleted).
     * Reactivation is possible.
     */
    INACTIVE
}
