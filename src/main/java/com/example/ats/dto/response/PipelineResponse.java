package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the full Kanban pipeline board view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PipelineResponse", description = "The full Kanban recruitment pipeline board containing all stage columns.")
public class PipelineResponse {

    @Schema(description = "Ordered list of pipeline stage columns (APPLIED → REVIEWING → INTERVIEW → OFFER → HIRED / REJECTED)")
    private List<PipelineColumnResponse> columns;
}
