package com.example.ats.service;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.dto.response.AuthResponse;
import com.example.ats.dto.response.LoginResponse;

/**
 * Service interface handling all authentication and authorization operations.
 *
 * <p>Includes registration, and will be extended with login, token refresh,
 * and logout functionality in subsequent steps.
 */
public interface AuthService {

    /**
     * Registers a new user into the system.
     *
     * <p>Validates that the email is not already registered, hashes the password
     * using BCrypt, assigns the default {@code RECRUITER} role and {@code ACTIVE}
     * status, and persists the new user.
     *
     * @param request the registration details
     * @return the registration details response DTO
     * @throws com.example.ats.exception.DuplicateResourceException if the email is already in use
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user's credentials and issues a JWT access token.
     *
     * @param request the email and password credentials
     * @return the LoginResponse containing access token and user metadata
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refreshes an expired JWT access token using a valid, non-expired, non-revoked refresh token.
     * Generates a new access token and a new rotated refresh token.
     *
     * @param request the token refresh request containing the refresh token string
     * @return the TokenRefreshResponse containing the new access and refresh tokens
     */
    com.example.ats.dto.response.TokenRefreshResponse refreshToken(com.example.ats.dto.request.TokenRefreshRequest request);
}
