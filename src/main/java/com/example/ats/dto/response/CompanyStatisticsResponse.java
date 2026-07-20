package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing company active job status metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CompanyStatisticsResponse", description = "Company metrics based on whether companies have active job postings.")
public class CompanyStatisticsResponse {

    @Schema(description = "Total number of active company profiles", example = "48")
    private long totalCompanies;

    @Schema(description = "Number of companies with at least one OPEN job posting", example = "35")
    private long companiesWithActiveJobs;

    @Schema(description = "Number of companies with no active job postings", example = "13")
    private long companiesWithoutJobs;
}
