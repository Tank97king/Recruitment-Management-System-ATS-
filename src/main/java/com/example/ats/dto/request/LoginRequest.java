package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user login request payload.
 *
 * <p>Enforces input validation using Jakarta validation annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "LoginRequest",
    description = "Request payload for user authentication. Provide registered email and password."
)
public class LoginRequest {

    /**
     * The email address of the user.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
        description = "Registered email address of the user",
        example = "john.doe@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    /**
     * The user's plain text password.
     */
    @NotBlank(message = "Password is required")
    @Schema(
        description = "Account password (minimum 8 characters)",
        example = "SecurePass123!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}
