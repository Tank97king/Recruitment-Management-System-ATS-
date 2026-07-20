package com.example.ats.service;

import com.example.ats.dto.response.ApplicationStatisticsResponse;
import com.example.ats.dto.response.CandidateStatisticsResponse;
import com.example.ats.dto.response.CompanyStatisticsResponse;
import com.example.ats.dto.response.DashboardSummaryResponse;
import com.example.ats.dto.response.JobStatisticsResponse;

/**
 * Service interface for generating administrative system dashboard metrics and analytics.
 */
public interface DashboardService {

    /**
     * Calculates overall system totals (users, companies, jobs, candidates, applications, interviews).
     *
     * @return summary response containing system entity totals
     */
    DashboardSummaryResponse getDashboardSummary();

    /**
     * Calculates application counts and percentage breakdowns by stage.
     *
     * @return application statistics response containing counts and percentages
     */
    ApplicationStatisticsResponse getApplicationStatistics();

    /**
     * Calculates job posting metrics grouped by status (OPEN, CLOSED, DRAFT).
     *
     * @return job statistics response containing counts by status
     */
    JobStatisticsResponse getJobStatistics();

    /**
     * Calculates candidate metrics regarding CV/resume uploads.
     *
     * @return candidate statistics response containing CV counts
     */
    CandidateStatisticsResponse getCandidateStatistics();

    /**
     * Calculates company metrics regarding active open job postings.
     *
     * @return company statistics response containing active job counts
     */
    CompanyStatisticsResponse getCompanyStatistics();
}
