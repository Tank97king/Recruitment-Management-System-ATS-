package com.example.ats.controller;

import com.example.ats.dto.response.ApplicationStatisticsResponse;
import com.example.ats.dto.response.CandidateStatisticsResponse;
import com.example.ats.dto.response.CompanyStatisticsResponse;
import com.example.ats.dto.response.DashboardSummaryResponse;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.JobStatisticsResponse;
import com.example.ats.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for retrieving system dashboard analytics and statistics.
 */
@RestController
@RequestMapping(ApiConstants.DASHBOARD)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard & Statistics", description = "Aggregate analytics, metrics, and summary statistics for the dashboard.")
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final DashboardService dashboardService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/summary
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves overall system entity counts and summary totals.
     * Accessible by authenticated users only.
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get dashboard summary totals",
        description = "Retrieve total counts for users, companies, jobs, candidates, applications, and interviews. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard summary retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        log.info("REST request to get dashboard summary totals.");
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Dashboard summary retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/applications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves application counts and percentage breakdowns by stage.
     * Accessible by authenticated users only.
     */
    @GetMapping("/applications")
    @Operation(
        summary = "Get application stage statistics",
        description = "Retrieve application counts and percentage breakdowns across all stages. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application statistics retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<ApplicationStatisticsResponse>> getApplicationStatistics() {
        log.info("REST request to get application stage statistics.");
        ApplicationStatisticsResponse response = dashboardService.getApplicationStatistics();
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Application statistics retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves job posting status counts (OPEN, CLOSED, DRAFT).
     * Accessible by authenticated users only.
     */
    @GetMapping("/jobs")
    @Operation(
        summary = "Get job posting statistics",
        description = "Retrieve job counts grouped by status (OPEN, CLOSED, DRAFT). Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job statistics retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobStatisticsResponse>> getJobStatistics() {
        log.info("REST request to get job posting statistics.");
        JobStatisticsResponse response = dashboardService.getJobStatistics();
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Job statistics retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/candidates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves candidate resume metrics (profiles with vs without CV).
     * Accessible by authenticated users only.
     */
    @GetMapping("/candidates")
    @Operation(
        summary = "Get candidate resume statistics",
        description = "Retrieve candidate metrics for profiles with vs without an uploaded CV. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Candidate statistics retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CandidateStatisticsResponse>> getCandidateStatistics() {
        log.info("REST request to get candidate resume statistics.");
        CandidateStatisticsResponse response = dashboardService.getCandidateStatistics();
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Candidate statistics retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard/companies
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves company metrics (companies with active jobs vs without jobs).
     * Accessible by authenticated users only.
     */
    @GetMapping("/companies")
    @Operation(
        summary = "Get company active job statistics",
        description = "Retrieve company counts for those with active open jobs vs without jobs. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company statistics retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CompanyStatisticsResponse>> getCompanyStatistics() {
        log.info("REST request to get company active job statistics.");
        CompanyStatisticsResponse response = dashboardService.getCompanyStatistics();
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Company statistics retrieved successfully", response));
    }
}
