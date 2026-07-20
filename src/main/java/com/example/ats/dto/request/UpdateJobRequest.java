package com.example.ats.dto.request;

import com.example.ats.validation.ValidSalaryRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for updating a job posting.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 * Salary range consistency is enforced by the {@link ValidSalaryRange} cross-field
 * constraint. Business rules (e.g., company existence, status transition) remain
 * in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSalaryRange(minField = "salaryMin", maxField = "salaryMax")
@Schema(
    name = "UpdateJobRequest",
    description = "Request payload for updating an existing job posting. " +
                  "salaryMax must be greater than or equal to salaryMin when both are provided."
)
public class UpdateJobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
        description = "Updated job title",
        example = "Senior Java Backend Engineer",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @NotNull(message = "Company ID is required")
    @Schema(
        description = "UUID of the company this job belongs to",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID companyId;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(
        description = "Updated detailed description of the role",
        example = "We are looking for an experienced Senior Java Engineer...",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @Size(max = 5000, message = "Requirements must not exceed 5000 characters")
    @Schema(
        description = "Updated candidate qualifications requirements",
        example = "- 5+ years Java\n- Spring Boot",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String requirements;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(
        description = "Updated location info",
        example = "San Francisco, CA (Hybrid)",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String location;

    @Size(max = 100, message = "Employment type must not exceed 100 characters")
    @Schema(
        description = "Updated employment type",
        example = "FULL_TIME",
        maxLength = 100,
        allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE", "INTERNSHIP"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String employmentType;

    @Size(max = 100, message = "Experience level must not exceed 100 characters")
    @Schema(
        description = "Updated experience level",
        example = "SENIOR",
        maxLength = 100,
        allowableValues = {"ENTRY", "JUNIOR", "MID", "SENIOR", "LEAD", "EXECUTIVE"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String experienceLevel;

    @PositiveOrZero(message = "Minimum salary must be zero or positive")
    @Schema(
        description = "Updated minimum annual salary (USD)",
        example = "90000.00",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private BigDecimal salaryMin;

    @PositiveOrZero(message = "Maximum salary must be zero or positive")
    @Schema(
        description = "Updated maximum annual salary (USD)",
        example = "130000.00",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private BigDecimal salaryMax;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Schema(
        description = "Updated status of the job posting",
        example = "OPEN",
        maxLength = 50,
        allowableValues = {"OPEN", "CLOSED", "DRAFT"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String status;

    @FutureOrPresent(message = "Deadline must be today or in the future")
    @Schema(
        description = "Updated application deadline (ISO 8601: YYYY-MM-DD)",
        example = "2026-12-31",
        type = "string",
        format = "date",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate deadline;
}
