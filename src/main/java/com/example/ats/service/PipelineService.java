package com.example.ats.service;

import com.example.ats.dto.request.UpdateApplicationStageRequest;
import com.example.ats.dto.response.PipelineApplicationResponse;
import com.example.ats.dto.response.PipelineResponse;
import com.example.ats.dto.response.PipelineSummaryResponse;

import java.util.UUID;

/**
 * Service interface for managing the recruitment pipeline.
 */
public interface PipelineService {

    /**
     * Retrieves the Kanban pipeline columns with applications, filtered by optional filters.
     *
     * @param jobId       optional job ID filter
     * @param companyId   optional company ID filter
     * @param recruiterId optional recruiter ID filter
     * @return pipeline Kanban response containing columns for all stages
     */
    PipelineResponse getPipeline(UUID jobId, UUID companyId, UUID recruiterId);

    /**
     * Retrieves the Kanban pipeline for a specific job.
     *
     * @param jobId job UUID
     * @return pipeline Kanban response for the specified job
     */
    PipelineResponse getPipelineByJob(UUID jobId);

    /**
     * Moves an application to a new stage in the recruitment pipeline.
     *
     * @param applicationId application UUID
     * @param request       update request containing target stage status
     * @return updated application pipeline response
     */
    PipelineApplicationResponse moveApplicationStage(UUID applicationId, UpdateApplicationStageRequest request);

    /**
     * Retrieves summary statistics for the recruitment pipeline.
     *
     * @param jobId       optional job ID filter
     * @param companyId   optional company ID filter
     * @param recruiterId optional recruiter ID filter
     * @return pipeline summary statistics response
     */
    PipelineSummaryResponse getPipelineSummary(UUID jobId, UUID companyId, UUID recruiterId);
}
