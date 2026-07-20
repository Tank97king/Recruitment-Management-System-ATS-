package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing application stage counts and percentage breakdowns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ApplicationStatisticsResponse",
    description = "Application counts and percentage breakdowns across all hiring pipeline stages."
)
public class ApplicationStatisticsResponse {

    @Schema(description = "Number of applications in APPLIED stage", example = "210")
    private long applied;
    @Schema(description = "Percentage of total applications in APPLIED stage", example = "24.0")
    private double appliedPercentage;

    @Schema(description = "Number of applications in REVIEWING stage", example = "180")
    private long reviewing;
    @Schema(description = "Percentage of total applications in REVIEWING stage", example = "20.6")
    private double reviewingPercentage;

    @Schema(description = "Number of applications in INTERVIEW stage", example = "156")
    private long interview;
    @Schema(description = "Percentage of total applications in INTERVIEW stage", example = "17.8")
    private double interviewPercentage;

    @Schema(description = "Number of applications in OFFER stage", example = "90")
    private long offer;
    @Schema(description = "Percentage of total applications in OFFER stage", example = "10.3")
    private double offerPercentage;

    @Schema(description = "Number of applications in HIRED stage", example = "65")
    private long hired;
    @Schema(description = "Percentage of total applications in HIRED stage", example = "7.4")
    private double hiredPercentage;

    @Schema(description = "Number of applications in REJECTED stage", example = "174")
    private long rejected;
    @Schema(description = "Percentage of total applications in REJECTED stage", example = "19.9")
    private double rejectedPercentage;

    @Schema(description = "Total number of applications across all stages", example = "875")
    private long totalApplications;
}
