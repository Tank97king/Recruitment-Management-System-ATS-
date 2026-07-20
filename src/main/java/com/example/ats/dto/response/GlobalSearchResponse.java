package com.example.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Combined Data Transfer Object (DTO) containing search results for Companies, Jobs, and Candidates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponse {

    private SectionResultResponse<CompanyResponse> companies;
    private SectionResultResponse<JobResponse> jobs;
    private SectionResultResponse<CandidateResponse> candidates;
}
