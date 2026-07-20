package com.example.ats.service;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateCv;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.service.impl.CandidateServiceImpl;
import com.example.ats.util.CandidateMapper;
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
 * Unit tests for {@link CandidateServiceImpl}.
 *
 * <p>Covers candidate creation (duplicate-email guard), retrieval,
 * and deletion (business rule: cannot delete a candidate with active applications).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandidateServiceImpl Unit Tests")
class CandidateServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private CandidateMapper candidateMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuditLogService auditLogService;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private CandidateServiceImpl candidateService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID candidateId;
    private Candidate candidate;
    private CreateCandidateRequest createRequest;
    private CandidateResponse candidateResponse;

    @BeforeEach
    void setUp() {
        candidateId = UUID.randomUUID();

        candidate = new Candidate();
        candidate.setEmail("alice@example.com");
        candidate.setFirstName("Alice");
        candidate.setLastName("Smith");
        candidate.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(candidate, candidateId);
        } catch (Exception ignored) {}

        createRequest = CreateCandidateRequest.builder()
                .fullName("Alice Smith")
                .email("alice@example.com")
                .phone("0123456789")
                .build();

        candidateResponse = CandidateResponse.builder()
                .id(candidateId)
                .email("alice@example.com")
                .fullName("Alice Smith")
                .build();
    }

    // =========================================================================
    // createCandidate()
    // =========================================================================

    @Test
    @DisplayName("shouldCreateCandidateSuccessfully — unique email, entity saved")
    void shouldCreateCandidateSuccessfully() {
        // Arrange
        when(candidateRepository.existsByEmailAndIsDeletedFalse("alice@example.com"))
                .thenReturn(false);
        when(candidateMapper.toEntity(createRequest)).thenReturn(candidate);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);
        when(candidateMapper.toResponse(candidate)).thenReturn(candidateResponse);

        // Act
        CandidateResponse result = candidateService.createCandidate(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");

        verify(candidateRepository, times(1)).existsByEmailAndIsDeletedFalse("alice@example.com");
        verify(candidateRepository, times(1)).save(any(Candidate.class));
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenCandidateEmailAlreadyExists — duplicate email blocked")
    void shouldThrowExceptionWhenCandidateEmailAlreadyExists() {
        // Arrange
        when(candidateRepository.existsByEmailAndIsDeletedFalse("alice@example.com"))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> candidateService.createCandidate(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("candidate");

        // Mapper and repository save must NOT be called
        verify(candidateMapper, never()).toEntity(any());
        verify(candidateRepository, never()).save(any());
    }

    // =========================================================================
    // getCandidateById()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnCandidateWhenFoundById — existing active candidate")
    void shouldReturnCandidateWhenFoundById() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(candidateMapper.toResponse(candidate)).thenReturn(candidateResponse);

        // Act
        CandidateResponse result = candidateService.getCandidateById(candidateId);

        // Assert
        assertThat(result.getId()).isEqualTo(candidateId);
        verify(candidateRepository, times(1)).findByIdAndIsDeletedFalse(candidateId);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCandidateDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenCandidateDoesNotExist() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> candidateService.getCandidateById(candidateId)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("candidate");
    }

    // =========================================================================
    // deleteCandidate()
    // =========================================================================

    @Test
    @DisplayName("shouldDeleteCandidateSuccessfully — no active applications, soft-deletes")
    void shouldDeleteCandidateSuccessfully() {
        // Arrange — candidate found, no active applications
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobApplicationRepository.existsByCandidateIdAndIsDeletedFalse(candidateId))
                .thenReturn(false);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(candidate);

        // Act
        candidateService.deleteCandidate(candidateId);

        // Assert
        assertThat(candidate.getIsDeleted()).isTrue();
        verify(candidateRepository, times(1)).save(candidate);
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenDeletingCandidateWithExistingApplications — business rule")
    void shouldThrowExceptionWhenDeletingCandidateWithExistingApplications() {
        // Arrange — candidate has active applications
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));
        when(jobApplicationRepository.existsByCandidateIdAndIsDeletedFalse(candidateId))
                .thenReturn(true);

        // Act + Assert
        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> candidateService.deleteCandidate(candidateId)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("application");

        // Candidate must NOT be soft-deleted
        verify(candidateRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCandidate")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCandidate() {
        // Arrange
        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> candidateService.deleteCandidate(candidateId));

        verify(candidateRepository, never()).save(any());
    }

    // =========================================================================
    // getCvMetadata()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnCvMetadataWhenCandidateHasCv")
    void shouldReturnCvMetadataWhenCandidateHasCv() {
        // Arrange — candidate with a CV attached
        CandidateCv cv = new CandidateCv();
        cv.setOriginalFileName("alice_resume.pdf");
        cv.setFilePath("/uploads/cv/alice_resume.pdf");
        cv.setFileSize(102400L);
        candidate.setCv(cv);

        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));

        // Act
        CandidateCv result = candidateService.getCvMetadata(candidateId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOriginalFileName()).isEqualTo("alice_resume.pdf");
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCandidateHasNoCv")
    void shouldThrowResourceNotFoundExceptionWhenCandidateHasNoCv() {
        // Arrange — candidate has no CV
        candidate.setCv(null);

        when(candidateRepository.findByIdAndIsDeletedFalse(candidateId))
                .thenReturn(Optional.of(candidate));

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> candidateService.getCvMetadata(candidateId));
    }
}
