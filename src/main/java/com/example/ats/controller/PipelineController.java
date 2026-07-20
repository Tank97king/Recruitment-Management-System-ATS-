package com.example.ats.controller;

import com.example.ats.dto.request.UpdateApplicationStageRequest;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.PipelineApplicationResponse;
import com.example.ats.dto.response.PipelineResponse;
import com.example.ats.dto.response.PipelineSummaryResponse;
import com.example.ats.service.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing the recruitment pipeline.
 */
@RestController
@RequestMapping(ApiConstants.PIPELINE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recruitment Pipeline Management", description = "Kanban board view and stage movement for the recruitment pipeline.")
@PreAuthorize("isAuthenticated()")
public class PipelineController {

    private final PipelineService pipelineService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/pipeline
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the Kanban pipeline view with stage columns.
     */
    @GetMapping
    @Operation(
        summary = "Get recruitment pipeline (Kanban board)",
        description = "Retrieve the Kanban board with all pipeline stages (APPLIED → REVIEWING → INTERVIEW → OFFER → HIRED / REJECTED). " +
                      "Supports optional filtering by jobId, companyId, and recruiterId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pipeline retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PipelineResponse>> getPipeline(
            @Parameter(description = "Filter pipeline by specific job UUID", example = "550e8400-e29b-41d4-a716-446655440002")
            @RequestParam(required = false) UUID jobId,

            @Parameter(description = "Filter pipeline by company UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter pipeline by recruiter UUID", example = "550e8400-e29b-41d4-a716-446655440005")
            @RequestParam(required = false) UUID recruiterId
    ) {
        log.info("REST request to get recruitment pipeline. JobId: {}, CompanyId: {}, RecruiterId: {}",
                jobId, companyId, recruiterId);
        PipelineResponse response = pipelineService.getPipeline(jobId, companyId, recruiterId);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Recruitment pipeline retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/pipeline/jobs/{jobId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the Kanban pipeline view for a specific job.
     */
    @GetMapping("/jobs/{jobId}")
    @Operation(
        summary = "Get pipeline for a specific job",
        description = "Retrieve the Kanban board view showing all application stages for a specific job posting."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pipeline for job retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PipelineResponse>> getPipelineByJob(
            @Parameter(description = "UUID of the job posting to view pipeline for", required = true, example = "550e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID jobId
    ) {
        log.info("REST request to get recruitment pipeline for job ID: {}", jobId);
        PipelineResponse response = pipelineService.getPipelineByJob(jobId);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Pipeline for job retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/pipeline/applications/{applicationId}/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Moves an application to a new stage in the recruitment pipeline.
     */
    @PatchMapping("/applications/{applicationId}/status")
    @Operation(
        summary = "Move application to a new pipeline stage",
        description = "Advance or reject an application in the Kanban pipeline. " +
                      "Stage transitions must follow the allowed workflow:\n" +
                      "`APPLIED` → `REVIEWING` → `INTERVIEW` → `OFFER` → `HIRED` or `REJECTED`"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application stage updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid or disallowed stage transition",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job application not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PipelineApplicationResponse>> moveApplicationStage(
            @Parameter(description = "UUID of the job application to move", required = true, example = "550e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID applicationId,
            @Valid @RequestBody UpdateApplicationStageRequest request
    ) {
        log.info("REST request to move application {} stage to {}", applicationId, request.getStatus());
        PipelineApplicationResponse response = pipelineService.moveApplicationStage(applicationId, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Application stage updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/pipeline/summary
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves summary counts of applications across all pipeline stages.
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get pipeline summary statistics",
        description = "Retrieve aggregate application counts grouped by each pipeline stage. " +
                      "Supports the same optional filters as the main pipeline endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pipeline summary retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PipelineSummaryResponse>> getPipelineSummary(
            @Parameter(description = "Filter by specific job UUID", example = "550e8400-e29b-41d4-a716-446655440002")
            @RequestParam(required = false) UUID jobId,

            @Parameter(description = "Filter by company UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter by recruiter UUID", example = "550e8400-e29b-41d4-a716-446655440005")
            @RequestParam(required = false) UUID recruiterId
    ) {
        log.info("REST request to get pipeline summary statistics.");
        PipelineSummaryResponse response = pipelineService.getPipelineSummary(jobId, companyId, recruiterId);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Pipeline summary retrieved successfully", response));
    }
}
