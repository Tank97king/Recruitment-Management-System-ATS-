package com.example.ats.dto.request;

import com.example.ats.validation.FieldsMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user registration request payload.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldsMatch(
        field = "password",
        fieldMatch = "confirmPassword",
        message = "Confirm password must match password"
)
@Schema(
    name = "RegisterRequest",
    description = "Request payload for registering a new recruiter account. " +
                  "Password and confirmPassword must match."
)
public class RegisterRequest {

    /**
     * Full name of the registering user (e.g., "John Doe").
     *
     * <p>We split this field into firstName and lastName at the service layer
     * before mapping to the User entity.
     */
    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    @Schema(
        description = "Full name of the user (first and last name)",
        example = "John Doe",
        maxLength = 200,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    /**
     * Email address used for authentication and registration.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(
        description = "Unique email address used for login and communication",
        example = "john.doe@example.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    /**
     * Plain text password.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Size(max = 100, message = "Password must not exceed 100 characters")
    @Schema(
        description = "Account password — minimum 8 characters, maximum 100 characters",
        example = "SecurePass123!",
        minLength = 8,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    /**
     * Password confirmation field. Must match {@code password}.
     */
    @NotBlank(message = "Confirm password is required")
    @Schema(
        description = "Repeat the password to confirm. Must exactly match the password field.",
        example = "SecurePass123!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmPassword;
}
