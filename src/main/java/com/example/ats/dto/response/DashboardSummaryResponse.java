package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing overall system dashboard totals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DashboardSummaryResponse",
    description = "Overall system entity counts returned by the dashboard summary endpoint."
)
public class DashboardSummaryResponse {

    @Schema(description = "Total number of active users in the system", example = "25")
    private long totalUsers;

    @Schema(description = "Total number of active company profiles", example = "48")
    private long totalCompanies;

    @Schema(description = "Total number of job postings (all statuses)", example = "120")
    private long totalJobs;

    @Schema(description = "Total number of active candidate profiles", example = "342")
    private long totalCandidates;

    @Schema(description = "Total number of job applications submitted", example = "875")
    private long totalApplications;

    @Schema(description = "Total number of scheduled interviews", example = "156")
    private long totalInterviews;
}
