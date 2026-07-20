package com.example.ats.controller;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.request.UpdateCompanyRequest;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.service.CompanyService;
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
 * REST controller for managing companies.
 * Handles endpoints for company profile creation, retrieval, updates, and deletion.
 */
@RestController
@RequestMapping(ApiConstants.COMPANIES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management", description = "Create, retrieve, update, and delete company profiles.")
@PreAuthorize("isAuthenticated()")
public class CompanyController {

    private final CompanyService companyService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/companies
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new company profile.
     * Accessible by any authenticated user.
     */
    @PostMapping
    @Operation(
        summary = "Create a company profile",
        description = "Creates a new company profile. **Company name must be unique** across all active profiles. " +
                      "Requires a valid JWT token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Company created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(name = "Company Created", value = """
                    {
                      "success": true,
                      "message": "Company created successfully",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "companyName": "TechCorp Solutions Inc.",
                        "email": "hr@techcorp.com",
                        "phone": "+1-555-123-4567",
                        "website": "https://www.techcorp.com",
                        "address": "123 Tech Boulevard, San Francisco, CA 94105",
                        "description": "Leading software development firm...",
                        "totalJobs": 0,
                        "createdAt": "2026-07-19T10:00:00Z",
                        "updatedAt": "2026-07-19T10:00:00Z"
                      }
                    }""")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed — missing company name or invalid email format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Company name already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "status": 409,
                      "message": "Company with name 'TechCorp Solutions Inc.' already exists",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "path": "/api/companies"
                    }""")))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request
    ) {
        log.info("REST request to create company: {}", request.getCompanyName());
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.ats.dto.response.ApiResponse.success("Company created successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/companies
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated, sorted list of active companies with optional keyword search.
     * Accessible by any authenticated user.
     */
    @GetMapping
    @Operation(
        summary = "Get all companies",
        description = "Retrieve a paginated and sorted list of all active companies. " +
                      "Supports optional keyword search across name, email, and address fields."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Companies retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<CompanyResponse>>> getCompanies(
            @Parameter(description = "Search keyword to filter by company name, email, or address", example = "Tech")
            @RequestParam(required = false) String search,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of companies per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "companyName",
                schema = @Schema(allowableValues = {"companyName", "createdAt", "updatedAt"}))
            @RequestParam(defaultValue = "companyName") String sortBy,

            @Parameter(description = "Sort direction", example = "asc",
                schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("REST request to get companies list. Search: {}, Page: {}, Size: {}, SortBy: {}, Direction: {}",
                search, page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        String entitySortBy = sortBy;
        if ("companyName".equals(sortBy)) {
            entitySortBy = "name";
        } else if ("address".equals(sortBy)) {
            entitySortBy = "headquartersLocation";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, entitySortBy));
        PageResponse<CompanyResponse> response = companyService.getCompanies(search, pageable);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Companies retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/companies/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the profile details of a specific company.
     * Accessible by any authenticated user.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get company by ID",
        description = "Retrieve detailed profile information of a single company by its UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Company not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CompanyResponse>> getCompanyById(
            @Parameter(description = "UUID of the company to retrieve", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        log.info("REST request to get company: {}", id);
        CompanyResponse response = companyService.getCompanyById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Company details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/companies/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing company's profile.
     * Accessible by any authenticated user.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update company details",
        description = "Modify details of an existing company profile. " +
                      "Company name uniqueness is enforced (excluding the current company being updated)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Company not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Company name already taken by another company",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CompanyResponse>> updateCompany(
            @Parameter(description = "UUID of the company to update", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        log.info("REST request to update company: {}", id);
        CompanyResponse response = companyService.updateCompany(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Company updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/companies/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a company profile.
     * Fails if there are active (OPEN) jobs associated with the company.
     * Accessible by any authenticated user.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a company profile",
        description = "Soft-deletes a company from the system. " +
                      "**Fails with HTTP 409** if the company has active OPEN job postings."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Company deleted successfully",
                      "timestamp": "2026-07-19T10:00:00Z"
                    }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Company not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cannot delete company with active job postings",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "status": 409,
                      "message": "Cannot delete company with active open job postings",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "path": "/api/companies/550e8400-e29b-41d4-a716-446655440000"
                    }""")))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteCompany(
            @Parameter(description = "UUID of the company to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete company: {}", id);
        companyService.deleteCompany(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Company deleted successfully"));
    }
}
