package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) returned upon successful login authentication.
 *
 * <p>Contains the cryptographically signed JWT access token alongside basic
 * profile metadata so the client can construct their user session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "LoginResponse",
    description = "Response payload returned after successful user authentication. " +
                  "Contains JWT tokens and basic user profile information."
)
public class LoginResponse {

    @Schema(
        description = "JWT access token — include in Authorization header as 'Bearer {accessToken}'",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJFQ1JVSVRFUiJdLCJpYXQiOjE3MjE5MDAwMDB9.token"
    )
    private String accessToken;

    @Schema(
        description = "Refresh token — use with POST /api/auth/refresh-token to get a new access token when expired",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTcyMTkwMDAwMH0.refresh_token"
    )
    private String refreshToken;

    @Schema(description = "Token type (always 'Bearer')", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in seconds from the time of issuance", example = "3600")
    private long expiresIn;

    @Schema(description = "Unique identifier of the authenticated user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Full name of the authenticated user", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address of the authenticated user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Role of the authenticated user", example = "RECRUITER", allowableValues = {"ADMIN", "RECRUITER"})
    private String role;
}
