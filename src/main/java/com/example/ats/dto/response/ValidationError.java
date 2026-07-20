package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a single field-level validation error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ValidationError",
    description = "A single field-level validation error returned when request body validation fails."
)
public class ValidationError {

    @Schema(description = "The field name that failed validation", example = "email")
    private String field;

    @Schema(description = "The validation error message for this field", example = "Email must be valid")
    private String message;
}
