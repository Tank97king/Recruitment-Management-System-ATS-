package com.example.ats.service;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewStatusRequest;
import com.example.ats.dto.response.InterviewResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for managing interviews.
 */
public interface InterviewService {

    /**
     * Schedules a new interview for a job application.
     *
     * @param request interview creation details
     * @return details of the scheduled interview DTO
     */
    InterviewResponse scheduleInterview(CreateInterviewRequest request);

    /**
     * Retrieves a paginated list of interviews with search filtering.
     *
     * @param candidateId optional filter by candidate ID
     * @param companyId   optional filter by company ID
     * @param statusStr   optional filter by interview status
     * @param typeStr     optional filter by interview type
     * @param date        optional filter by interview date
     * @param pageable    pagination and sorting
     * @return page response containing interview details DTOs
     */
    PageResponse<InterviewResponse> getInterviews(
            UUID candidateId,
            UUID companyId,
            String statusStr,
            String typeStr,
            LocalDate date,
            Pageable pageable
    );

    /**
     * Retrieves an interview by ID.
     *
     * @param id interview UUID
     * @return details of the interview DTO
     */
    InterviewResponse getInterviewById(UUID id);

    /**
     * Updates details of an existing interview.
     *
     * @param id      interview UUID
     * @param request updated interview details
     * @return details of the updated interview DTO
     */
    InterviewResponse updateInterview(UUID id, UpdateInterviewRequest request);

    /**
     * Updates the status of an interview (PATCH).
     *
     * @param id      interview UUID
     * @param request updated status details
     * @return details of the updated interview DTO
     */
    InterviewResponse updateStatus(UUID id, UpdateInterviewStatusRequest request);

    /**
     * Deletes (soft-deletes) an interview.
     *
     * @param id interview UUID
     */
    void deleteInterview(UUID id);
}
