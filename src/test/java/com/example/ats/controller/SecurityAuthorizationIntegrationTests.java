package com.example.ats.controller;

import com.example.ats.config.JwtProperties;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Spring Security Authorization Integration Tests")
class SecurityAuthorizationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    private User adminUser;
    private User recruiterUser;

    private String adminToken;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // 1. Seed Roles
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN, "Admin")));
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));

        // 2. Create Admin user
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("hash");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.addRole(adminRole);
        adminUser = userRepository.saveAndFlush(adminUser);

        // 3. Create Recruiter user
        recruiterUser = new User();
        recruiterUser.setEmail("recruiter@example.com");
        recruiterUser.setPasswordHash("hash");
        recruiterUser.setFirstName("Recruiter");
        recruiterUser.setLastName("User");
        recruiterUser.setStatus(UserStatus.ACTIVE);
        recruiterUser.addRole(recruiterRole);
        recruiterUser = userRepository.saveAndFlush(recruiterUser);

        // 4. Generate Tokens
        adminToken = jwtService.generateAccessToken(adminUser);
        recruiterToken = jwtService.generateAccessToken(recruiterUser);
    }

    @Test
    @DisplayName("Should block access with 401 Unauthorized when missing Authorization header on protected URL")
    void shouldBlockWhenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Authentication required. Please provide a valid Bearer token.")));
    }

    @Test
    @DisplayName("Should block access with 401 Unauthorized when token is invalid format")
    void shouldBlockWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-string"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid JWT token.")));
    }

    @Test
    @DisplayName("Should block access with 401 Unauthorized when token has expired")
    void shouldBlockWhenTokenIsExpired() throws Exception {
        // Create an expired token manually using same secret config
        String secret = jwtProperties.getSecret();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", recruiterUser.getId().toString());
        claims.put("email", recruiterUser.getEmail());
        claims.put("role", "RECRUITER");

        Date now = new Date();
        // Expired 1 hour ago
        Date expiryDate = new Date(now.getTime() - 3600000);

        String expiredToken = Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(claims)
                .subject(recruiterUser.getEmail())
                .issuedAt(new Date(now.getTime() - 7200000))
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("JWT token has expired. Please log in again.")));
    }

    @Test
    @DisplayName("Should block access with 403 Forbidden when RECRUITER attempts to access ADMIN-only route")
    void shouldBlockRecruiterFromAdminRoute() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("You do not have permission to perform this action.")));
    }

    @Test
    @DisplayName("Should permit access for ADMIN to access ADMIN-only route")
    void shouldPermitAdminToAdminRoute() throws Exception {
        // Since there is no actual Users Controller implemented yet, a successful authorization check
        // will pass security filters and fail on Spring MVC routing (404 NOT FOUND),
        // whereas a blocked authorization would fail on Spring Security filters (403 FORBIDDEN).
        // Hence, expecting 404 confirms authorization passed successfully!
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should permit access for both ADMIN and RECRUITER to business resource route")
    void shouldPermitBothToBusinessRoute() throws Exception {
        // Business resource /api/v1/companies is not implemented yet so it returns 404 (Not Found)
        // when authorized, confirming it successfully bypassed the Security Filters!
        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isNotFound());
    }
}
