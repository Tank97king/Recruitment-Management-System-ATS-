package com.example.ats.service;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.request.UpdateCandidateRequest;
import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing candidates.
 */
public interface CandidateService {

    /**
     * Creates a new candidate profile.
     *
     * @param request candidate creation details
     * @return the created candidate details DTO
     */
    CandidateResponse createCandidate(CreateCandidateRequest request);

    /**
     * Retrieves a paginated list of candidate profiles with search filtering.
     *
     * @param search   optional keyword matching name, email, or phone
     * @param pageable pagination and sorting
     * @return page response containing candidate response DTOs
     */
    PageResponse<CandidateResponse> getCandidates(String search, Pageable pageable);

    /**
     * Retrieves a specific candidate profile by ID.
     *
     * @param id UUID of the candidate
     * @return the candidate details DTO
     */
    CandidateResponse getCandidateById(UUID id);

    /**
     * Updates an existing candidate profile.
     *
     * @param id      UUID of the candidate to update
     * @param request updated candidate details
     * @return the updated candidate details DTO
     */
    CandidateResponse updateCandidate(UUID id, UpdateCandidateRequest request);

    /**
     * Deletes (soft-deletes) a candidate profile.
     * Fails if the candidate has any job applications.
     *
     * @param id UUID of the candidate to delete
     */
    void deleteCandidate(UUID id);

    /**
     * Uploads and attaches a CV (PDF format) to a candidate profile.
     * Replaces previous CV if exists.
     *
     * @param candidateId candidate UUID
     * @param file        multipart resume file
     * @return updated candidate details DTO
     */
    CandidateResponse uploadCv(UUID candidateId, org.springframework.web.multipart.MultipartFile file);

    /**
     * Retrieves the CV metadata for a candidate.
     *
     * @param candidateId candidate UUID
     * @return the candidate CV metadata entity
     */
    com.example.ats.entity.CandidateCv getCvMetadata(UUID candidateId);

    /**
     * Deletes the CV file from disk and metadata from the database.
     *
     * @param candidateId candidate UUID
     */
    void deleteCv(UUID candidateId);
}
