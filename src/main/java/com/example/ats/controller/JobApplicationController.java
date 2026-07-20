package com.example.ats.controller;

import com.example.ats.dto.request.CreateJobApplicationRequest;
import com.example.ats.dto.request.UpdateApplicationStatusRequest;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.JobApplicationResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing job applications.
 */
@RestController
@RequestMapping(ApiConstants.APPLICATIONS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Application Management", description = "Submit and manage job applications throughout the hiring workflow.")
@PreAuthorize("isAuthenticated()")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/job-applications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Submits a new job application.
     */
    @PostMapping
    @Operation(
        summary = "Submit a job application",
        description = "Creates a new job application. Business rules enforced:\n" +
                      "- The candidate must have an uploaded CV\n" +
                      "- The job must be in **OPEN** status\n" +
                      "- A candidate cannot apply to the same job twice"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Application submitted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(name = "Application Submitted", value = """
                    {
                      "success": true,
                      "message": "Job application submitted successfully",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440003",
                        "candidateId": "550e8400-e29b-41d4-a716-446655440001",
                        "candidateName": "Jane Smith",
                        "jobId": "550e8400-e29b-41d4-a716-446655440002",
                        "jobTitle": "Senior Java Backend Engineer",
                        "companyName": "TechCorp Solutions Inc.",
                        "applicationStatus": "APPLIED",
                        "coverLetter": "I am excited to apply...",
                        "appliedAt": "2026-07-19T10:00:00Z"
                      }
                    }""")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed or business rule violation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(name = "No CV uploaded", value = """
                    {
                      "success": false,
                      "status": 400,
                      "message": "Candidate does not have a CV uploaded. Please upload a CV before applying.",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "path": "/api/job-applications"
                    }""")
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate or Job not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Candidate already applied to this job",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobApplicationResponse>> createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request
    ) {
        log.info("REST request to submit job application for candidate {} and job {}",
                request.getCandidateId(), request.getJobId());
        JobApplicationResponse response = jobApplicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.ats.dto.response.ApiResponse.success("Job application submitted successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/job-applications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated, sorted list of job applications with filtering.
     */
    @GetMapping
    @Operation(
        summary = "Get all job applications",
        description = "Retrieve a paginated list of job applications. Supports filtering by candidate, job, company, and status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job applications retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<JobApplicationResponse>>> getApplications(
            @Parameter(description = "Filter by candidate UUID", example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam(required = false) UUID candidateId,

            @Parameter(description = "Filter by job UUID", example = "550e8400-e29b-41d4-a716-446655440002")
            @RequestParam(required = false) UUID jobId,

            @Parameter(description = "Filter by company UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter by application status",
                schema = @Schema(allowableValues = {"APPLIED", "REVIEWING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"}))
            @RequestParam(required = false) String status,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of applications per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by",
                schema = @Schema(allowableValues = {"appliedAt", "updatedAt", "applicationStatus"}))
            @RequestParam(defaultValue = "appliedAt") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("REST request to get job applications. Page: {}, Size: {}", page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<JobApplicationResponse> response = jobApplicationService.getApplications(
                candidateId, jobId, companyId, status, pageable);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Job applications retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/job-applications/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves details of a specific job application.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get job application by ID",
        description = "Retrieve detailed information of a single job application by its UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job application details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job application not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobApplicationResponse>> getApplicationById(
            @Parameter(description = "UUID of the job application", required = true, example = "550e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID id
    ) {
        log.info("REST request to get job application details for ID: {}", id);
        JobApplicationResponse response = jobApplicationService.getApplicationById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Job application details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/job-applications/{id}/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the status of a job application.
     */
    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update job application status",
        description = "Advance or reject a job application. Status transitions follow the workflow:\n" +
                      "`APPLIED` → `REVIEWING` → `INTERVIEW` → `OFFER` → `HIRED` or `REJECTED`"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application status updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status transition",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job application not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobApplicationResponse>> updateStatus(
            @Parameter(description = "UUID of the job application to update", required = true, example = "550e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        log.info("REST request to update status of job application {} to {}", id, request.getStatus());
        JobApplicationResponse response = jobApplicationService.updateStatus(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success(
                "Job application status updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/job-applications/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a job application.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a job application",
        description = "Soft-deletes a job application by its UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job application deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job application not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteApplication(
            @Parameter(description = "UUID of the job application to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete job application: {}", id);
        jobApplicationService.deleteApplication(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Job application deleted successfully"));
    }
}
