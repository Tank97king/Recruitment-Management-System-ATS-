package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for creating a job application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CreateJobApplicationRequest",
    description = "Request payload for submitting a job application. " +
                  "The candidate must have an uploaded CV, and the job must be in OPEN status."
)
public class CreateJobApplicationRequest {

    @NotNull(message = "Candidate ID is required")
    @Schema(
        description = "UUID of the candidate applying for the job",
        example = "550e8400-e29b-41d4-a716-446655440001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID candidateId;

    @NotNull(message = "Job ID is required")
    @Schema(
        description = "UUID of the job posting to apply for",
        example = "550e8400-e29b-41d4-a716-446655440002",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID jobId;

    @Schema(
        description = "Optional cover letter text from the candidate",
        example = "I am excited to apply for this Senior Java Engineer role. With 7 years of experience " +
                  "in Spring Boot and microservices, I believe I would be a great fit for your team...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String coverLetter;
}
