package com.example.ats.service;

import com.example.ats.dto.request.CreateJobApplicationRequest;
import com.example.ats.dto.request.UpdateApplicationStatusRequest;
import com.example.ats.dto.response.JobApplicationResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateCv;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.CvRequiredException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.InvalidStatusTransitionException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.service.impl.JobApplicationServiceImpl;
import com.example.ats.util.JobApplicationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JobApplicationServiceImpl}.
 *
 * <p>Covers the core application workflow:
 * <ul>
 *   <li>Successful application submission</li>
 *   <li>Duplicate application guard</li>
 *   <li>CV-required guard</li>
 *   <li>Status transition validation (state machine)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JobApplicationServiceImpl Unit Tests")
class JobApplicationServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID candidateId;
    private UUID jobId;
    private UUID applicationId;
    private Candidate candidate;
    private Job openJob;
    private JobApplication application;
    private JobApplicationResponse applicationResponse;
    private CreateJobApplicationRequest createRequest;
    private Company techCorp;

    @BeforeEach
    void setUp() {
        candidateId   = UUID.randomUUID();
        jobId         = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        // ── Candidate with a CV ───────────────────────────────────────────
        CandidateCv cv = new CandidateCv();
        cv.setOriginalFileName("resume.pdf");
        cv.setFilePath("/uploads/cv/resume.pdf");

        candidate = new Candidate();
        candidate.setEmail("alice@example.com");
        candidate.setFirstName("Alice");
        candidate.setLastName("Smith");
        candidate.setIsDeleted(false);
        candidate.setCv(cv);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(candidate, candidateId);
        } catch (Exception ignored) {}

        // ── Company + open job ────────────────────────────────────────────
        techCorp = new Company();
        techCorp.setName("TechCorp");

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(techCorp, UUID.randomUUID());
        } catch (Exception ignored) {}

        openJob = new Job();
        openJob.setStatus(JobStatus.OPEN);
        openJob.setCompany(techCorp);
        openJob.setTitle("Senior Java Engineer");

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(openJob, jobId);
        } catch (Exception ignored) {}

        // ── Application entity ────────────────────────────────────────────
        application = new JobApplication();
        application.setCandidate(candidate);
        application.setJob(openJob);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(application, applicationId);
        } catch (Exception ignored) {}

        applicationResponse = JobApplicationResponse.builder()
                .id(applicationId)
                .applicationStatus(ApplicationStatus.APPLIED)
                .build();

        createRequest = CreateJobApplicationRequest.builder()
                .candidateId(candidateId)
                .jobId(jobId)
                .coverLetter("I am very interested in this position.")
                .build();
    }

    // =========================================================================
    // createApplication()
    // =========================================================================

    @Test
    @DisplayName("shouldApplySuccessfully — candidate has CV, job is OPEN, no duplicate")
    void shouldApplySuccessfully() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobRepository.findByIdAndIsDeletedFalse(jobId))
                .thenReturn(Optional.of(openJob));
        when(jobApplicationRepository.existsByJobIdAndCandidateIdAndIsDeletedFalse(jobId, candidateId))
                .thenReturn(false);
        when(jobApplicationRepository.save(any(JobApplication.class)))
                .thenReturn(application);
        when(jobApplicationMapper.toResponse(application))
                .thenReturn(applicationResponse);

        // Act
        JobApplicationResponse result = jobApplicationService.createApplication(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.APPLIED);

        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenDuplicateApplicationExists — same job + candidate")
    void shouldThrowExceptionWhenDuplicateApplicationExists() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobRepository.findByIdAndIsDeletedFalse(jobId))
                .thenReturn(Optional.of(openJob));
        when(jobApplicationRepository.existsByJobIdAndCandidateIdAndIsDeletedFalse(jobId, candidateId))
                .thenReturn(true); // duplicate!

        // Act + Assert
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> jobApplicationService.createApplication(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("jobapplication");

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowCvRequiredExceptionWhenCandidateHasNoCV")
    void shouldThrowCvRequiredExceptionWhenCandidateHasNoCV() {
        // Arrange — candidate has no CV
        candidate.setCv(null);

        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobRepository.findByIdAndIsDeletedFalse(jobId))
                .thenReturn(Optional.of(openJob));

        // Act + Assert
        CvRequiredException ex = assertThrows(
                CvRequiredException.class,
                () -> jobApplicationService.createApplication(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("CV");

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenJobIsNotOpen — closed job rejects applications")
    void shouldThrowExceptionWhenJobIsNotOpen() {
        // Arrange — job is CLOSED
        openJob.setStatus(JobStatus.CLOSED);

        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobRepository.findByIdAndIsDeletedFalse(jobId))
                .thenReturn(Optional.of(openJob));

        // Act + Assert
        com.example.ats.exception.BusinessRuleViolationException ex = assertThrows(
                com.example.ats.exception.BusinessRuleViolationException.class,
                () -> jobApplicationService.createApplication(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("OPEN");

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCandidateNotFound")
    void shouldThrowResourceNotFoundExceptionWhenCandidateNotFound() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> jobApplicationService.createApplication(createRequest));

        verify(jobApplicationRepository, never()).save(any());
    }

    // =========================================================================
    // updateStatus() — state machine validation
    // =========================================================================

    @Test
    @DisplayName("shouldUpdateApplicationStatusSuccessfully — APPLIED to REVIEWING")
    void shouldUpdateApplicationStatusSuccessfully() {
        // Arrange
        application.setStatus(ApplicationStatus.APPLIED);
        UpdateApplicationStatusRequest statusRequest = UpdateApplicationStatusRequest.builder()
                .status("REVIEWING")
                .build();

        JobApplicationResponse updatedResponse = JobApplicationResponse.builder()
                .id(applicationId)
                .applicationStatus(ApplicationStatus.REVIEWING)
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(jobApplicationMapper.toResponse(any(JobApplication.class))).thenReturn(updatedResponse);

        // Act
        JobApplicationResponse result = jobApplicationService.updateStatus(applicationId, statusRequest);

        // Assert
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.REVIEWING);
        verify(emailService, times(1)).sendApplicationStatusUpdate(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowInvalidStatusTransitionExceptionForIllegalTransition — HIRED to APPLIED")
    void shouldThrowInvalidStatusTransitionExceptionForIllegalTransition() {
        // Arrange — terminal HIRED state cannot transition back to APPLIED
        application.setStatus(ApplicationStatus.HIRED);
        UpdateApplicationStatusRequest statusRequest = UpdateApplicationStatusRequest.builder()
                .status("APPLIED")
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(application));

        // Act + Assert
        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> jobApplicationService.updateStatus(applicationId, statusRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("HIRED");

        verify(jobApplicationRepository, never()).save(any());
        verify(emailService, never()).sendApplicationStatusUpdate(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("shouldThrowInvalidStatusTransitionExceptionForIllegalTransition — REJECTED to OFFER")
    void shouldThrowInvalidStatusTransitionExceptionWhenTransitioningFromRejectedToOffer() {
        // Arrange — terminal REJECTED state cannot transition
        application.setStatus(ApplicationStatus.REJECTED);
        UpdateApplicationStatusRequest statusRequest = UpdateApplicationStatusRequest.builder()
                .status("OFFER")
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(application));

        // Act + Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> jobApplicationService.updateStatus(applicationId, statusRequest));

        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldAllowSameStatusTransition — APPLIED to APPLIED is a no-op")
    void shouldAllowSameStatusTransition() {
        // Arrange — same status transition is always valid (no-op)
        application.setStatus(ApplicationStatus.APPLIED);
        UpdateApplicationStatusRequest statusRequest = UpdateApplicationStatusRequest.builder()
                .status("APPLIED")
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(jobApplicationMapper.toResponse(any())).thenReturn(applicationResponse);

        // Act
        JobApplicationResponse result = jobApplicationService.updateStatus(applicationId, statusRequest);

        // Assert — no exception; save is still called; email is NOT sent (status unchanged)
        assertThat(result).isNotNull();
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.APPLIED);
        verify(jobApplicationRepository, times(1)).save(any());
        verify(emailService, never()).sendApplicationStatusUpdate(any(), any(), any(), any(), any());
    }

    // =========================================================================
    // deleteApplication()
    // =========================================================================

    @Test
    @DisplayName("shouldSoftDeleteApplicationSuccessfully")
    void shouldSoftDeleteApplicationSuccessfully() {
        // Arrange
        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);

        // Act
        jobApplicationService.deleteApplication(applicationId);

        // Assert
        assertThat(application.getIsDeleted()).isTrue();
        verify(jobApplicationRepository, times(1)).save(application);
    }
}
