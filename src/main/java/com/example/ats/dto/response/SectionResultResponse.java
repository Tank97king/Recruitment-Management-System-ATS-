package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Data Transfer Object (DTO) wrapping section search results and pagination metadata.
 *
 * @param <T> the response payload entity type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SectionResultResponse", description = "A section of global search results for a specific resource type, with pagination metadata.")
public class SectionResultResponse<T> {

    @Schema(description = "Total matching records found in this section across all pages", example = "42")
    private long totalResults;

    @Schema(description = "Paginated result data for this section")
    private PageResponse<T> data;
}
