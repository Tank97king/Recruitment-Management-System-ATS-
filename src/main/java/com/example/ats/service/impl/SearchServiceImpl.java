package com.example.ats.service.impl;

import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.dto.response.GlobalSearchResponse;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.dto.response.SectionResultResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.exception.BadRequestException;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.specification.CandidateSpecification;
import com.example.ats.repository.specification.CompanySpecification;
import com.example.ats.repository.specification.JobSpecification;
import com.example.ats.service.SearchService;
import com.example.ats.util.CandidateMapper;
import com.example.ats.util.CompanyMapper;
import com.example.ats.util.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service implementation for multi-entity global searching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;
    private final CandidateMapper candidateMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdat", "updatedat", "name", "title"
    );

    @Override
    public GlobalSearchResponse globalSearch(String keyword, int page, int size, String sortBy, String sortDirection) {
        log.info("Executing global search | keyword: '{}', page: {}, size: {}, sortBy: '{}', direction: '{}'",
                keyword, page, size, sortBy, sortDirection);

        // 1. Validate Keyword
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Search keyword is required and cannot be blank");
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() > 100) {
            throw new BadRequestException("Search keyword cannot exceed 100 characters");
        }

        // 2. Validate and Map Sort Field
        String rawSort = (sortBy == null || sortBy.trim().isEmpty()) ? "createdAt" : sortBy.trim();
        String normalizedSort = rawSort.toLowerCase();

        if (!ALLOWED_SORT_FIELDS.contains(normalizedSort)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Company sort property
        String companySort = switch (normalizedSort) {
            case "name", "title" -> "name";
            case "updatedat" -> "updatedAt";
            default -> "createdAt";
        };

        // Job sort property
        String jobSort = switch (normalizedSort) {
            case "name", "title" -> "title";
            case "updatedat" -> "updatedAt";
            default -> "createdAt";
        };

        // Candidate sort property
        String candidateSort = switch (normalizedSort) {
            case "name", "title" -> "firstName";
            case "updatedat" -> "updatedAt";
            default -> "createdAt";
        };

        // 3. Search Companies
        Pageable companyPageable = PageRequest.of(page, size, Sort.by(direction, companySort));
        Page<Company> companyPage = companyRepository.findAll(CompanySpecification.globalSearch(trimmedKeyword), companyPageable);
        PageResponse<CompanyResponse> companyPageResp = PageResponse.from(companyPage.map(companyMapper::toResponse));
        SectionResultResponse<CompanyResponse> companySection = SectionResultResponse.<CompanyResponse>builder()
                .totalResults(companyPage.getTotalElements())
                .data(companyPageResp)
                .build();

        // 4. Search Jobs
        Pageable jobPageable = PageRequest.of(page, size, Sort.by(direction, jobSort));
        Page<Job> jobPage = jobRepository.findAll(JobSpecification.globalSearch(trimmedKeyword), jobPageable);
        PageResponse<JobResponse> jobPageResp = PageResponse.from(jobPage.map(jobMapper::toResponse));
        SectionResultResponse<JobResponse> jobSection = SectionResultResponse.<JobResponse>builder()
                .totalResults(jobPage.getTotalElements())
                .data(jobPageResp)
                .build();

        // 5. Search Candidates
        Pageable candidatePageable = PageRequest.of(page, size, Sort.by(direction, candidateSort));
        Page<Candidate> candidatePage = candidateRepository.findAll(CandidateSpecification.globalSearch(trimmedKeyword), candidatePageable);
        PageResponse<CandidateResponse> candidatePageResp = PageResponse.from(candidatePage.map(candidateMapper::toResponse));
        SectionResultResponse<CandidateResponse> candidateSection = SectionResultResponse.<CandidateResponse>builder()
                .totalResults(candidatePage.getTotalElements())
                .data(candidatePageResp)
                .build();

        return GlobalSearchResponse.builder()
                .companies(companySection)
                .jobs(jobSection)
                .candidates(candidateSection)
                .build();
    }
}
