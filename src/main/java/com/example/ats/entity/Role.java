package com.example.ats.entity;

import com.example.ats.entity.base.BaseEntity;
import com.example.ats.enums.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a permission role in the ATS system.
 *
 * <p>Maps to the {@code roles} table. Roles are seed data — they are
 * created once via Flyway migrations and never modified at runtime.
 * The system currently defines two roles:
 * <ul>
 *   <li>{@link RoleName#ADMIN}     — Full system access.</li>
 *   <li>{@link RoleName#RECRUITER} — Day-to-day hiring operations.</li>
 * </ul>
 *
 * <h3>Relationship</h3>
 * <p>{@code Role} is the <em>inverse</em> (non-owning) side of the
 * {@code User ↔ Role} Many-to-Many relationship. The owning side with the
 * {@code @JoinTable} annotation lives on the {@link User} entity. We deliberately
 * do NOT map {@code Set<User> users} here — loading all users when fetching a
 * role would be both inefficient and semantically wrong for this use case.
 *
 * <h3>Common JPA Mistake Avoided</h3>
 * <p>Not cascading from Role to User. Roles are independent seed data;
 * persisting/deleting a role must never affect user records.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Role extends BaseEntity {

    /**
     * The named role identifier stored as its enum constant name.
     *
     * <p>{@code @Enumerated(EnumType.STRING)} stores the enum constant's
     * string name (e.g., {@code "ADMIN"}) rather than its ordinal integer.
     *
     * <p><strong>Common JPA Mistake Avoided:</strong> Never use
     * {@code EnumType.ORDINAL} for enum columns. If a new value is inserted
     * at any position in the enum, all subsequent ordinal values shift,
     * silently corrupting every existing row in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleName name;

    /**
     * Human-readable description of what this role can do.
     * Optional; used for display purposes only.
     */
    @Column(name = "description", length = 500)
    private String description;

    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }
}
