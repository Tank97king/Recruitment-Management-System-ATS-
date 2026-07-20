package com.example.ats.service.impl;

import com.example.ats.dto.request.UpdateApplicationStageRequest;
import com.example.ats.dto.response.PipelineApplicationResponse;
import com.example.ats.dto.response.PipelineColumnResponse;
import com.example.ats.dto.response.PipelineResponse;
import com.example.ats.dto.response.PipelineSummaryResponse;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.exception.BadRequestException;
import com.example.ats.exception.InvalidStatusTransitionException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.service.PipelineService;
import com.example.ats.util.PipelineMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.ats.service.EmailService;
import com.example.ats.service.AuditLogService;

/**
 * Service implementation for managing the recruitment pipeline.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final PipelineMapper pipelineMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    private static final List<ApplicationStatus> PIPELINE_STAGES = List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.REVIEWING,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.OFFER,
            ApplicationStatus.HIRED,
            ApplicationStatus.REJECTED
    );

    @Override
    public PipelineResponse getPipeline(UUID jobId, UUID companyId, UUID recruiterId) {
        log.info("Fetching recruitment pipeline. JobId: {}, CompanyId: {}, RecruiterId: {}", jobId, companyId, recruiterId);

        if (jobId != null) {
            verifyJobExists(jobId);
        }

        List<JobApplication> applications = jobApplicationRepository.findActiveApplicationsForPipeline(jobId, companyId, recruiterId);

        Map<ApplicationStatus, List<PipelineApplicationResponse>> mappedByStage = applications.stream()
                .collect(Collectors.groupingBy(
                        JobApplication::getStatus,
                        () -> new EnumMap<>(ApplicationStatus.class),
                        Collectors.mapping(pipelineMapper::toApplicationResponse, Collectors.toList())
                ));

        List<PipelineColumnResponse> columns = new ArrayList<>();
        for (ApplicationStatus stage : PIPELINE_STAGES) {
            List<PipelineApplicationResponse> appsInStage = mappedByStage.getOrDefault(stage, List.of());
            columns.add(PipelineColumnResponse.builder()
                    .stage(stage)
                    .totalApplications(appsInStage.size())
                    .applications(appsInStage)
                    .build());
        }

        return PipelineResponse.builder()
                .columns(columns)
                .build();
    }

    @Override
    public PipelineResponse getPipelineByJob(UUID jobId) {
        log.info("Fetching recruitment pipeline for job ID: {}", jobId);
        return getPipeline(jobId, null, null);
    }

    @Override
    @Transactional
    public PipelineApplicationResponse moveApplicationStage(UUID applicationId, UpdateApplicationStageRequest request) {
        log.info("Moving application {} to new status: {}", applicationId, request.getStatus());

        JobApplication application = jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        ApplicationStatus nextStatus;
        try {
            nextStatus = ApplicationStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        ApplicationStatus currentStatus = application.getStatus();

        if (!isValidPipelineTransition(currentStatus, nextStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", currentStatus, nextStatus)
            );
        }

        application.setStatus(nextStatus);
        JobApplication updated = jobApplicationRepository.save(application);
        log.info("Successfully moved application {} to stage {}", applicationId, nextStatus);

        auditLogService.logAction("APPLICATION_STATUS_UPDATE", "JOB_APPLICATION", applicationId.toString(), "Moved application stage to: " + nextStatus);

        if (currentStatus != nextStatus) {
            String candidateName = application.getCandidate().getFirstName() +
                    (application.getCandidate().getLastName() != null ? " " + application.getCandidate().getLastName() : "");
            emailService.sendApplicationStatusUpdate(
                    application.getCandidate().getEmail(),
                    candidateName,
                    application.getJob().getTitle(),
                    application.getJob().getCompany().getName(),
                    nextStatus.name()
            );
        }

        return pipelineMapper.toApplicationResponse(updated);
    }

    @Override
    public PipelineSummaryResponse getPipelineSummary(UUID jobId, UUID companyId, UUID recruiterId) {
        log.info("Fetching recruitment pipeline summary. JobId: {}, CompanyId: {}, RecruiterId: {}", jobId, companyId, recruiterId);

        if (jobId != null) {
            verifyJobExists(jobId);
        }

        List<Object[]> statusCounts = jobApplicationRepository.countByStatusForPipeline(jobId, companyId, recruiterId);

        Map<ApplicationStatus, Long> countsMap = new EnumMap<>(ApplicationStatus.class);
        long total = 0;

        for (Object[] row : statusCounts) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            countsMap.put(status, count);
            total += count;
        }

        return PipelineSummaryResponse.builder()
                .totalApplications(total)
                .applied(countsMap.getOrDefault(ApplicationStatus.APPLIED, 0L))
                .reviewing(countsMap.getOrDefault(ApplicationStatus.REVIEWING, 0L))
                .interview(countsMap.getOrDefault(ApplicationStatus.INTERVIEW, 0L))
                .offer(countsMap.getOrDefault(ApplicationStatus.OFFER, 0L))
                .hired(countsMap.getOrDefault(ApplicationStatus.HIRED, 0L))
                .rejected(countsMap.getOrDefault(ApplicationStatus.REJECTED, 0L))
                .build();
    }

    private void verifyJobExists(UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job", "id", jobId);
        }
    }

    private boolean isValidPipelineTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) {
            return true;
        }
        if (current == ApplicationStatus.HIRED || current == ApplicationStatus.REJECTED) {
            return false;
        }
        if (next == ApplicationStatus.REJECTED) {
            return true;
        }
        switch (current) {
            case APPLIED:
                return next == ApplicationStatus.REVIEWING;
            case REVIEWING:
                return next == ApplicationStatus.INTERVIEW;
            case INTERVIEW:
                return next == ApplicationStatus.OFFER;
            case OFFER:
                return next == ApplicationStatus.HIRED;
            default:
                return false;
        }
    }
}
