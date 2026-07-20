package com.example.ats.service.impl;

import com.example.ats.dto.response.ApplicationStatisticsResponse;
import com.example.ats.dto.response.CandidateStatisticsResponse;
import com.example.ats.dto.response.CompanyStatisticsResponse;
import com.example.ats.dto.response.DashboardSummaryResponse;
import com.example.ats.dto.response.JobStatisticsResponse;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.JobStatus;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.InterviewRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for compiling dashboard analytics and statistics.
 * Uses optimized repository JPQL count and aggregation queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Fetching overall dashboard summary metrics.");

        long users = userRepository.countByIsDeletedFalse();
        long companies = companyRepository.countByIsDeletedFalse();
        long jobs = jobRepository.countByIsDeletedFalse();
        long candidates = candidateRepository.countByIsDeletedFalse();
        long applications = jobApplicationRepository.countByIsDeletedFalse();
        long interviews = interviewRepository.countByIsDeletedFalse();

        return DashboardSummaryResponse.builder()
                .totalUsers(users)
                .totalCompanies(companies)
                .totalJobs(jobs)
                .totalCandidates(candidates)
                .totalApplications(applications)
                .totalInterviews(interviews)
                .build();
    }

    @Override
    public ApplicationStatisticsResponse getApplicationStatistics() {
        log.info("Fetching application status statistics.");

        List<Object[]> rows = jobApplicationRepository.countByStatusForDashboard();
        Map<ApplicationStatus, Long> countsMap = new EnumMap<>(ApplicationStatus.class);
        long total = 0;

        for (Object[] row : rows) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            countsMap.put(status, count);
            total += count;
        }

        long applied = countsMap.getOrDefault(ApplicationStatus.APPLIED, 0L);
        long reviewing = countsMap.getOrDefault(ApplicationStatus.REVIEWING, 0L);
        long interview = countsMap.getOrDefault(ApplicationStatus.INTERVIEW, 0L);
        long offer = countsMap.getOrDefault(ApplicationStatus.OFFER, 0L);
        long hired = countsMap.getOrDefault(ApplicationStatus.HIRED, 0L);
        long rejected = countsMap.getOrDefault(ApplicationStatus.REJECTED, 0L);

        return ApplicationStatisticsResponse.builder()
                .applied(applied)
                .appliedPercentage(calculatePercentage(applied, total))
                .reviewing(reviewing)
                .reviewingPercentage(calculatePercentage(reviewing, total))
                .interview(interview)
                .interviewPercentage(calculatePercentage(interview, total))
                .offer(offer)
                .offerPercentage(calculatePercentage(offer, total))
                .hired(hired)
                .hiredPercentage(calculatePercentage(hired, total))
                .rejected(rejected)
                .rejectedPercentage(calculatePercentage(rejected, total))
                .totalApplications(total)
                .build();
    }

    @Override
    public JobStatisticsResponse getJobStatistics() {
        log.info("Fetching job status statistics.");

        List<Object[]> rows = jobRepository.countJobStatuses();
        Map<JobStatus, Long> countsMap = new EnumMap<>(JobStatus.class);
        long total = 0;

        for (Object[] row : rows) {
            JobStatus status = (JobStatus) row[0];
            Long count = (Long) row[1];
            countsMap.put(status, count);
            total += count;
        }

        long openJobs = countsMap.getOrDefault(JobStatus.OPEN, 0L);
        long closedJobs = countsMap.getOrDefault(JobStatus.CLOSED, 0L);
        long draftJobs = countsMap.getOrDefault(JobStatus.DRAFT, 0L);

        return JobStatisticsResponse.builder()
                .totalJobs(total)
                .openJobs(openJobs)
                .closedJobs(closedJobs)
                .draftJobs(draftJobs)
                .build();
    }

    @Override
    public CandidateStatisticsResponse getCandidateStatistics() {
        log.info("Fetching candidate resume statistics.");

        long total = candidateRepository.countByIsDeletedFalse();
        long withCv = candidateRepository.countByCvNotNullAndIsDeletedFalse();
        long withoutCv = Math.max(0, total - withCv);

        return CandidateStatisticsResponse.builder()
                .totalCandidates(total)
                .candidatesWithCv(withCv)
                .candidatesWithoutCv(withoutCv)
                .build();
    }

    @Override
    public CompanyStatisticsResponse getCompanyStatistics() {
        log.info("Fetching company active job statistics.");

        long total = companyRepository.countByIsDeletedFalse();
        long withActiveJobs = companyRepository.countCompaniesWithActiveJobs();
        long withoutJobs = Math.max(0, total - withActiveJobs);

        return CompanyStatisticsResponse.builder()
                .totalCompanies(total)
                .companiesWithActiveJobs(withActiveJobs)
                .companiesWithoutJobs(withoutJobs)
                .build();
    }

    private double calculatePercentage(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        double val = (double) part / total * 100.0;
        return Math.round(val * 100.0) / 100.0;
    }
}
