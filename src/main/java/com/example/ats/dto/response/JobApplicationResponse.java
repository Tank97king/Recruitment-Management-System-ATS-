package com.example.ats.dto.response;

import com.example.ats.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing job application response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "JobApplicationResponse",
    description = "Job application details returned by job application management endpoints."
)
public class JobApplicationResponse {

    @Schema(description = "Unique identifier of the job application", example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID id;

    @Schema(description = "UUID of the candidate who applied", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID candidateId;

    @Schema(description = "Full name of the candidate who applied", example = "Jane Smith")
    private String candidateName;

    @Schema(description = "UUID of the job the candidate applied for", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID jobId;

    @Schema(description = "Title of the job posting", example = "Senior Java Backend Engineer")
    private String jobTitle;

    @Schema(description = "Name of the company that posted the job", example = "TechCorp Solutions Inc.")
    private String companyName;

    @Schema(
        description = "Current status of the application in the hiring workflow",
        example = "REVIEWING",
        allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"}
    )
    private ApplicationStatus applicationStatus;

    @Schema(description = "Cover letter provided by the candidate (may be null)", example = "I am excited to apply for this Senior Java Engineer role...")
    private String coverLetter;

    @Schema(description = "Timestamp when the application was submitted (UTC ISO 8601)", example = "2026-07-01T14:00:00Z")
    private Instant appliedAt;

    @Schema(description = "Timestamp when the application was last updated (UTC ISO 8601)", example = "2026-07-15T09:30:00Z")
    private Instant updatedAt;
}
