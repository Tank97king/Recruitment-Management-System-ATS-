package com.example.ats.controller;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.dto.request.TokenRefreshRequest;
import com.example.ats.entity.RefreshToken;
import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.RefreshTokenRepository;
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

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Refresh Token Integration Tests")
class AuthControllerRefreshTokenIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthService authService;

    private User testUser;
    private String validRefreshTokenString;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register test user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("John Recruiter")
                .email("john.recruiter@example.com")
                .password("securePassword123")
                .confirmPassword("securePassword123")
                .build();
        authService.register(registerRequest);

        // 2. Login to generate tokens
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.recruiter@example.com")
                .password("securePassword123")
                .build();
        
        var loginResponse = authService.login(loginRequest);
        validRefreshTokenString = loginResponse.getRefreshToken();
        
        testUser = userRepository.findByEmailAndIsDeletedFalse("john.recruiter@example.com").orElseThrow();
    }

    @Test
    @DisplayName("Should successfully refresh token and rotate refresh token when valid")
    void shouldRefreshSuccessfully() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken(validRefreshTokenString)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Token refreshed successfully")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.expiresIn", is(3600)));

        // Verify the old token is now revoked (token rotation)
        var oldToken = refreshTokenRepository.findByToken(validRefreshTokenString);
        assertTrue(oldToken.isPresent());
        assertTrue(oldToken.get().getIsRevoked());
    }

    @Test
    @DisplayName("Should fail to refresh token when token is expired and delete it from DB")
    void shouldFailWhenTokenIsExpired() throws Exception {
        // Manually update the token's expiration to the past in database
        RefreshToken token = refreshTokenRepository.findByToken(validRefreshTokenString).orElseThrow();
        token.setExpiresAt(Instant.now().minusSeconds(100));
        refreshTokenRepository.saveAndFlush(token);

        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken(validRefreshTokenString)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Refresh token was expired or revoked. Please log in again.")));

        // Verify the expired token has been deleted from the database
        assertFalse(refreshTokenRepository.findByToken(validRefreshTokenString).isPresent());
    }

    @Test
    @DisplayName("Should fail to refresh token when token is invalid or non-existent")
    void shouldFailWhenTokenIsInvalid() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken(UUID.randomUUID().toString())
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid refresh token")));
    }

    @Test
    @DisplayName("Should fail with 400 Bad Request when request body input validation fails")
    void shouldFailWhenValidationFails() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("")
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")));
    }
}
