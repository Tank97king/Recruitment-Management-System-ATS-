package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagination metadata wrapper for paginated list responses.
 *
 * <p>Wraps Spring's {@link Page} result into a clean, serializable DTO
 * so that the pagination metadata (total pages, total elements, etc.)
 * is included alongside the content list.
 *
 * <p>Usage in a controller:
 * <pre>{@code
 *   Page<JobResponse> jobPage = jobService.getJobs(pageable, filters);
 *   PageResponse<JobResponse> pageResponse = PageResponse.from(jobPage);
 *   return ResponseEntity.ok(ApiResponse.success("Jobs retrieved", pageResponse));
 * }</pre>
 *
 * <p>Example JSON output:
 * <pre>{@code
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false,
 *   "numberOfElements": 20
 * }
 * }</pre>
 *
 * @param <T> the type of items in the page content list
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PageResponse", description = "Paginated response wrapper containing content items alongside pagination metadata.")
public class PageResponse<T> {

    @Schema(description = "The list of items in the current page")
    private List<T> content;

    @Schema(description = "Zero-based current page number", example = "0")
    private int page;

    @Schema(description = "The maximum number of items per page", example = "10")
    private int size;

    @Schema(description = "Total number of items across all pages", example = "342")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "35")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Number of items in the current page (may be less than size on the last page)", example = "10")
    private int numberOfElements;

    /**
     * Factory method that converts a Spring {@link Page} into a {@link PageResponse}.
     *
     * @param page   the Spring Data Page result
     * @param <T>    the type of items
     * @return a serializable PageResponse
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
