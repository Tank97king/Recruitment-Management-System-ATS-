package com.example.ats.controller;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing job postings.
 */
@RestController
@RequestMapping(ApiConstants.JOBS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Management", description = "Post, search, update, and manage job postings.")
public class JobController {

    private final JobService jobService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new job posting.
     * Accessible by authenticated users only.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new job posting",
        description = "Publish a new job posting. The creating user is recorded as the owner. " +
                      "The referenced company must exist. Requires JWT authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Job posting created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(name = "Job Created", value = """
                    {
                      "success": true,
                      "message": "Job created successfully",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440002",
                        "title": "Senior Java Backend Engineer",
                        "company": { "id": "550e8400-e29b-41d4-a716-446655440000", "companyName": "TechCorp Solutions Inc." },
                        "location": "San Francisco, CA (Hybrid)",
                        "employmentType": "FULL_TIME",
                        "experienceLevel": "SENIOR",
                        "salaryMin": 90000.00,
                        "salaryMax": 130000.00,
                        "status": "OPEN",
                        "deadline": "2026-12-31",
                        "createdAt": "2026-07-19T10:00:00Z"
                      }
                    }""")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed — missing title, invalid company ID, or salaryMax < salaryMin",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Referenced company not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            Principal principal
    ) {
        log.info("REST request to create job: {}", request.getTitle());
        JobResponse response = jobService.createJob(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.ats.dto.response.ApiResponse.success("Job created successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated and filtered list of job postings.
     * Publicly accessible (no token required).
     */
    @GetMapping
    @SecurityRequirements   // Public endpoint — no JWT required
    @Operation(
        summary = "Search and list job postings (Public)",
        description = "Retrieve a paginated list of job postings. Supports advanced filtering by keyword, " +
                      "company, location, employment type, experience level, status, salary range, " +
                      "and application deadline range. **No authentication required.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<JobResponse>>> getJobs(
            @Parameter(description = "Search keyword matched against job title, description, and requirements", example = "Java Engineer")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by company UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter by job location (partial match)", example = "San Francisco")
            @RequestParam(required = false) String location,

            @Parameter(description = "Filter by employment type",
                schema = @Schema(allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE", "INTERNSHIP"}))
            @RequestParam(required = false) String employmentType,

            @Parameter(description = "Filter by required experience level",
                schema = @Schema(allowableValues = {"ENTRY", "JUNIOR", "MID", "SENIOR", "LEAD", "EXECUTIVE"}))
            @RequestParam(required = false) String experienceLevel,

            @Parameter(description = "Filter by job status",
                schema = @Schema(allowableValues = {"OPEN", "CLOSED", "DRAFT"}))
            @RequestParam(required = false) String status,

            @Parameter(description = "Minimum salary filter (annual, USD)", example = "80000")
            @RequestParam(required = false) BigDecimal salaryMin,

            @Parameter(description = "Maximum salary filter (annual, USD)", example = "150000")
            @RequestParam(required = false) BigDecimal salaryMax,

            @Parameter(description = "Deadline range start date (ISO 8601: YYYY-MM-DD)", example = "2026-08-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,

            @Parameter(description = "Deadline range end date (ISO 8601: YYYY-MM-DD)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of jobs per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by",
                schema = @Schema(allowableValues = {"createdAt", "title", "deadline", "salaryMin", "salaryMax"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("REST request to get jobs list with advanced queries. Keyword: {}, CompanyId: {}, Location: {}",
                keyword, companyId, location);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        String entitySortBy = sortBy;
        if ("company".equals(sortBy)) {
            entitySortBy = "company.name";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, entitySortBy));
        PageResponse<JobResponse> response = jobService.getJobs(
                keyword, companyId, location, employmentType, experienceLevel, status,
                salaryMin, salaryMax, deadlineFrom, deadlineTo, pageable
        );
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Jobs retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the details of a specific job posting.
     * Publicly accessible (no token required).
     */
    @GetMapping("/{id}")
    @SecurityRequirements   // Public endpoint — no JWT required
    @Operation(
        summary = "Get job details by ID (Public)",
        description = "Retrieve detailed information of a single job posting by its UUID. **No authentication required.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobResponse>> getJobById(
            @Parameter(description = "UUID of the job posting", required = true, example = "550e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID id
    ) {
        log.info("REST request to get job: {}", id);
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Job details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing job posting.
     * Accessible by authenticated users only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update a job posting",
        description = "Modify details of an existing job posting. Requires JWT authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<JobResponse>> updateJob(
            @Parameter(description = "UUID of the job to update", required = true, example = "550e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        log.info("REST request to update job: {}", id);
        JobResponse response = jobService.updateJob(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Job updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a job posting.
     * Accessible by authenticated users only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete a job posting",
        description = "Soft-deletes a job posting from the system. Requires JWT authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Job deleted successfully",
                      "timestamp": "2026-07-19T10:00:00Z"
                    }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteJob(
            @Parameter(description = "UUID of the job to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete job: {}", id);
        jobService.deleteJob(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Job deleted successfully"));
    }
}
