package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating interview status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateInterviewStatusRequest",
    description = "Request payload for updating the status of a scheduled interview."
)
public class UpdateInterviewStatusRequest {

    @NotBlank(message = "Status is required")
    @Schema(
        description = "New interview status",
        example = "COMPLETED",
        allowableValues = {"SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
}
