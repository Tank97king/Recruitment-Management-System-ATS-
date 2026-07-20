package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload DTO for updating current user profile details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateProfileRequest",
    description = "Request payload for updating user profile details (fullName, phone)"
)
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Schema(
        description = "Full name of the user",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Schema(
        description = "Phone number of the user",
        example = "+1555123456",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phone;
}
