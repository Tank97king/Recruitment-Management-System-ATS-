package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing job status statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "JobStatisticsResponse", description = "Job posting counts grouped by status (OPEN, CLOSED, DRAFT).")
public class JobStatisticsResponse {

    @Schema(description = "Total number of job postings", example = "120")
    private long totalJobs;

    @Schema(description = "Number of job postings with OPEN status", example = "78")
    private long openJobs;

    @Schema(description = "Number of job postings with CLOSED status", example = "32")
    private long closedJobs;

    @Schema(description = "Number of job postings with DRAFT status", example = "10")
    private long draftJobs;
}
