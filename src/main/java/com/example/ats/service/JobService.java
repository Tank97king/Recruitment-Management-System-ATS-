package com.example.ats.service;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing job postings.
 */
public interface JobService {

    /**
     * Creates a new job posting.
     *
     * @param request   job creation details
     * @param userEmail email of the authenticated user who created the job
     * @return the created job details response DTO
     */
    JobResponse createJob(CreateJobRequest request, String userEmail);

    /**
     * Retrieves a paginated list of job postings with optional filters.
     */
    PageResponse<JobResponse> getJobs(
            String keyword,
            UUID companyId,
            String location,
            String employmentType,
            String experienceLevel,
            String status,
            java.math.BigDecimal salaryMin,
            java.math.BigDecimal salaryMax,
            java.time.LocalDate deadlineFrom,
            java.time.LocalDate deadlineTo,
            Pageable pageable
    );

    /**
     * Retrieves details of a specific job by its ID.
     *
     * @param id UUID of the job to retrieve
     * @return the job response DTO
     */
    JobResponse getJobById(UUID id);

    /**
     * Updates an existing job posting.
     * Fails if the job is closed and the update doesn't reopen it.
     *
     * @param id      UUID of the job to update
     * @param request update job details
     * @return the updated job response DTO
     */
    JobResponse updateJob(UUID id, UpdateJobRequest request);

    /**
     * Deletes (soft-deletes) an existing job posting.
     *
     * @param id UUID of the job to delete
     */
    void deleteJob(UUID id);
}
