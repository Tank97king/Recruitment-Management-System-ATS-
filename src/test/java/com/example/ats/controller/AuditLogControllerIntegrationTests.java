package com.example.ats.controller;

import com.example.ats.entity.AuditLog;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.AuditLogRepository;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtService jwtService;

    private User admin;
    private String adminToken;
    private User recruiter;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN, "Admin")));
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));

        admin = new User();
        admin.setEmail("admin.audit@example.com");
        admin.setPasswordHash("password");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setStatus(UserStatus.ACTIVE);
        admin.addRole(adminRole);
        admin = userRepository.saveAndFlush(admin);
        adminToken = jwtService.generateAccessToken(admin);

        recruiter = new User();
        recruiter.setEmail("recruiter.audit@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("User");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Seed sample audit log
        auditLogRepository.save(AuditLog.builder()
                .userId(admin.getId())
                .userEmail(admin.getEmail())
                .action("COMPANY_CREATE")
                .resourceType("COMPANY")
                .resourceId("comp-123")
                .description("Created test company")
                .ipAddress("127.0.0.1")
                .createdAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("GET /api/audit-logs - Should allow ADMIN to query audit logs")
    void shouldAllowAdminToQueryAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("action", "COMPANY_CREATE")
                        .param("resourceType", "COMPANY")
                        .param("userEmail", "admin.audit@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].action").value("COMPANY_CREATE"))
                .andExpect(jsonPath("$.data.content[0].userEmail").value("admin.audit@example.com"));
    }

    @Test
    @DisplayName("GET /api/audit-logs - Should return 403 Forbidden for non-ADMIN user")
    void shouldReturn403ForNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
