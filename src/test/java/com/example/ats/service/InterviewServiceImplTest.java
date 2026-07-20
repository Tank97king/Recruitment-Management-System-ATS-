package com.example.ats.service;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewStatusRequest;
import com.example.ats.dto.response.InterviewResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.Company;
import com.example.ats.entity.Interview;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.InterviewStatus;
import com.example.ats.enums.InterviewType;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.InvalidInterviewDateException;
import com.example.ats.exception.InvalidInterviewTypeException;
import com.example.ats.exception.InvalidStatusTransitionException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.InterviewRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.service.impl.InterviewServiceImpl;
import com.example.ats.util.InterviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
 * Unit tests for {@link InterviewServiceImpl}.
 *
 * <p>Covers interview scheduling (including past-date guard and type-validation),
 * status updates (state machine), and cancellation (soft-delete).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewServiceImpl Unit Tests")
class InterviewServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private InterviewMapper interviewMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private InterviewServiceImpl interviewService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID interviewId;
    private UUID applicationId;
    private JobApplication interviewApplication;
    private Interview scheduledInterview;
    private InterviewResponse interviewResponse;
    private CreateInterviewRequest createRequest;

    @BeforeEach
    void setUp() {
        interviewId   = UUID.randomUUID();
        applicationId = UUID.randomUUID();

        // ── Build the nested entity tree: Company → Job → Candidate → Application
        Company company = new Company();
        company.setName("TechCorp");

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(company, UUID.randomUUID());
        } catch (Exception ignored) {}

        Job job = new Job();
        job.setStatus(JobStatus.OPEN);
        job.setCompany(company);
        job.setTitle("Senior Java Engineer");

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(job, UUID.randomUUID());
        } catch (Exception ignored) {}

        Candidate candidate = new Candidate();
        candidate.setEmail("alice@example.com");
        candidate.setFirstName("Alice");
        candidate.setLastName("Smith");

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(candidate, UUID.randomUUID());
        } catch (Exception ignored) {}

        interviewApplication = new JobApplication();
        interviewApplication.setJob(job);
        interviewApplication.setCandidate(candidate);
        interviewApplication.setStatus(ApplicationStatus.INTERVIEW); // required status
        interviewApplication.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(interviewApplication, applicationId);
        } catch (Exception ignored) {}

        // ── Interview entity ──────────────────────────────────────────────
        scheduledInterview = new Interview();
        scheduledInterview.setApplication(interviewApplication);
        scheduledInterview.setInterviewType(InterviewType.VIDEO);
        scheduledInterview.setMeetingLink("https://meet.google.com/abc-defg");
        scheduledInterview.setStatus(InterviewStatus.SCHEDULED);
        scheduledInterview.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(scheduledInterview, interviewId);
        } catch (Exception ignored) {}

        interviewResponse = InterviewResponse.builder()
                .id(interviewId)
                .status(InterviewStatus.SCHEDULED)
                .interviewType(InterviewType.VIDEO)
                .build();

        // Default create request — future date, VIDEO type with meeting link
        createRequest = CreateInterviewRequest.builder()
                .jobApplicationId(applicationId)
                .interviewDate(LocalDateTime.now().plusDays(7))
                .interviewType("VIDEO")
                .interviewerName("Michael Johnson")
                .interviewerEmail("m.johnson@techcorp.com")
                .meetingLink("https://meet.google.com/abc-defg")
                .build();
    }

    // =========================================================================
    // scheduleInterview()
    // =========================================================================

    @Test
    @DisplayName("shouldScheduleInterviewSuccessfully — INTERVIEW status, future date, VIDEO type with link")
    void shouldScheduleInterviewSuccessfully() {
        // Arrange
        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(interviewApplication));
        when(interviewRepository.save(any(Interview.class))).thenReturn(scheduledInterview);
        when(interviewMapper.toResponse(scheduledInterview)).thenReturn(interviewResponse);

        // Act
        InterviewResponse result = interviewService.scheduleInterview(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(InterviewStatus.SCHEDULED);

        verify(interviewRepository, times(1)).save(any(Interview.class));
        verify(emailService, times(1)).sendInterviewInvitation(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString());
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenInterviewDateIsInThePast")
    void shouldThrowExceptionWhenInterviewDateIsInThePast() {
        // Arrange — past date (2 days ago)
        CreateInterviewRequest pastDateRequest = CreateInterviewRequest.builder()
                .jobApplicationId(applicationId)
                .interviewDate(LocalDateTime.now().minusDays(2)) // in the PAST!
                .interviewType("VIDEO")
                .interviewerName("Michael Johnson")
                .interviewerEmail("m.johnson@techcorp.com")
                .meetingLink("https://meet.google.com/abc-defg")
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(interviewApplication));

        // Act + Assert
        InvalidInterviewDateException ex = assertThrows(
                InvalidInterviewDateException.class,
                () -> interviewService.scheduleInterview(pastDateRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("future");

        verify(interviewRepository, never()).save(any());
        verify(emailService, never()).sendInterviewInvitation(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenApplicationStatusIsNotInterview")
    void shouldThrowExceptionWhenApplicationStatusIsNotInterview() {
        // Arrange — application is in APPLIED status (not INTERVIEW)
        interviewApplication.setStatus(ApplicationStatus.APPLIED);

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(interviewApplication));

        // Act + Assert
        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> interviewService.scheduleInterview(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("INTERVIEW");

        verify(interviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenVideoInterviewHasNoMeetingLink")
    void shouldThrowExceptionWhenVideoInterviewHasNoMeetingLink() {
        // Arrange — VIDEO type requires meetingLink
        CreateInterviewRequest noLinkRequest = CreateInterviewRequest.builder()
                .jobApplicationId(applicationId)
                .interviewDate(LocalDateTime.now().plusDays(7))
                .interviewType("VIDEO")
                .interviewerName("Michael Johnson")
                .interviewerEmail("m.johnson@techcorp.com")
                .meetingLink(null) // no link — must fail!
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(interviewApplication));

        // Act + Assert
        InvalidInterviewTypeException ex = assertThrows(
                InvalidInterviewTypeException.class,
                () -> interviewService.scheduleInterview(noLinkRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("link");

        verify(interviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenOnsiteInterviewHasNoLocation")
    void shouldThrowExceptionWhenOnsiteInterviewHasNoLocation() {
        // Arrange — ONSITE type requires meetingLocation
        CreateInterviewRequest noLocationRequest = CreateInterviewRequest.builder()
                .jobApplicationId(applicationId)
                .interviewDate(LocalDateTime.now().plusDays(7))
                .interviewType("ONSITE")
                .interviewerName("Michael Johnson")
                .interviewerEmail("m.johnson@techcorp.com")
                .meetingLocation(null) // no location — must fail!
                .build();

        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.of(interviewApplication));

        // Act + Assert
        InvalidInterviewTypeException ex = assertThrows(
                InvalidInterviewTypeException.class,
                () -> interviewService.scheduleInterview(noLocationRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("location");

        verify(interviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenJobApplicationNotFound")
    void shouldThrowResourceNotFoundExceptionWhenJobApplicationNotFound() {
        // Arrange
        when(jobApplicationRepository.findByIdAndIsDeletedFalse(applicationId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> interviewService.scheduleInterview(createRequest));

        verify(interviewRepository, never()).save(any());
    }

    // =========================================================================
    // updateStatus() — cancel / complete transitions
    // =========================================================================

    @Test
    @DisplayName("shouldCancelInterviewSuccessfully — SCHEDULED to CANCELLED")
    void shouldCancelInterviewSuccessfully() {
        // Arrange
        scheduledInterview.setStatus(InterviewStatus.SCHEDULED);
        UpdateInterviewStatusRequest cancelRequest = UpdateInterviewStatusRequest.builder()
                .status("CANCELLED")
                .build();

        Interview cancelledInterview = new Interview();
        cancelledInterview.setStatus(InterviewStatus.CANCELLED);
        cancelledInterview.setIsDeleted(false);

        InterviewResponse cancelledResponse = InterviewResponse.builder()
                .id(interviewId)
                .status(InterviewStatus.CANCELLED)
                .build();

        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.of(scheduledInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(cancelledInterview);
        when(interviewMapper.toResponse(any(Interview.class))).thenReturn(cancelledResponse);

        // Act
        InterviewResponse result = interviewService.updateStatus(interviewId, cancelRequest);

        // Assert
        assertThat(result.getStatus()).isEqualTo(InterviewStatus.CANCELLED);
        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    @DisplayName("shouldCompleteInterviewSuccessfully — SCHEDULED to COMPLETED")
    void shouldCompleteInterviewSuccessfully() {
        // Arrange
        scheduledInterview.setStatus(InterviewStatus.SCHEDULED);
        UpdateInterviewStatusRequest completeRequest = UpdateInterviewStatusRequest.builder()
                .status("COMPLETED")
                .build();

        Interview completedInterview = new Interview();
        completedInterview.setStatus(InterviewStatus.COMPLETED);

        InterviewResponse completedResponse = InterviewResponse.builder()
                .id(interviewId)
                .status(InterviewStatus.COMPLETED)
                .build();

        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.of(scheduledInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(completedInterview);
        when(interviewMapper.toResponse(any(Interview.class))).thenReturn(completedResponse);

        // Act
        InterviewResponse result = interviewService.updateStatus(interviewId, completeRequest);

        // Assert
        assertThat(result.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenTransitioningFromNonScheduledStatus — CANCELLED to COMPLETED")
    void shouldThrowExceptionWhenTransitioningFromNonScheduledStatus() {
        // Arrange — CANCELLED interview cannot be completed
        scheduledInterview.setStatus(InterviewStatus.CANCELLED);
        UpdateInterviewStatusRequest request = UpdateInterviewStatusRequest.builder()
                .status("COMPLETED")
                .build();

        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.of(scheduledInterview));

        // Act + Assert
        InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> interviewService.updateStatus(interviewId, request)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("CANCELLED");

        verify(interviewRepository, never()).save(any());
    }

    // =========================================================================
    // deleteInterview() — soft-delete (cancel)
    // =========================================================================

    @Test
    @DisplayName("shouldSoftDeleteInterviewSuccessfully — isDeleted flag set")
    void shouldSoftDeleteInterviewSuccessfully() {
        // Arrange
        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.of(scheduledInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(scheduledInterview);

        // Act
        interviewService.deleteInterview(interviewId);

        // Assert
        assertThat(scheduledInterview.getIsDeleted()).isTrue();
        verify(interviewRepository, times(1)).save(scheduledInterview);
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentInterview")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentInterview() {
        // Arrange
        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> interviewService.deleteInterview(interviewId));

        verify(interviewRepository, never()).save(any());
    }

    // =========================================================================
    // getInterviewById()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnInterviewWhenFoundById")
    void shouldReturnInterviewWhenFoundById() {
        // Arrange
        when(interviewRepository.findByIdAndIsDeletedFalse(interviewId))
                .thenReturn(Optional.of(scheduledInterview));
        when(interviewMapper.toResponse(scheduledInterview)).thenReturn(interviewResponse);

        // Act
        InterviewResponse result = interviewService.getInterviewById(interviewId);

        // Assert
        assertThat(result.getId()).isEqualTo(interviewId);
        verify(interviewRepository, times(1)).findByIdAndIsDeletedFalse(interviewId);
    }
}
