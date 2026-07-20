package com.example.ats.dto.request;

import com.example.ats.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating a company profile.
 *
 * <p>Enforces input validation using Jakarta Validation API annotations.
 * {@code email} and {@code phone} are optional but validated when provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateCompanyRequest",
    description = "Request payload for updating an existing company profile. " +
                  "Company name uniqueness is enforced (excluding the current company)."
)
public class UpdateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Schema(
        description = "Updated official name of the company",
        example = "TechCorp Solutions Inc.",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String companyName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(
        description = "Updated contact email address (optional)",
        example = "hr@techcorp.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @ValidPhoneNumber
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    @Schema(
        description = "Updated contact phone number (optional)",
        example = "+1-555-123-4567",
        maxLength = 50,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phone;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Schema(
        description = "Updated company website URL (optional)",
        example = "https://www.techcorp.com",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String website;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(
        description = "Updated headquarters address (optional)",
        example = "123 Tech Boulevard, San Francisco, CA 94105, USA",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(
        description = "Updated company description (optional)",
        example = "TechCorp Solutions is a leading software development firm specializing in cloud-native solutions.",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;
}
