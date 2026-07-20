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
 * Data Transfer Object (DTO) for creating a company profile.
 *
 * <p>Enforces input validation using Jakarta Validation API annotations.
 * {@code email} and {@code phone} are optional but validated when provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CreateCompanyRequest",
    description = "Request payload for creating a new company profile. Company name must be unique."
)
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Schema(
        description = "Unique official name of the company",
        example = "TechCorp Solutions Inc.",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String companyName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(
        description = "Official contact email address of the company (optional)",
        example = "hr@techcorp.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @ValidPhoneNumber
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    @Schema(
        description = "Company contact phone number in E.164 or local format (optional)",
        example = "+1-555-123-4567",
        maxLength = 50,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phone;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Schema(
        description = "Company official website URL (optional)",
        example = "https://www.techcorp.com",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String website;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(
        description = "Company headquarters address (optional)",
        example = "123 Tech Boulevard, San Francisco, CA 94105, USA",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(
        description = "Detailed company description, culture, and background (optional)",
        example = "TechCorp Solutions is a leading software development firm specializing in " +
                  "cloud-native enterprise solutions. Founded in 2010, we have grown to 500+ employees.",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;
}
