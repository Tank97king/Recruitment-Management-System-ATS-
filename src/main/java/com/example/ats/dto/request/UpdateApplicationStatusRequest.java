package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating a job application status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateApplicationStatusRequest",
    description = "Request payload for updating the status of a job application."
)
public class UpdateApplicationStatusRequest {

    @NotBlank(message = "Status is required")
    @Schema(
        description = "New application status. Allowed values follow the hiring workflow stages.",
        example = "REVIEWING",
        allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
}
