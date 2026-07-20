package com.example.ats.dto.response;

import com.example.ats.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a single stage column in the recruitment pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PipelineColumnResponse", description = "A single stage column in the Kanban recruitment pipeline board.")
public class PipelineColumnResponse {

    @Schema(
        description = "The pipeline stage this column represents",
        example = "REVIEWING",
        allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"}
    )
    private ApplicationStatus stage;

    @Schema(description = "Total number of applications in this stage", example = "15")
    private int totalApplications;

    @Schema(description = "List of application cards in this stage column")
    private List<PipelineApplicationResponse> applications;
}
