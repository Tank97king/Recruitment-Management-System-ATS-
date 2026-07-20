package com.example.ats.controller;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewStatusRequest;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.InterviewResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.service.InterviewService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing scheduled interviews.
 */
@RestController
@RequestMapping(ApiConstants.INTERVIEWS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interview Management", description = "Schedule, update, and track interview sessions.")
@PreAuthorize("isAuthenticated()")
public class InterviewController {

    private final InterviewService interviewService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/interviews
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Schedules a new interview session.
     */
    @PostMapping
    @Operation(
        summary = "Schedule an interview",
        description = "Schedule a new interview for a job application. " +
                      "The linked job application must be in **INTERVIEW** status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Interview scheduled successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(name = "Interview Scheduled", value = """
                    {
                      "success": true,
                      "message": "Interview scheduled successfully",
                      "timestamp": "2026-07-20T08:00:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440004",
                        "candidateName": "Jane Smith",
                        "jobTitle": "Senior Java Backend Engineer",
                        "companyName": "TechCorp Solutions Inc.",
                        "interviewDate": "2026-08-15T10:30:00",
                        "interviewType": "VIDEO_CALL",
                        "interviewerName": "Michael Johnson",
                        "interviewerEmail": "m.johnson@techcorp.com",
                        "meetingLink": "https://meet.google.com/abc-defg-hij",
                        "status": "SCHEDULED",
                        "createdAt": "2026-07-20T08:00:00Z"
                      }
                    }""")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed or application not in INTERVIEW status",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job application not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<InterviewResponse>> scheduleInterview(
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        log.info("REST request to schedule interview for job application: {}", request.getJobApplicationId());
        InterviewResponse response = interviewService.scheduleInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.ats.dto.response.ApiResponse.success("Interview scheduled successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated list of interviews with optional filters.
     */
    @GetMapping
    @Operation(
        summary = "Get all interviews",
        description = "Retrieve a paginated list of interviews. Supports filtering by candidate, company, status, type, and date."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interviews retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<InterviewResponse>>> getInterviews(
            @Parameter(description = "Filter by candidate UUID", example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam(required = false) UUID candidateId,

            @Parameter(description = "Filter by company UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID companyId,

            @Parameter(description = "Filter by interview status",
                schema = @Schema(allowableValues = {"SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"}))
            @RequestParam(required = false) String interviewStatus,

            @Parameter(description = "Filter by interview type",
                schema = @Schema(allowableValues = {"VIDEO_CALL", "PHONE", "IN_PERSON", "TECHNICAL", "HR"}))
            @RequestParam(required = false) String interviewType,

            @Parameter(description = "Filter by interview date (ISO 8601: YYYY-MM-DD)", example = "2026-08-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate interviewDate,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of interviews per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by",
                schema = @Schema(allowableValues = {"interviewDate", "createdAt", "status"}))
            @RequestParam(defaultValue = "interviewDate") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("REST request to get interviews. Page: {}, Size: {}", page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        String entitySortBy = "interviewDate".equals(sortBy) ? "scheduledAt" : sortBy;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, entitySortBy));
        PageResponse<InterviewResponse> response = interviewService.getInterviews(
                candidateId, companyId, interviewStatus, interviewType, interviewDate, pageable);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Interviews retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves details of a specific interview session.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get interview by ID",
        description = "Retrieve detailed information of a single interview by its UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interview details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Interview not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<InterviewResponse>> getInterviewById(
            @Parameter(description = "UUID of the interview", required = true, example = "550e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id
    ) {
        log.info("REST request to get interview details for ID: {}", id);
        InterviewResponse response = interviewService.getInterviewById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Interview details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/interviews/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates details of an existing interview session.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update interview details",
        description = "Modify the date, mode, interviewer, or notes for an existing interview."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interview updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Interview not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<InterviewResponse>> updateInterview(
            @Parameter(description = "UUID of the interview to update", required = true, example = "550e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInterviewRequest request
    ) {
        log.info("REST request to update interview: {}", id);
        InterviewResponse response = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Interview updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/interviews/{id}/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates status of an existing interview session.
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update interview status",
        description = "Change the status of an interview to COMPLETED, CANCELLED, or RESCHEDULED."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interview status updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Interview not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<InterviewResponse>> updateStatus(
            @Parameter(description = "UUID of the interview to update status for", required = true, example = "550e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInterviewStatusRequest request
    ) {
        log.info("REST request to patch status of interview {} to {}", id, request.getStatus());
        InterviewResponse response = interviewService.updateStatus(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Interview status updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/interviews/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes an interview.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an interview",
        description = "Soft-deletes an interview by its UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interview deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Interview not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteInterview(
            @Parameter(description = "UUID of the interview to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete interview: {}", id);
        interviewService.deleteInterview(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Interview deleted successfully"));
    }
}
