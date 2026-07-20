package com.example.ats.repository;

import com.example.ats.entity.Role;
import com.example.ats.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link Role} entity.
 *
 * <p>Roles are seed data — they are inserted once by Flyway migrations
 * and never created/deleted at runtime. This repository is used exclusively
 * for READ operations: loading roles to assign to users.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds a role by its name enum constant.
     * Used by {@code UserService} when assigning roles to newly created users.
     *
     * @param name the role name enum constant (ADMIN or RECRUITER)
     * @return the matching Role, or empty if not yet seeded
     */
    Optional<Role> findByName(RoleName name);
}
