package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) containing sanitized user profile data.
 * Used to avoid exposing internal entity classes directly to consumers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UserResponse",
    description = "User profile details returned by user management endpoints."
)
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number of the user", example = "+1555123456")
    private String phone;

    @Schema(description = "Role assigned to the user", example = "RECRUITER", allowableValues = {"ADMIN", "RECRUITER"})
    private String role;

    @Schema(description = "Account status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(description = "Timestamp when the user account was created (UTC ISO 8601)", example = "2026-01-15T08:30:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the user account was last updated (UTC ISO 8601)", example = "2026-07-19T10:00:00Z")
    private Instant updatedAt;
}
