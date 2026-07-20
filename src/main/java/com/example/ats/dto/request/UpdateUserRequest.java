package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload DTO for updating an existing user's details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateUserRequest",
    description = "Request payload for updating a user's name, email, and account status. Admin only."
)
public class UpdateUserRequest {

    /**
     * The full name of the user (e.g. "John Doe").
     * Must not be blank.
     */
    @NotBlank(message = "Full name is required")
    @Schema(
        description = "Full name of the user",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    /**
     * The email address of the user.
     * Must be in a valid email format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(
        description = "Email address of the user — must be unique in the system",
        example = "john.doe@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    /**
     * The active status of the user (ACTIVE or INACTIVE).
     * Must be one of the enum values.
     */
    @NotBlank(message = "Status is required")
    @Schema(
        description = "Account status of the user",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
}
