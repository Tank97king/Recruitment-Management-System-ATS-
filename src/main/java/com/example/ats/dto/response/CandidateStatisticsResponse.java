package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing candidate resume/CV metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CandidateStatisticsResponse", description = "Candidate profile CV upload metrics.")
public class CandidateStatisticsResponse {

    @Schema(description = "Total number of active candidate profiles", example = "342")
    private long totalCandidates;

    @Schema(description = "Number of candidates who have uploaded a CV/resume", example = "278")
    private long candidatesWithCv;

    @Schema(description = "Number of candidates who have NOT uploaded a CV/resume", example = "64")
    private long candidatesWithoutCv;
}
