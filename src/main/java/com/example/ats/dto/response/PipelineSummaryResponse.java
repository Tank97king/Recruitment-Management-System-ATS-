package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing pipeline summary counts by stage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PipelineSummaryResponse", description = "Aggregate application counts grouped by each pipeline stage.")
public class PipelineSummaryResponse {

    @Schema(description = "Total applications across all stages", example = "875")
    private long totalApplications;

    @Schema(description = "Applications in APPLIED stage", example = "210")
    private long applied;

    @Schema(description = "Applications in REVIEWING stage", example = "180")
    private long reviewing;

    @Schema(description = "Applications in INTERVIEW stage", example = "156")
    private long interview;

    @Schema(description = "Applications in OFFER stage", example = "90")
    private long offer;

    @Schema(description = "Applications in HIRED stage", example = "65")
    private long hired;

    @Schema(description = "Applications in REJECTED stage", example = "174")
    private long rejected;
}
