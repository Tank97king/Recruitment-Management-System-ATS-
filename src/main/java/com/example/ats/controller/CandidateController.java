package com.example.ats.controller;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.request.UpdateCandidateRequest;
import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.CandidateCv;
import com.example.ats.exception.FileStorageException;
import com.example.ats.service.CandidateService;
import com.example.ats.service.FileStorageService;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing candidates.
 */
@RestController
@RequestMapping(ApiConstants.CANDIDATES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidate Management", description = "Manage candidate profiles including CV upload/download.")
@PreAuthorize("isAuthenticated()")
public class CandidateController {

    private final CandidateService candidateService;
    private final FileStorageService fileStorageService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/candidates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new candidate profile.
     * Accessible by authenticated users only.
     */
    @PostMapping
    @Operation(
        summary = "Create a candidate profile",
        description = "Creates a new candidate profile. **Candidate email must be unique** across all active profiles."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Candidate profile created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(name = "Candidate Created", value = """
                    {
                      "success": true,
                      "message": "Candidate profile created successfully",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440001",
                        "fullName": "Jane Smith",
                        "email": "jane.smith@email.com",
                        "phone": "+1-555-987-6543",
                        "yearsOfExperience": 7,
                        "skills": ["Java", "Spring Boot", "PostgreSQL"],
                        "hasCv": false,
                        "createdAt": "2026-07-19T10:00:00Z"
                      }
                    }""")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Candidate email already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CandidateResponse>> createCandidate(
            @Valid @RequestBody CreateCandidateRequest request
    ) {
        log.info("REST request to create candidate: {}", request.getEmail());
        CandidateResponse response = candidateService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.ats.dto.response.ApiResponse.success("Candidate profile created successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/candidates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated list of candidate profiles.
     */
    @GetMapping
    @Operation(
        summary = "Get all candidates",
        description = "Retrieve a paginated and sorted list of all active candidate profiles. " +
                      "Supports optional keyword search on full name, email, or phone."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<CandidateResponse>>> getCandidates(
            @Parameter(description = "Search keyword to filter by full name, email, or phone number", example = "Jane")
            @RequestParam(required = false) String search,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of candidates per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by",
                schema = @Schema(allowableValues = {"fullName", "email", "createdAt", "yearsOfExperience"}))
            @RequestParam(defaultValue = "fullName") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        log.info("REST request to get candidates list. Search: {}, Page: {}, Size: {}", search, page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        Sort sort;
        if ("fullName".equals(sortBy)) {
            sort = Sort.by(direction, "firstName").and(Sort.by(direction, "lastName"));
        } else {
            sort = Sort.by(direction, sortBy);
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<CandidateResponse> response = candidateService.getCandidates(search, pageable);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Candidates retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/candidates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves details of a specific candidate profile.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get candidate by ID",
        description = "Retrieve detailed profile information of a single candidate by their UUID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Candidate details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CandidateResponse>> getCandidateById(
            @Parameter(description = "UUID of the candidate", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id
    ) {
        log.info("REST request to get candidate: {}", id);
        CandidateResponse response = candidateService.getCandidateById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Candidate details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/candidates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing candidate profile.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update candidate profile",
        description = "Modify details of an existing candidate profile."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Candidate profile updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CandidateResponse>> updateCandidate(
            @Parameter(description = "UUID of the candidate to update", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCandidateRequest request
    ) {
        log.info("REST request to update candidate: {}", id);
        CandidateResponse response = candidateService.updateCandidate(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Candidate profile updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/candidates/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a candidate profile.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a candidate profile",
        description = "Soft-deletes a candidate profile. **Fails with HTTP 409** if the candidate has active job applications."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Candidate profile deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Cannot delete candidate with active job applications",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteCandidate(
            @Parameter(description = "UUID of the candidate to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete candidate: {}", id);
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Candidate profile deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/candidates/{id}/cv
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Uploads and attaches a CV (PDF format) to a candidate profile.
     */
    @PostMapping(value = "/{id}/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload candidate CV (PDF)",
        description = "Upload a PDF resume for the candidate. Replaces any existing CV. " +
                      "Only PDF files are accepted. Max file size is configured server-side."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CV uploaded successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file — only PDF format is accepted",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<CandidateResponse>> uploadCv(
            @Parameter(description = "UUID of the candidate to upload CV for", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id,
            @Parameter(description = "PDF resume file to upload", required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        log.info("REST request to upload CV for candidate: {}", id);
        CandidateResponse response = candidateService.uploadCv(id, file);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("CV uploaded successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/candidates/{id}/cv
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Downloads the attached PDF resume for the candidate.
     */
    @GetMapping("/{id}/cv")
    @Operation(
        summary = "Download candidate CV",
        description = "Downloads the attached PDF resume for the candidate as a file attachment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CV file returned as attachment",
            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate or CV not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Resource> downloadCv(
            @Parameter(description = "UUID of the candidate whose CV to download", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id
    ) {
        log.info("REST request to download CV for candidate: {}", id);
        CandidateCv cv = candidateService.getCvMetadata(id);
        Path path = fileStorageService.loadFile(cv.getFilePath());
        try {
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(cv.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cv.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read CV file: " + cv.getOriginalFileName(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/candidates/{id}/cv
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deletes the attached resume file and metadata for the candidate.
     */
    @DeleteMapping("/{id}/cv")
    @Operation(
        summary = "Delete candidate CV",
        description = "Deletes the attached resume file and its metadata for the candidate."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CV deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Candidate or CV not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteCv(
            @Parameter(description = "UUID of the candidate whose CV to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id
    ) {
        log.info("REST request to delete CV for candidate: {}", id);
        candidateService.deleteCv(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("CV deleted successfully"));
    }
}
