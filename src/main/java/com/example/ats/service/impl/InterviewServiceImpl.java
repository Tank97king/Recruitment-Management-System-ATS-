package com.example.ats.service.impl;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewStatusRequest;
import com.example.ats.dto.response.InterviewResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.Interview;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.InterviewStatus;
import com.example.ats.enums.InterviewType;
import com.example.ats.exception.BadRequestException;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.InvalidInterviewDateException;
import com.example.ats.exception.InvalidInterviewTypeException;
import com.example.ats.exception.InvalidStatusTransitionException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.InterviewRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.service.InterviewService;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.InterviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.example.ats.service.EmailService;

/**
 * Service implementation for managing interviews.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewMapper interviewMapper;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(CreateInterviewRequest request) {
        log.info("Scheduling interview for job application {}", request.getJobApplicationId());

        JobApplication application = jobApplicationRepository.findByIdAndIsDeletedFalse(request.getJobApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", request.getJobApplicationId()));

        // Enforce: Job Application status must be INTERVIEW
        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new BusinessRuleViolationException("Job Application status must be INTERVIEW to schedule an interview");
        }

        // Enforce: Interview date must be in the future
        validateInterviewDate(request.getInterviewDate());

        // Validate interview type and modality requirements (meetingLink / meetingLocation)
        InterviewType interviewType = parseAndValidateInterviewType(
                request.getInterviewType(), request.getMeetingLink(), request.getMeetingLocation());

        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setScheduledAt(request.getInterviewDate().toInstant(ZoneOffset.UTC));
        interview.setInterviewType(interviewType);
        interview.setInterviewerNames(request.getInterviewerName());
        interview.setInterviewerEmail(request.getInterviewerEmail());
        interview.setLocation(request.getMeetingLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNotes(request.getNotes());
        interview.setStatus(InterviewStatus.SCHEDULED);

        Interview saved = interviewRepository.save(interview);
        log.info("Interview scheduled successfully with ID: {}", saved.getId());

        auditLogService.logAction("INTERVIEW_SCHEDULE", "INTERVIEW", saved.getId().toString(), "Scheduled interview for application: " + application.getId());

        // Trigger email invitation
        String candidateName = application.getCandidate().getFirstName() +
                (application.getCandidate().getLastName() != null ? " " + application.getCandidate().getLastName() : "");
        String locationOrLink = (interviewType == InterviewType.ONLINE || interviewType == InterviewType.VIDEO)
                ? request.getMeetingLink() : request.getMeetingLocation();

        emailService.sendInterviewInvitation(
                application.getCandidate().getEmail(),
                candidateName,
                application.getJob().getTitle(),
                application.getJob().getCompany().getName(),
                request.getInterviewDate().toString(),
                request.getInterviewType(),
                locationOrLink,
                request.getInterviewerName()
        );

        return interviewMapper.toResponse(saved);
    }

    @Override
    public PageResponse<InterviewResponse> getInterviews(
            UUID candidateId,
            UUID companyId,
            String statusStr,
            String typeStr,
            LocalDate date,
            Pageable pageable
    ) {
        log.info("Fetching interviews list with filters. CandidateId: {}, CompanyId: {}, Status: {}, Type: {}, Date: {}",
                candidateId, companyId, statusStr, typeStr, date);

        InterviewStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = InterviewStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid interview status filter: " + statusStr);
            }
        }

        InterviewType type = null;
        if (typeStr != null && !typeStr.trim().isEmpty()) {
            try {
                type = InterviewType.valueOf(typeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidInterviewTypeException("Invalid interview type filter: " + typeStr);
            }
        }

        Instant startDate = null;
        Instant endDate = null;
        if (date != null) {
            startDate = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            endDate = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        }

        Page<Interview> page = interviewRepository.findAllActiveWithFilters(
                candidateId, companyId, status, type, startDate, endDate, pageable);

        Page<InterviewResponse> dtoPage = page.map(interviewMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public InterviewResponse getInterviewById(UUID id) {
        log.info("Fetching interview details by ID: {}", id);

        Interview interview = interviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewResponse updateInterview(UUID id, UpdateInterviewRequest request) {
        log.info("Updating interview details for ID: {}", id);

        Interview interview = interviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        // Validate date and type requirements
        validateInterviewDate(request.getInterviewDate());
        InterviewType interviewType = parseAndValidateInterviewType(
                request.getInterviewType(), request.getMeetingLink(), request.getMeetingLocation());

        interview.setScheduledAt(request.getInterviewDate().toInstant(ZoneOffset.UTC));
        interview.setInterviewType(interviewType);
        interview.setInterviewerNames(request.getInterviewerName());
        interview.setInterviewerEmail(request.getInterviewerEmail());
        interview.setLocation(request.getMeetingLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNotes(request.getNotes());

        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} updated successfully", id);

        auditLogService.logAction("INTERVIEW_UPDATE", "INTERVIEW", id.toString(), "Updated interview details");

        return interviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public InterviewResponse updateStatus(UUID id, UpdateInterviewStatusRequest request) {
        log.info("Updating status for interview: {} to {}", id, request.getStatus());

        Interview interview = interviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        InterviewStatus nextStatus;
        try {
            nextStatus = InterviewStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        InterviewStatus currentStatus = interview.getStatus();
        if (currentStatus != nextStatus) {
            if (currentStatus != InterviewStatus.SCHEDULED) {
                throw new InvalidStatusTransitionException(
                        String.format("Cannot transition interview status from %s to %s", currentStatus, nextStatus)
                );
            }
        }

        interview.setStatus(nextStatus);
        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} status updated to {}", id, nextStatus);

        auditLogService.logAction("INTERVIEW_UPDATE", "INTERVIEW", id.toString(), "Updated interview status to: " + nextStatus);

        return interviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInterview(UUID id) {
        log.info("Deleting interview: {}", id);

        Interview interview = interviewRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", id));

        interview.setIsDeleted(true);
        interviewRepository.save(interview);

        auditLogService.logAction("INTERVIEW_DELETE", "INTERVIEW", id.toString(), "Cancelled/deleted interview");
        log.info("Interview {} soft-deleted successfully", id);
    }

    private void validateInterviewDate(LocalDateTime date) {
        if (date == null || !date.isAfter(LocalDateTime.now())) {
            throw new InvalidInterviewDateException("Interview date must be in the future");
        }
    }

    private InterviewType parseAndValidateInterviewType(String typeStr, String meetingLink, String meetingLocation) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new InvalidInterviewTypeException("Interview type is required");
        }

        InterviewType type;
        try {
            type = InterviewType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInterviewTypeException("Invalid interview type: " + typeStr);
        }

        if (type == InterviewType.ONLINE || type == InterviewType.VIDEO) {
            if (meetingLink == null || meetingLink.trim().isEmpty()) {
                throw new InvalidInterviewTypeException("Meeting link is required for ONLINE interviews");
            }
        } else if (type == InterviewType.OFFLINE || type == InterviewType.ONSITE) {
            if (meetingLocation == null || meetingLocation.trim().isEmpty()) {
                throw new InvalidInterviewTypeException("Meeting location is required for OFFLINE interviews");
            }
        }

        return type;
    }
}
