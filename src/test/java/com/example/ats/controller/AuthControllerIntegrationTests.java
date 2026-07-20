package com.example.ats.controller;

import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clear database user records before each test (rollback ensures clean environment)
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register user when given valid request")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Registration successful")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.fullName", is("Jane Doe")))
                .andExpect(jsonPath("$.data.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.data.role", is("RECRUITER")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Should fail registration when passwords do not match")
    void shouldFailWhenPasswordsDoNotMatch() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .confirmPassword("differentPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")))
                .andExpect(jsonPath("$.errors[0].field", is("confirmPassword")))
                .andExpect(jsonPath("$.errors[0].message", is("Confirm password must match password")));
    }

    @Test
    @DisplayName("Should fail registration when email is duplicate")
    void shouldFailWhenEmailIsDuplicate() throws Exception {
        // Register first user
        RegisterRequest firstRequest = RegisterRequest.builder()
                .fullName("First User")
                .email("duplicate@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Register second user with same email
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .fullName("Second User")
                .email("duplicate@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Email address is already in use: duplicate@example.com")));
    }

    @Test
    @DisplayName("Should fail registration when email is invalid format")
    void shouldFailWhenEmailIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Invalid Email User")
                .email("invalid-email-format")
                .password("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")))
                .andExpect(jsonPath("$.errors[0].field", is("email")))
                .andExpect(jsonPath("$.errors[0].message", is("Email must be valid")));
    }

    @Test
    @DisplayName("Should fail registration when password is too short")
    void shouldFailWhenPasswordIsTooShort() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Short Password User")
                .email("short-password@example.com")
                .password("short")
                .confirmPassword("short")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")))
                .andExpect(jsonPath("$.errors[0].field", is("password")))
                .andExpect(jsonPath("$.errors[0].message", is("Password must be at least 8 characters")));
    }
}
