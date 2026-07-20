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
 * Data Transfer Object (DTO) for job creation request.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 * Bean Validation covers structural / syntactic rules (blank, size, positive values,
 * salary range consistency). Business rules (e.g., company existence) are enforced
 * in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidSalaryRange(minField = "salaryMin", maxField = "salaryMax")
@Schema(
    name = "CreateJobRequest",
    description = "Request payload for creating a new job posting. " +
                  "salaryMax must be greater than or equal to salaryMin when both are provided."
)
public class CreateJobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(
        description = "Job posting title",
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
        description = "Detailed description of the job role and responsibilities",
        example = "We are looking for an experienced Senior Java Engineer to join our backend team. " +
                  "You will design, develop, and maintain scalable microservices...",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    @Size(max = 5000, message = "Requirements must not exceed 5000 characters")
    @Schema(
        description = "Candidate requirements and qualifications for the role",
        example = "- 5+ years of Java experience\n- Spring Boot expertise\n- PostgreSQL proficiency\n- Experience with microservices",
        maxLength = 5000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String requirements;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(
        description = "Job location (city, country, or 'Remote')",
        example = "San Francisco, CA (Hybrid)",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String location;

    @Size(max = 100, message = "Employment type must not exceed 100 characters")
    @Schema(
        description = "Employment type (e.g., FULL_TIME, PART_TIME, CONTRACT, FREELANCE, INTERNSHIP)",
        example = "FULL_TIME",
        maxLength = 100,
        allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE", "INTERNSHIP"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String employmentType;

    @Size(max = 100, message = "Experience level must not exceed 100 characters")
    @Schema(
        description = "Required experience level (e.g., ENTRY, JUNIOR, MID, SENIOR, LEAD, EXECUTIVE)",
        example = "SENIOR",
        maxLength = 100,
        allowableValues = {"ENTRY", "JUNIOR", "MID", "SENIOR", "LEAD", "EXECUTIVE"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String experienceLevel;

    @PositiveOrZero(message = "Minimum salary must be zero or positive")
    @Schema(
        description = "Minimum salary offered (annual, in USD). Must be 0 or greater.",
        example = "90000.00",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private BigDecimal salaryMin;

    @PositiveOrZero(message = "Maximum salary must be zero or positive")
    @Schema(
        description = "Maximum salary offered (annual, in USD). Must be >= salaryMin.",
        example = "130000.00",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private BigDecimal salaryMax;

    @FutureOrPresent(message = "Deadline must be today or in the future")
    @Schema(
        description = "Application deadline date (ISO 8601 format: YYYY-MM-DD). Must be today or future.",
        example = "2026-12-31",
        type = "string",
        format = "date",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate deadline;
}
