package com.example.ats.enums;

/**
 * Named permission roles available in the ATS system.
 *
 * <p>Roles are loaded from the {@code roles} seed table and assigned
 * to users via the {@code user_roles} junction table. Spring Security
 * uses these role names as granted authorities ({@code ROLE_ADMIN},
 * {@code ROLE_RECRUITER}) when evaluating {@code @PreAuthorize} expressions.
 *
 * <ul>
 *   <li>{@code ADMIN}     — Full system access. Manages users, companies,
 *       and can perform all destructive operations.</li>
 *   <li>{@code RECRUITER} — Day-to-day hiring operations. Cannot delete data
 *       or perform system administration.</li>
 * </ul>
 */
public enum RoleName {

    /**
     * System administrator with unrestricted access to all features.
     * Spring Security authority: {@code ROLE_ADMIN}.
     */
    ADMIN,

    /**
     * Hiring staff responsible for daily recruitment activities.
     * Spring Security authority: {@code ROLE_RECRUITER}.
     */
    RECRUITER
}
