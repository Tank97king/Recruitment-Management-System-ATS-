package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a system user account.
 *
 * <p>Maps to the {@code users} table. A User is the authenticated actor
 * within the ATS — either an Admin or a Recruiter. This entity is the
 * central identity anchor; it is referenced by jobs, interviews, status
 * histories, and refresh tokens.
 *
 * <h3>Relationships</h3>
 *
 * <h4>User → Role (Many-to-Many, OWNING side)</h4>
 * <ul>
 *   <li>A user can hold multiple roles (e.g., ADMIN + RECRUITER).</li>
 *   <li>A role can be assigned to many users.</li>
 *   <li>{@code @JoinTable} lives here because User is the natural owning side
 *       — roles are assigned TO users, not the other way around.</li>
 *   <li>{@code FetchType.LAZY} is mandatory — eager loading roles on every
 *       user query would cause an extra SQL on every authentication check.</li>
 *   <li>{@code Set<Role>} instead of {@code List<Role>} — Hibernate's behavior
 *       with @ManyToMany + List is to DELETE ALL then re-INSERT all junction rows
 *       on every update. Set avoids this by issuing targeted INSERT/DELETE operations.</li>
 *   <li>No {@code CascadeType}: roles are independent seed data. Saving a user
 *       must never create or delete roles.</li>
 * </ul>
 *
 * <h3>Common JPA Mistakes Avoided</h3>
 * <ul>
 *   <li><strong>Never expose passwordHash in toString/serialization:</strong>
 *       {@code @ToString.Exclude} ensures the hash is never accidentally logged.</li>
 *   <li><strong>Initialize collection to empty HashSet:</strong> Prevents
 *       {@code NullPointerException} when calling {@code user.getRoles().add(role)}
 *       before the entity is saved. Always initialize collections in the field
 *       declaration, not in a constructor.</li>
 *   <li><strong>FetchType.EAGER on collections is banned:</strong> Would execute
 *       an additional SQL query for every single User query in the system.</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"passwordHash", "roles"})
public class User extends BaseEntity {

    // ─── Core Identity Fields ─────────────────────────────────────────────

    /**
     * Unique email address used as the login identifier.
     *
     * <p>The uniqueness constraint is a partial index in PostgreSQL
     * ({@code WHERE is_deleted = FALSE}) so that a soft-deleted user's
     * email can be reregistered. The {@code unique = true} here applies
     * a full unique constraint; the partial index is defined in the Flyway
     * migration SQL.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt-hashed password. Never stores plaintext.
     *
     * <p>This field is excluded from {@code toString()} to prevent the
     * hash from appearing in log files or error messages.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    // ─── Account State ────────────────────────────────────────────────────

    /**
     * Account activation status.
     * INACTIVE accounts are rejected at the authentication filter before
     * password verification is attempted.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Soft-delete flag. When {@code true}, the user is logically deleted.
     * Queries should always include {@code WHERE is_deleted = FALSE}.
     *
     * <p>The Hibernate {@code @SQLRestriction} annotation can be added here
     * to automatically filter deleted users, but we handle this explicitly
     * in repository queries for clarity.
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    // ─── Relationships ────────────────────────────────────────────────────

    /**
     * The roles assigned to this user.
     *
     * <p><strong>Design choices:</strong>
     * <ul>
     *   <li>{@code FetchType.LAZY} — roles are loaded on-demand, not on every user SELECT.</li>
     *   <li>{@code Set<Role>} — prevents Hibernate's delete-all-reinsert-all
     *       behavior that occurs with @ManyToMany + List.</li>
     *   <li>No {@code CascadeType} — roles are seed data; user lifecycle
     *       must not affect role records.</li>
     *   <li>Initialized to empty {@code HashSet} — prevents NPE when adding
     *       roles to a new, unsaved User object.</li>
     * </ul>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "id",
                    nullable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "id",
                    nullable = false
            )
    )
    private Set<Role> roles = new HashSet<>();

    // ─── Convenience Methods ──────────────────────────────────────────────

    /**
     * Adds a role to this user, maintaining collection integrity.
     *
     * <p>Always use this helper method instead of calling
     * {@code user.getRoles().add(role)} directly — this ensures any future
     * bidirectional sync logic can be added here in one place.
     *
     * @param role the role to assign
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Removes a role from this user.
     *
     * @param role the role to revoke
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Checks if this user holds the specified role.
     *
     * @param roleName the role name to check
     * @return {@code true} if the user has this role
     */
    public boolean hasRole(com.example.ats.enums.RoleName roleName) {
        return this.roles.stream()
                .anyMatch(r -> r.getName() == roleName);
    }
}
