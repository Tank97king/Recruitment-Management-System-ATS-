package com.example.ats.controller;

import com.example.ats.dto.request.UpdateUserRequest;
import com.example.ats.dto.response.ErrorResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.dto.response.UserResponse;
import com.example.ats.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

import com.example.ats.util.ApiConstants;

/**
 * REST controller for managing users.
 * Contains endpoints for viewing profiles, updating user details,
 * deleting users, and listing users with search/pagination.
 */
@RestController
@RequestMapping(ApiConstants.USERS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Manage system users, view profiles, and administer accounts. Admin only.")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a paginated, sorted, and filtered list of users.
     * Accessible by ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users (Admin only)",
        description = "Retrieve a paginated and sorted list of all active users. " +
                      "Supports optional keyword search on name/email and status filter. " +
                      "**Requires ADMIN role.**"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "status": 403,
                      "message": "Access Denied",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "path": "/api/users"
                    }""")))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<PageResponse<UserResponse>>> getUsers(
            @Parameter(description = "Search keyword to filter by name or email", example = "john")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by account status", example = "ACTIVE",
                schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE"}))
            @RequestParam(required = false) String status,

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of users per page (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field to sort by", example = "createdAt",
                schema = @Schema(allowableValues = {"fullName", "email", "createdAt", "updatedAt"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction", example = "desc",
                schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("REST request to get users list. Page: {}, Size: {}, SortBy: {}, Direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<UserResponse> response = userService.getUsers(search, status, pageable);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("Users retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves details of a specific user.
     * Accessible by ADMIN only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user by ID (Admin only)",
        description = "Retrieve detailed profile information of a single user by their UUID. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "status": 404,
                      "message": "User not found with id: 550e8400-e29b-41d4-a716-446655440000",
                      "timestamp": "2026-07-19T10:00:00Z",
                      "path": "/api/users/550e8400-e29b-41d4-a716-446655440000"
                    }""")))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "UUID of the user to retrieve", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        log.info("REST request to get user: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User details retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing user's information.
     * Accessible by ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user details (Admin only)",
        description = "Modify an existing user's full name, email, and account status. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "UUID of the user to update", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("REST request to update user: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Performs a soft delete on a user.
     * Accessible by ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Soft-delete a user (Admin only)",
        description = "Logically deletes a user by setting their `isDeleted` flag to true. " +
                      "The user can no longer login. **Requires ADMIN role.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "User deleted successfully",
                      "timestamp": "2026-07-19T10:00:00Z"
                    }"""))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — requires ADMIN role",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Void>> deleteUser(
            @Parameter(description = "UUID of the user to soft-delete", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        log.info("REST request to soft-delete user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User deleted successfully", null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the profile of the currently logged in user.
     * Accessible by any authenticated user.
     *
     * @deprecated Use {@code GET /api/v1/users/me} instead.
     */
    @Deprecated
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get my profile (deprecated)",
        description = "[DEPRECATED] Use GET /api/v1/users/me instead. " +
                      "Returns the profile details of the currently authenticated user.",
        deprecated = true
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized — JWT token missing or invalid",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<UserResponse>> getProfile(Principal principal) {
        log.info("REST request to get profile of logged in user: {}", principal.getName());
        UserResponse response = userService.getProfile(principal.getName());
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User profile retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/me
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get my profile details",
        description = "Returns the profile details of the currently authenticated user."
    )
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<UserResponse>> getMyProfile(Principal principal) {
        log.info("REST request to get /me profile of logged in user: {}", principal.getName());
        UserResponse response = userService.getProfile(principal.getName());
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User profile retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/me
    // ─────────────────────────────────────────────────────────────────────────

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update my profile details",
        description = "Updates the full name and phone of the currently authenticated user."
    )
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<UserResponse>> updateMyProfile(
            Principal principal,
            @Valid @RequestBody com.example.ats.dto.request.UpdateProfileRequest request
    ) {
        log.info("REST request to update /me profile of logged in user: {}", principal.getName());
        UserResponse response = userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(com.example.ats.dto.response.ApiResponse.success("User profile updated successfully", response));
    }
}
