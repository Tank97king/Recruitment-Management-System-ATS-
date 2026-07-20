package com.example.ats.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic API success response wrapper.
 *
 * <p>All successful API responses are wrapped in this structure to provide
 * a consistent contract for API consumers. The {@code data} field uses Java
 * generics so this single class covers all resource types.
 *
 * <p>Example JSON output (single resource):
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Job created successfully",
 *   "timestamp": "2026-07-19T07:00:00Z",
 *   "data": {
 *     "id": "uuid-here",
 *     "title": "Senior Java Developer"
 *   }
 * }
 * }</pre>
 *
 * <p>Example JSON output (paginated list):
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Jobs retrieved successfully",
 *   "timestamp": "2026-07-19T07:00:00Z",
 *   "data": {
 *     "content": [...],
 *     "page": 0,
 *     "size": 20,
 *     "totalElements": 150,
 *     "totalPages": 8,
 *     "last": false
 *   }
 * }
 * }</pre>
 *
 * @param <T> the type of the {@code data} payload
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "ApiResponse",
    description = "Standard success response wrapper returned by all ATS API endpoints."
)
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the result", example = "Job created successfully")
    private String message;

    @Schema(description = "Server timestamp when the response was generated (UTC ISO 8601)", example = "2026-07-19T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "Response data payload — can be a single object, a list, or a paginated response object")
    private T data;

    /**
     * Creates a successful response with data payload.
     *
     * @param message descriptive success message (e.g., "Job created successfully")
     * @param data    the response body data
     * @param <T>     the type of the data payload
     * @return a wrapped success response
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Creates a successful response without a data payload.
     * Used for operations like delete, logout where there is no meaningful body.
     *
     * @param message descriptive success message
     * @param <T>     the type parameter (will be Void in practice)
     * @return a wrapped success response with no data field
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
