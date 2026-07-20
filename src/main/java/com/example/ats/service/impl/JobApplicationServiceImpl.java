package com.example.ats.service.impl;

import com.example.ats.dto.request.CreateJobApplicationRequest;
import com.example.ats.dto.request.UpdateApplicationStatusRequest;
import com.example.ats.dto.response.JobApplicationResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BadRequestException;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.CvRequiredException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.InvalidStatusTransitionException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.service.JobApplicationService;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.JobApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import com.example.ats.service.EmailService;

/**
 * Service implementation for managing job applications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
        log.info("Submitting job application for candidate {} to job {}", request.getCandidateId(), request.getJobId());

        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", request.getCandidateId()));

        Job job = jobRepository.findByIdAndIsDeletedFalse(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        // Enforce: Job status must be OPEN
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessRuleViolationException("Cannot apply to a job that is not OPEN");
        }

        // NOTE: CV requirement temporarily disabled for demo data seeding.
        // Re-enable for production:
        // if (candidate.getCv() == null) {
        //     throw new CvRequiredException("Candidate must upload a CV before applying for a job");
        // }

        // Enforce: A candidate cannot apply to the same job more than once
        if (jobApplicationRepository.existsByJobIdAndCandidateIdAndIsDeletedFalse(request.getJobId(), request.getCandidateId())) {
            throw new DuplicateResourceException("JobApplication", "jobId + candidateId", request.getJobId() + " + " + request.getCandidateId());
        }

        JobApplication application = new JobApplication();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(Instant.now());

        JobApplication saved = jobApplicationRepository.save(application);
        log.info("Job application created successfully with ID: {}", saved.getId());

        auditLogService.logAction("APPLICATION_CREATE", "JOB_APPLICATION", saved.getId().toString(), "Submitted application for job: " + job.getTitle());

        return jobApplicationMapper.toResponse(saved);
    }

    @Override
    public PageResponse<JobApplicationResponse> getApplications(
            UUID candidateId,
            UUID jobId,
            UUID companyId,
            String statusStr,
            Pageable pageable
    ) {
        log.info("Fetching job applications with filters. CandidateId: {}, JobId: {}, CompanyId: {}, Status: {}",
                candidateId, jobId, companyId, statusStr);

        ApplicationStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = ApplicationStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid application status filter: " + statusStr);
            }
        }

        Page<JobApplication> page = jobApplicationRepository.findAllActiveWithFilters(
                candidateId, jobId, companyId, status, pageable);

        Page<JobApplicationResponse> dtoPage = page.map(jobApplicationMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public JobApplicationResponse getApplicationById(UUID id) {
        log.info("Fetching job application by ID: {}", id);

        JobApplication application = jobApplicationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", id));

        return jobApplicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public JobApplicationResponse updateStatus(UUID id, UpdateApplicationStatusRequest request) {
        log.info("Updating status for job application: {}", id);

        JobApplication application = jobApplicationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", id));

        ApplicationStatus nextStatus;
        try {
            nextStatus = ApplicationStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        ApplicationStatus currentStatus = application.getStatus();

        // Validate state transition flow rules
        if (!isValidTransition(currentStatus, nextStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Invalid status transition from %s to %s", currentStatus, nextStatus)
            );
        }

        try {
            application.setStatus(nextStatus);
            JobApplication updated = jobApplicationRepository.save(application);
            log.info("Successfully updated status of job application {} to {}", id, nextStatus);

            auditLogService.logAction("APPLICATION_STATUS_UPDATE", "JOB_APPLICATION", id.toString(), "Updated application status to: " + nextStatus);

            if (currentStatus != nextStatus) {
                String candidateName = (application.getCandidate().getFirstName() != null ? application.getCandidate().getFirstName() : "") +
                        (application.getCandidate().getLastName() != null ? " " + application.getCandidate().getLastName() : "");
                emailService.sendApplicationStatusUpdate(
                        application.getCandidate().getEmail(),
                        candidateName,
                        application.getJob().getTitle(),
                        application.getJob().getCompany().getName(),
                        nextStatus.name()
                );
            }

            return jobApplicationMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("EXPLICIT ERROR IN UPDATE_STATUS for application {}: {}", id, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void deleteApplication(UUID id) {
        log.info("Deleting job application: {}", id);

        JobApplication application = jobApplicationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", id));

        application.setIsDeleted(true);
        jobApplicationRepository.save(application);
        log.info("Job application {} soft-deleted successfully", id);
    }

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) {
            return true;
        }
        switch (current) {
            case APPLIED:
                return next == ApplicationStatus.REVIEWING || next == ApplicationStatus.REJECTED;
            case REVIEWING:
                return next == ApplicationStatus.INTERVIEW || next == ApplicationStatus.REJECTED;
            case INTERVIEW:
                return next == ApplicationStatus.OFFER || next == ApplicationStatus.REJECTED;
            case OFFER:
                return next == ApplicationStatus.HIRED || next == ApplicationStatus.REJECTED;
            case HIRED:
            case REJECTED:
            case WITHDRAWN:
            default:
                return false;
        }
    }
}
