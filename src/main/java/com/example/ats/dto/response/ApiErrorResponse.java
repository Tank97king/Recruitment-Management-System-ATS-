package com.example.ats.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standardized API error response body (extends ErrorResponse for backwards compatibility).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Builder.Default
    private boolean success = false;
    private int status;
    private String message;
    private Instant timestamp;
    private String path;
    private List<FieldError> errors;

    public static ApiErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return ApiErrorResponse.builder()
                .success(false)
                .status(httpStatus.value())
                .message(message)
                .timestamp(Instant.now())
                .path(path)
                .errors(Collections.emptyList())
                .build();
    }

    public static ApiErrorResponse validationError(String path, List<FieldError> fieldErrors) {
        return ApiErrorResponse.builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed. Please check the errors field.")
                .timestamp(Instant.now())
                .path(path)
                .errors(fieldErrors != null ? fieldErrors : Collections.emptyList())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;

        /** Convenience constructor when rejectedValue is not available. */
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
