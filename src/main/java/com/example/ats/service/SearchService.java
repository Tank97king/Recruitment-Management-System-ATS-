package com.example.ats.service;

import com.example.ats.dto.response.GlobalSearchResponse;

/**
 * Service interface for multi-entity global searching across Companies, Jobs, and Candidates.
 */
public interface SearchService {

    /**
     * Executes global search across Companies, Jobs, and Candidates matching the keyword.
     *
     * @param keyword       the required non-blank search term (max 100 chars)
     * @param page          zero-based page index
     * @param size          page size
     * @param sortBy        field to sort by (createdAt, updatedAt, name, title)
     * @param sortDirection sort direction (ASC, DESC)
     * @return global search response containing matching section results and pagination metadata
     */
    GlobalSearchResponse globalSearch(String keyword, int page, int size, String sortBy, String sortDirection);
}
