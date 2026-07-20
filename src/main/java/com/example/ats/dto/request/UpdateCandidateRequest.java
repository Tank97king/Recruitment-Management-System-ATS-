package com.example.ats.dto.request;

import com.example.ats.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object (DTO) for updating an existing candidate profile.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 * Business rules (e.g., duplicate email check) are handled in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateCandidateRequest",
    description = "Request payload for updating an existing candidate profile. " +
                  "Email must be unique across all active candidates (excluding this candidate)."
)
public class UpdateCandidateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    @Schema(
        description = "Updated full name of the candidate",
        example = "Jane Smith",
        maxLength = 200,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(
        description = "Updated email address of the candidate — must be unique in the system",
        example = "jane.smith@email.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @ValidPhoneNumber
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    @Schema(
        description = "Updated phone number of the candidate",
        example = "+1-555-987-6543",
        maxLength = 50,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phone;

    @Past(message = "Date of birth must be a past date")
    @Schema(
        description = "Updated date of birth (ISO 8601: YYYY-MM-DD). Must be a past date.",
        example = "1990-05-15",
        type = "string",
        format = "date",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Gender must not exceed 50 characters")
    @Schema(
        description = "Updated gender description",
        example = "Female",
        maxLength = 50,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String gender;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(
        description = "Updated current address",
        example = "456 Oak Avenue, Austin, TX 78701",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Schema(
        description = "Updated years of experience. Must be 0 or greater.",
        example = "7",
        minimum = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer yearsOfExperience;

    @Size(max = 255, message = "Highest education must not exceed 255 characters")
    @Schema(
        description = "Updated highest educational degree/qualification",
        example = "Bachelor of Computer Science — MIT (2012)",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String highestEducation;

    @Size(max = 255, message = "Current title must not exceed 255 characters")
    @Schema(
        description = "Updated current job title",
        example = "Senior Software Engineer",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String currentTitle;

    @Size(max = 255, message = "Current company must not exceed 255 characters")
    @Schema(
        description = "Updated current employer name",
        example = "Google LLC",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String currentCompany;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(
        description = "Updated preferred work location",
        example = "Austin, TX",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String location;

    @Schema(
        description = "Updated list of candidate skills",
        example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> skills;

    @Size(max = 5000, message = "Summary must not exceed 5000 characters")
    @Schema(
        description = "Updated candidate professional summary / bio",
        example = "Experienced software engineer with 7+ years building scalable Java microservices...",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String summary;
}
