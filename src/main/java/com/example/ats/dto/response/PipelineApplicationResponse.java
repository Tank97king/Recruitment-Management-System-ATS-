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
 * Data Transfer Object (DTO) representing an application card in the pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PipelineApplicationResponse", description = "A condensed application card displayed inside a pipeline stage column.")
public class PipelineApplicationResponse {

    @Schema(description = "Unique identifier of the job application", example = "550e8400-e29b-41d4-a716-446655440003")
    private UUID id;

    @Schema(description = "UUID of the candidate", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID candidateId;

    @Schema(description = "Full name of the candidate", example = "Jane Smith")
    private String candidateName;

    @Schema(description = "UUID of the job posting", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID jobId;

    @Schema(description = "Title of the job posting", example = "Senior Java Backend Engineer")
    private String jobTitle;

    @Schema(description = "Name of the company that posted the job", example = "TechCorp Solutions Inc.")
    private String companyName;

    @Schema(description = "Timestamp when the application was submitted", example = "2026-07-01T14:00:00Z")
    private Instant appliedAt;

    @Schema(
        description = "Current pipeline stage of the application",
        example = "REVIEWING",
        allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"}
    )
    private ApplicationStatus currentStatus;
}
