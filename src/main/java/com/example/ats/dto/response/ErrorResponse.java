package com.example.ats.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standardized API error response payload returned by GlobalExceptionHandler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    name = "ErrorResponse",
    description = "Standardized error response payload returned when an API call fails."
)
public class ErrorResponse {

    @Builder.Default
    @Schema(description = "Always false for error responses", example = "false")
    private boolean success = false;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Human-readable error message", example = "Validation failed for request body")
    private String message;

    @Schema(description = "List of field-level validation errors (present only for HTTP 400 validation failures)")
    private List<ValidationError> errors;

    @Schema(description = "Timestamp when the error occurred (UTC ISO 8601)", example = "2026-07-19T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "The request path that triggered the error", example = "/api/auth/login")
    private String path;

    /**
     * Factory method for creating a standard error response without field validation errors.
     */
    public static ErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(httpStatus.value())
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * Factory method for creating a validation error response containing field-level errors.
     */
    public static ErrorResponse validationError(String path, String message, List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .errors(validationErrors != null ? validationErrors : Collections.emptyList())
                .build();
    }
}
