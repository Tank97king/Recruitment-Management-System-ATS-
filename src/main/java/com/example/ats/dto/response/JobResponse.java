package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing a job posting details response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "JobResponse",
    description = "Job posting details returned by job management endpoints."
)
public class JobResponse {

    @Schema(description = "Unique identifier of the job posting", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID id;

    @Schema(description = "Title of the job posting", example = "Senior Java Backend Engineer")
    private String title;

    @Schema(description = "Company that posted this job")
    private CompanyResponse company;

    @Schema(description = "Detailed description of the job role", example = "We are looking for an experienced Senior Java Engineer...")
    private String description;

    @Schema(description = "Candidate requirements and qualifications", example = "- 5+ years Java\n- Spring Boot\n- PostgreSQL")
    private String requirements;

    @Schema(description = "Job location (city, country, or 'Remote')", example = "San Francisco, CA (Hybrid)")
    private String location;

    @Schema(description = "Employment type", example = "FULL_TIME", allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE", "INTERNSHIP"})
    private String employmentType;

    @Schema(description = "Required experience level", example = "SENIOR", allowableValues = {"ENTRY", "JUNIOR", "MID", "SENIOR", "LEAD", "EXECUTIVE"})
    private String experienceLevel;

    @Schema(description = "Minimum salary offered (annual, USD)", example = "90000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary offered (annual, USD)", example = "130000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Current status of the job posting", example = "OPEN", allowableValues = {"OPEN", "CLOSED", "DRAFT"})
    private String status;

    @Schema(description = "Application deadline date (ISO 8601: YYYY-MM-DD)", example = "2026-12-31", type = "string", format = "date")
    private LocalDate deadline;

    @Schema(description = "Timestamp when the job was created (UTC ISO 8601)", example = "2026-06-01T08:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the job was last updated (UTC ISO 8601)", example = "2026-07-10T12:00:00Z")
    private Instant updatedAt;
}
