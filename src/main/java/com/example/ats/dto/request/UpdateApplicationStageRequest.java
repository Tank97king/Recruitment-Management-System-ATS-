package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating an application's stage in the pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateApplicationStageRequest",
    description = "Request payload for moving an application to a different stage in the recruitment pipeline."
)
public class UpdateApplicationStageRequest {

    @NotBlank(message = "Status is required")
    @Schema(
        description = "Target pipeline stage for the application. Must follow allowed stage transition rules.",
        example = "INTERVIEW",
        allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
}
