package com.example.ats.controller;

import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.GlobalSearchResponse;
import com.example.ats.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for executing unified global searches across system resources.
 */
@RestController
@RequestMapping(ApiConstants.SEARCH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Global Search", description = "Cross-entity search across Companies, Jobs, and Candidates.")
@PreAuthorize("isAuthenticated()")
public class SearchController {

    private final SearchService searchService;

    /**
     * Executes global search across Companies, Jobs, and Candidates matching the keyword.
     */
    @GetMapping
    @Operation(
        summary = "Global search across resources",
        description = "Execute a unified search query that searches across Companies, Jobs, and Candidates simultaneously. " +
                      "Returns paginated matches for each section. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<GlobalSearchResponse>> search(
            @Parameter(description = "Search keyword matched against company name, job title/description, candidate name/email", required = true, example = "Tech")
            @RequestParam(name = "keyword") String keyword,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of items per page for each entity section", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @Parameter(description = "Field to sort search results by", schema = @Schema(allowableValues = {"createdAt", "id"}))
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction", schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(name = "sortDirection", defaultValue = "ASC") String sortDirection
    ) {
        log.info("REST request to execute global search for keyword: '{}'", keyword);
        GlobalSearchResponse response = searchService.globalSearch(keyword, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Search results retrieved successfully", response));
    }
}
