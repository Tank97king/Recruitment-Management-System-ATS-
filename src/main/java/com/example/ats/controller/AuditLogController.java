package com.example.ats.controller;

import com.example.ats.dto.response.AuditLogResponse;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for querying system audit logs.
 */
@RestController
@RequestMapping(ApiConstants.AUDIT_LOGS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Log Management", description = "Query system audit trails. Admin only.")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Retrieves a paginated list of system audit logs with optional filtering.
     * Accessible by ADMIN users only.
     */
    @GetMapping
    @Operation(
        summary = "Get audit logs (Admin only)",
        description = "Query audit trail logs filtered by action type, resource type, user email, and date range. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Filter by action type", example = "CREATE")
            @RequestParam(required = false) String action,

            @Parameter(description = "Filter by resource type", example = "JOB")
            @RequestParam(required = false) String resourceType,

            @Parameter(description = "Filter by email of user who performed the action", example = "john.doe@example.com")
            @RequestParam(required = false) String userEmail,

            @Parameter(description = "Filter records on or after this start date (ISO 8601: YYYY-MM-DD)", example = "2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Filter records on or before this end date (ISO 8601: YYYY-MM-DD)", example = "2026-07-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of audit logs per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field to sort by", schema = @Schema(allowableValues = {"createdAt", "action", "resourceType", "userEmail"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("REST request to query audit logs. Action: {}, ResourceType: {}, UserEmail: {}", action, resourceType, userEmail);

        Sort sort = sortDirection.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        PageResponse<AuditLogResponse> response = auditLogService.getAuditLogs(
                action, resourceType, userEmail, startInstant, endInstant, pageable);

        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Audit logs retrieved successfully", response));
    }
}
