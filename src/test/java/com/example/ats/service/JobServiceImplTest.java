package com.example.ats.service;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.User;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.InvalidSalaryException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.impl.JobServiceImpl;
import com.example.ats.util.JobMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Unit tests for {@link JobServiceImpl}.
 *
 * <p>Covers job creation (including company-not-found and invalid-salary paths),
 * job retrieval, and job status lifecycle (close/reopen).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JobServiceImpl Unit Tests")
class JobServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private AuditLogService auditLogService;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private JobServiceImpl jobService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID jobId;
    private UUID companyId;
    private String userEmail;
    private User recruiterUser;
    private Company techCorp;
    private Job openJob;
    private JobResponse jobResponse;
    private CreateJobRequest createJobRequest;

    @BeforeEach
    void setUp() {
        jobId     = UUID.randomUUID();
        companyId = UUID.randomUUID();
        userEmail = "recruiter@techcorp.com";

        recruiterUser = new User();
        recruiterUser.setEmail(userEmail);
        recruiterUser.setFirstName("Recruiter");
        recruiterUser.setLastName("One");
        recruiterUser.setIsDeleted(false);

        techCorp = new Company();
        techCorp.setName("TechCorp");
        techCorp.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(techCorp, companyId);
        } catch (Exception ignored) {}

        openJob = new Job();
        openJob.setStatus(JobStatus.OPEN);
        openJob.setCompany(techCorp);
        openJob.setCreatedByUser(recruiterUser);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(openJob, jobId);
        } catch (Exception ignored) {}

        createJobRequest = CreateJobRequest.builder()
                .title("Senior Java Engineer")
                .companyId(companyId)
                .employmentType("FULL_TIME")
                .salaryMin(new BigDecimal("80000"))
                .salaryMax(new BigDecimal("120000"))
                .deadline(LocalDate.now().plusMonths(3))
                .build();

        jobResponse = JobResponse.builder()
                .id(jobId)
                .title("Senior Java Engineer")
                .status(JobStatus.OPEN.name())
                .build();
    }

    // =========================================================================
    // createJob()
    // =========================================================================

    @Test
    @DisplayName("shouldCreateJobSuccessfully — user + company found, valid salary, OPEN status set")
    void shouldCreateJobSuccessfully() {
        // Arrange
        when(userRepository.findByEmailAndIsDeletedFalse(userEmail))
                .thenReturn(Optional.of(recruiterUser));
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(techCorp));
        when(jobMapper.toEntity(createJobRequest)).thenReturn(openJob);
        when(jobRepository.save(any(Job.class))).thenReturn(openJob);
        when(jobMapper.toResponse(openJob)).thenReturn(jobResponse);

        // Act
        JobResponse result = jobService.createJob(createJobRequest, userEmail);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Senior Java Engineer");

        verify(userRepository, times(1)).findByEmailAndIsDeletedFalse(userEmail);
        verify(companyRepository, times(1)).findByIdAndIsDeletedFalse(companyId);
        verify(jobRepository, times(1)).save(any(Job.class));
        verify(auditLogService, times(1))
                .logAction(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenCompanyNotFoundOnJobCreate — ResourceNotFoundException")
    void shouldThrowExceptionWhenCompanyNotFoundOnJobCreate() {
        // Arrange — user exists but company is missing
        when(userRepository.findByEmailAndIsDeletedFalse(userEmail))
                .thenReturn(Optional.of(recruiterUser));
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.createJob(createJobRequest, userEmail)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("company");

        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenUserNotFoundOnJobCreate — ResourceNotFoundException")
    void shouldThrowExceptionWhenUserNotFoundOnJobCreate() {
        // Arrange — user not found
        when(userRepository.findByEmailAndIsDeletedFalse(userEmail))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.createJob(createJobRequest, userEmail)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("user");

        verify(companyRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowInvalidSalaryExceptionWhenSalaryMinExceedsMax")
    void shouldThrowInvalidSalaryExceptionWhenSalaryMinExceedsMax() {
        // Arrange — salaryMin > salaryMax is an invalid range
        CreateJobRequest badSalaryRequest = CreateJobRequest.builder()
                .title("Some Job")
                .companyId(companyId)
                .employmentType("FULL_TIME")
                .salaryMin(new BigDecimal("150000")) // min > max!
                .salaryMax(new BigDecimal("80000"))
                .deadline(LocalDate.now().plusMonths(1))
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(userEmail))
                .thenReturn(Optional.of(recruiterUser));
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(techCorp));

        // Act + Assert
        InvalidSalaryException ex = assertThrows(
                InvalidSalaryException.class,
                () -> jobService.createJob(badSalaryRequest, userEmail)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("salary");

        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowInvalidSalaryExceptionWhenSalaryIsNegative")
    void shouldThrowInvalidSalaryExceptionWhenSalaryIsNegative() {
        // Arrange — negative salary
        CreateJobRequest negSalaryRequest = CreateJobRequest.builder()
                .title("Some Job")
                .companyId(companyId)
                .employmentType("FULL_TIME")
                .salaryMin(new BigDecimal("-5000"))
                .deadline(LocalDate.now().plusMonths(1))
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(userEmail))
                .thenReturn(Optional.of(recruiterUser));
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(techCorp));

        // Act + Assert
        assertThrows(InvalidSalaryException.class,
                () -> jobService.createJob(negSalaryRequest, userEmail));

        verify(jobRepository, never()).save(any());
    }

    // =========================================================================
    // getJobById()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnJobWhenFoundById — existing open job")
    void shouldReturnJobWhenFoundById() {
        // Arrange
        when(jobRepository.findByIdAndIsDeletedFalse(jobId)).thenReturn(Optional.of(openJob));
        when(jobMapper.toResponse(openJob)).thenReturn(jobResponse);

        // Act
        JobResponse result = jobService.getJobById(jobId);

        // Assert
        assertThat(result.getId()).isEqualTo(jobId);
        verify(jobRepository, times(1)).findByIdAndIsDeletedFalse(jobId);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenJobDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenJobDoesNotExist() {
        // Arrange
        when(jobRepository.findByIdAndIsDeletedFalse(jobId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> jobService.getJobById(jobId));
    }

    // =========================================================================
    // updateJob() — close / reopen scenarios
    // =========================================================================

    @Test
    @DisplayName("shouldCloseJobSuccessfully — update status to CLOSED")
    void shouldCloseJobSuccessfully() {
        // Arrange — job is currently OPEN
        Job closedJob = new Job();
        closedJob.setStatus(JobStatus.CLOSED);
        closedJob.setCompany(techCorp);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(closedJob, jobId);
        } catch (Exception ignored) {}

        UpdateJobRequest closeRequest = UpdateJobRequest.builder()
                .title("Senior Java Engineer")
                .companyId(companyId)
                .employmentType("FULL_TIME")
                .status("CLOSED")
                .deadline(LocalDate.now().plusMonths(1))
                .build();

        JobResponse closedResponse = JobResponse.builder()
                .id(jobId)
                .title("Senior Java Engineer")
                .status("CLOSED")
                .build();

        when(jobRepository.findByIdAndIsDeletedFalse(jobId)).thenReturn(Optional.of(openJob));
        when(companyRepository.findByIdAndIsDeletedFalse(companyId)).thenReturn(Optional.of(techCorp));
        when(jobMapper.toResponse(any(Job.class))).thenReturn(closedResponse);
        when(jobRepository.save(any(Job.class))).thenReturn(closedJob);

        // Act
        JobResponse result = jobService.updateJob(jobId, closeRequest);

        // Assert
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenUpdatingClosedJobWithoutReopening — business rule")
    void shouldThrowExceptionWhenUpdatingClosedJobWithoutReopening() {
        // Arrange — job is already CLOSED
        openJob.setStatus(JobStatus.CLOSED);

        UpdateJobRequest updateRequest = UpdateJobRequest.builder()
                .title("New Title")
                .companyId(companyId)
                .employmentType("FULL_TIME")
                .status("CLOSED") // Not reopening — should fail
                .deadline(LocalDate.now().plusMonths(1))
                .build();

        when(jobRepository.findByIdAndIsDeletedFalse(jobId)).thenReturn(Optional.of(openJob));

        // Act + Assert
        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> jobService.updateJob(jobId, updateRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("CLOSED");
        verify(jobRepository, never()).save(any());
    }

    // =========================================================================
    // deleteJob()
    // =========================================================================

    @Test
    @DisplayName("shouldSoftDeleteJobSuccessfully — sets isDeleted flag")
    void shouldSoftDeleteJobSuccessfully() {
        // Arrange
        when(jobRepository.findByIdAndIsDeletedFalse(jobId)).thenReturn(Optional.of(openJob));
        when(jobRepository.save(any(Job.class))).thenReturn(openJob);

        // Act
        jobService.deleteJob(jobId);

        // Assert
        assertThat(openJob.getIsDeleted()).isTrue();
        verify(jobRepository, times(1)).save(openJob);
    }
}
