package com.example.ats.controller;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.dto.request.TokenRefreshRequest;
import com.example.ats.dto.response.AuthResponse;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.LoginResponse;
import com.example.ats.dto.response.TokenRefreshResponse;
import com.example.ats.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ats.util.ApiConstants;

/**
 * Controller class exposing endpoints for authentication and registration.
 *
 * <p>Uses standard {@link com.example.ats.dto.response.ApiResponse} wrapper for
 * consistent API formatting.
 *
 * <p>All endpoints in this controller are <strong>public</strong> (no JWT required).
 */
@RestController
@RequestMapping(ApiConstants.AUTH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration, login, and token refresh endpoints. No JWT required.")
@SecurityRequirements   // No JWT needed for the entire auth controller
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new Recruiter account in the system.
     *
     * <p>Validates registration request payload using Jakarta validation annotations.
     * Maps error messages to a standardized API response structure when validation fails.
     *
     * @param request the registration details
     * @return the registered user details response wrapped in a standard ApiResponse
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register a new recruiter account",
        description = "Creates a new user account with the default **RECRUITER** role. " +
                      "The email must be unique. Password and confirmPassword must match. " +
                      "No JWT token required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Registration Success",
                    value = """
                        {
                          "success": true,
                          "message": "Registration successful",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "fullName": "John Doe",
                            "email": "john.doe@example.com",
                            "role": "RECRUITER",
                            "status": "ACTIVE"
                          }
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed — missing required fields, invalid email format, or password mismatch",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                        {
                          "success": false,
                          "status": 400,
                          "message": "Validation failed",
                          "errors": [
                            { "field": "email", "message": "Email must be valid" },
                            { "field": "password", "message": "Password must be at least 8 characters" }
                          ],
                          "timestamp": "2026-07-19T10:00:00Z",
                          "path": "/api/auth/register"
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email address is already registered",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Duplicate Email",
                    value = """
                        {
                          "success": false,
                          "status": 409,
                          "message": "Email already exists: john.doe@example.com",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "path": "/api/auth/register"
                        }"""
                )
            )
        )
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Received registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);

        com.example.ats.dto.response.ApiResponse<AuthResponse> body =
                com.example.ats.dto.response.ApiResponse.success("Registration successful", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user's credentials and issues a JWT token.
     *
     * @param request the email and password credentials
     * @return the login response containing access token wrapped in a standard ApiResponse
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login and obtain JWT tokens",
        description = "Authenticates user credentials and returns a **JWT access token** plus a **refresh token**. " +
                      "Copy the `accessToken` and use it in the Authorize button to test protected APIs. " +
                      "No JWT token required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful — JWT tokens returned",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Login Success",
                    value = """
                        {
                          "success": true,
                          "message": "Login successful",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.signature",
                            "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.refresh",
                            "tokenType": "Bearer",
                            "expiresIn": 3600,
                            "userId": "550e8400-e29b-41d4-a716-446655440000",
                            "fullName": "John Doe",
                            "email": "john.doe@example.com",
                            "role": "RECRUITER"
                          }
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed — blank email or password",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials — wrong email or password",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Credentials",
                    value = """
                        {
                          "success": false,
                          "status": 401,
                          "message": "Invalid email or password",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "path": "/api/auth/login"
                        }"""
                )
            )
        )
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Received login request for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);

        com.example.ats.dto.response.ApiResponse<LoginResponse> body =
                com.example.ats.dto.response.ApiResponse.success("Login successful", response);
        return ResponseEntity.ok(body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/refresh-token
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Refreshes an expired JWT access token using a valid refresh token.
     *
     * @param request the refresh token payload
     * @return the new tokens wrapped in a standard ApiResponse
     */
    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh an expired access token",
        description = "Validates the provided refresh token and returns a **new access token** and " +
                      "a **rotated refresh token** (the old refresh token is immediately invalidated). " +
                      "No JWT token required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Refresh Success",
                    value = """
                        {
                          "success": true,
                          "message": "Token refreshed successfully",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzUxMiJ9.new_access_token.signature",
                            "refreshToken": "eyJhbGciOiJIUzUxMiJ9.new_refresh_token.signature",
                            "tokenType": "Bearer",
                            "expiresIn": 3600
                          }
                        }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh token is invalid, expired, or already used",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Refresh Token",
                    value = """
                        {
                          "success": false,
                          "status": 401,
                          "message": "Refresh token is invalid or expired. Please login again.",
                          "timestamp": "2026-07-19T10:00:00Z",
                          "path": "/api/auth/refresh-token"
                        }"""
                )
            )
        )
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        log.info("Received refresh token request");
        TokenRefreshResponse response = authService.refreshToken(request);

        com.example.ats.dto.response.ApiResponse<TokenRefreshResponse> body =
                com.example.ats.dto.response.ApiResponse.success("Token refreshed successfully", response);
        return ResponseEntity.ok(body);
    }
}
