package com.example.ats.controller;

import com.example.ats.dto.request.UpdateUserRequest;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User recruiterUser;
    private String adminToken;
    private String recruiterToken;
    private Role adminRole;
    private Role recruiterRole;

    @BeforeEach
    void setUp() {
        // Clean up database tables
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // 1. Seed Roles
        adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN, "Admin Role")));
        recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter Role")));

        // 2. Create Admin User
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("hashed_password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.addRole(adminRole);
        adminUser = userRepository.saveAndFlush(adminUser);

        // 3. Create Recruiter User
        recruiterUser = new User();
        recruiterUser.setEmail("recruiter@example.com");
        recruiterUser.setPasswordHash("hashed_password");
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
    @DisplayName("GET /api/users - Should return paginated users for ADMIN")
    void getUsersShouldReturnPaginatedList() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Users retrieved successfully")))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.totalPages", is(1)))
                .andExpect(jsonPath("$.data.page", is(0)));
    }

    @Test
    @DisplayName("GET /api/users - Should filter users by search and status for ADMIN")
    void getUsersWithFiltersShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("search", "recruiter")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email", is("recruiter@example.com")));
    }

    @Test
    @DisplayName("GET /api/users - Should deny access to RECRUITER")
    void getUsersShouldDenyRecruiter() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("You do not have permission to perform this action.")));
    }

    @Test
    @DisplayName("GET /api/users - Should deny access to Unauthenticated users")
    void getUsersShouldDenyUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by ID for ADMIN")
    void getUserByIdShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(recruiterUser.getId().toString())))
                .andExpect(jsonPath("$.data.email", is("recruiter@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 for non-existent user ID")
    void getUserByIdShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/" + nonExistentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should deny access to RECRUITER")
    void getUserByIdShouldDenyRecruiter() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + adminUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update user successfully for ADMIN")
    void updateUserShouldUpdateSuccessfully() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Recruiter UpdatedName")
                .email("recruiter.new@example.com")
                .status(UserStatus.ACTIVE.name())
                .build();

        mockMvc.perform(put("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User updated successfully")))
                .andExpect(jsonPath("$.data.fullName", is("Recruiter UpdatedName")))
                .andExpect(jsonPath("$.data.email", is("recruiter.new@example.com")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should fail update when email is already taken")
    void updateUserShouldFailWithDuplicateEmail() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Recruiter UpdatedName")
                .email("admin@example.com") // Already used by adminUser
                .status(UserStatus.ACTIVE.name())
                .build();

        mockMvc.perform(put("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should fail update when validation fails (empty name)")
    void updateUserShouldFailWithValidationErrors() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("")
                .email("invalid-email")
                .status(UserStatus.ACTIVE.name())
                .build();

        mockMvc.perform(put("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should soft delete user successfully for ADMIN")
    void deleteUserShouldSoftDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User deleted successfully")));

        // Verify user is marked deleted by trying to fetch them (which should return 404)
        mockMvc.perform(get("/api/v1/users/" + recruiterUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return profile of currently logged-in admin")
    void getProfileShouldReturnOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User profile retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(adminUser.getId().toString())))
                .andExpect(jsonPath("$.data.email", is("admin@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/profile - Should return profile of currently logged-in recruiter")
    void getProfileShouldReturnOwnProfileForRecruiter() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User profile retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(recruiterUser.getId().toString())))
                .andExpect(jsonPath("$.data.email", is("recruiter@example.com")));
    }
}
