package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) containing registration success metadata returned to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "AuthResponse",
    description = "Response payload returned after successful user registration."
)
public class AuthResponse {

    @Schema(description = "Unique identifier of the newly created user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Full name of the registered user", example = "John Doe")
    private String fullName;

    @Schema(description = "Email address of the registered user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Default role assigned to the user", example = "RECRUITER")
    private String role;

    @Schema(description = "Activation status of the newly created account", example = "ACTIVE")
    private String status;
}
