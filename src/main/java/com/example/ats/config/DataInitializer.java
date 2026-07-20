package com.example.ats.config;

import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds default roles and admin/recruiter user accounts on startup
 * if they do not already exist in the database (e.g. when running with H2 or a fresh database).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Verifying system roles and default user accounts...");

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role r = new Role(RoleName.ADMIN, "Full system access");
                    log.info("Created missing default role: ADMIN");
                    return roleRepository.save(r);
                });

        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> {
                    Role r = new Role(RoleName.RECRUITER, "Recruitment operations access");
                    log.info("Created missing default role: RECRUITER");
                    return roleRepository.save(r);
                });

        if (userRepository.findByEmailAndIsDeletedFalse("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setStatus(UserStatus.ACTIVE);
            admin.setIsDeleted(false);
            admin.addRole(adminRole);
            admin.addRole(recruiterRole);
            userRepository.save(admin);
            log.info("Created default admin user: admin@example.com / password");
        }

        if (userRepository.findByEmailAndIsDeletedFalse("recruiter@example.com").isEmpty()) {
            User recruiter = new User();
            recruiter.setEmail("recruiter@example.com");
            recruiter.setPasswordHash(passwordEncoder.encode("password"));
            recruiter.setFirstName("Recruiter");
            recruiter.setLastName("User");
            recruiter.setStatus(UserStatus.ACTIVE);
            recruiter.setIsDeleted(false);
            recruiter.addRole(recruiterRole);
            userRepository.save(recruiter);
            log.info("Created default recruiter user: recruiter@example.com / password");
        }
    }
}
