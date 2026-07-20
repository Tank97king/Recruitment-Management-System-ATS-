package com.example.ats.service;

import com.example.ats.dto.request.CreateJobApplicationRequest;
import com.example.ats.dto.request.UpdateApplicationStatusRequest;
import com.example.ats.dto.response.JobApplicationResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing job applications.
 */
public interface JobApplicationService {

    /**
     * Submits a new job application.
     *
     * @param request application creation details
     * @return details of the submitted job application DTO
     */
    JobApplicationResponse createApplication(CreateJobApplicationRequest request);

    /**
     * Retrieves a paginated list of job applications with filtering.
     *
     * @param candidateId optional candidate ID filter
     * @param jobId       optional job ID filter
     * @param companyId   optional company ID filter
     * @param statusStr   optional status filter
     * @param pageable    pagination and sorting
     * @return page response containing job application details DTOs
     */
    PageResponse<JobApplicationResponse> getApplications(
            UUID candidateId,
            UUID jobId,
            UUID companyId,
            String statusStr,
            Pageable pageable
    );

    /**
     * Retrieves a job application by ID.
     *
     * @param id application UUID
     * @return details of the job application DTO
     */
    JobApplicationResponse getApplicationById(UUID id);

    /**
     * Updates the status of an existing job application.
     * Enforces the business rules for state transitions.
     *
     * @param id      application UUID
     * @param request updated status details
     * @return details of the updated job application DTO
     */
    JobApplicationResponse updateStatus(UUID id, UpdateApplicationStatusRequest request);

    /**
     * Deletes (soft-deletes) a job application.
     *
     * @param id application UUID
     */
    void deleteApplication(UUID id);
}
