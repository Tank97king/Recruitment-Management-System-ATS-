package com.example.ats.controller;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.AuthService;
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
@DisplayName("AuthController Login Integration Tests")
class AuthControllerLoginIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Register a test user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("John Recruiter")
                .email("john.recruiter@example.com")
                .password("securePassword123")
                .confirmPassword("securePassword123")
                .build();
        authService.register(registerRequest);
    }

    @Test
    @DisplayName("Should successfully login when given valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john.recruiter@example.com")
                .password("securePassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.expiresIn", is(3600))) // 3600 seconds = 1 hour
                .andExpect(jsonPath("$.data.userId", notNullValue()))
                .andExpect(jsonPath("$.data.fullName", is("John Recruiter")))
                .andExpect(jsonPath("$.data.email", is("john.recruiter@example.com")))
                .andExpect(jsonPath("$.data.role", is("RECRUITER")));
    }

    @Test
    @DisplayName("Should fail login when password is incorrect")
    void shouldFailWhenPasswordIsIncorrect() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("john.recruiter@example.com")
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                // Spring Security's BadCredentialsException message defaults to "Bad credentials"
                .andExpect(jsonPath("$.message", is("Bad credentials")));
    }

    @Test
    @DisplayName("Should fail login when email does not exist")
    void shouldFailWhenEmailDoesNotExist() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("securePassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Bad credentials")));
    }

    @Test
    @DisplayName("Should fail login when user status is INACTIVE")
    void shouldFailWhenUserIsInactive() throws Exception {
        // Change user status to INACTIVE
        User user = userRepository.findByEmailAndIsDeletedFalse("john.recruiter@example.com")
                .orElseThrow();
        user.setStatus(UserStatus.INACTIVE);
        userRepository.saveAndFlush(user);

        LoginRequest request = LoginRequest.builder()
                .email("john.recruiter@example.com")
                .password("securePassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                // Spring Security's DisabledException message defaults to "User is disabled"
                .andExpect(jsonPath("$.message", is("User is disabled")));
    }

    @Test
    @DisplayName("Should fail login when input validation fails")
    void shouldFailWhenValidationFails() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("invalid-email")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")));
    }
}
