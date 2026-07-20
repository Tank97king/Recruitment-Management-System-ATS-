package com.example.ats.service;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.entity.Company;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.service.impl.CompanyServiceImpl;
import com.example.ats.util.CompanyMapper;
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
 * Unit tests for {@link CompanyServiceImpl}.
 *
 * <p>Covers company creation (including duplicate guard), retrieval,
 * and deletion — particularly the business rule preventing deletion
 * of companies that still have active job postings.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyServiceImpl Unit Tests")
class CompanyServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private AuditLogService auditLogService;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private CompanyServiceImpl companyService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID companyId;
    private Company company;
    private CreateCompanyRequest createRequest;
    private CompanyResponse companyResponse;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();

        company = new Company();
        company.setName("TechCorp");
        company.setIndustry("Technology");
        company.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(company, companyId);
        } catch (Exception ignored) {}

        createRequest = CreateCompanyRequest.builder()
                .companyName("TechCorp")
                .build();

        companyResponse = CompanyResponse.builder()
                .id(companyId)
                .companyName("TechCorp")
                .totalJobs(0L)
                .build();
    }

    // =========================================================================
    // createCompany()
    // =========================================================================

    @Test
    @DisplayName("shouldCreateCompanySuccessfully — no duplicate, entity saved")
    void shouldCreateCompanySuccessfully() {
        // Arrange
        when(companyRepository.existsByNameAndIsDeletedFalse("TechCorp")).thenReturn(false);
        when(companyMapper.toEntity(createRequest)).thenReturn(company);
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toResponse(company)).thenReturn(companyResponse);

        // Act
        CompanyResponse result = companyService.createCompany(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCompanyName()).isEqualTo("TechCorp");
        assertThat(result.getTotalJobs()).isEqualTo(0L);

        verify(companyRepository, times(1)).existsByNameAndIsDeletedFalse("TechCorp");
        verify(companyRepository, times(1)).save(any(Company.class));
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenCompanyNameAlreadyExists — duplicate name blocked")
    void shouldThrowExceptionWhenCompanyNameAlreadyExists() {
        // Arrange
        when(companyRepository.existsByNameAndIsDeletedFalse("TechCorp")).thenReturn(true);

        // Act + Assert
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> companyService.createCompany(createRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("company");

        // Entity must NEVER be mapped or persisted
        verify(companyMapper, never()).toEntity(any());
        verify(companyRepository, never()).save(any());
    }

    // =========================================================================
    // getCompanyById()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnCompanyWhenFoundById — existing active company")
    void shouldReturnCompanyWhenFoundById() {
        // Arrange
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company)).thenReturn(companyResponse);
        when(jobRepository.countByCompanyIdAndIsDeletedFalse(companyId)).thenReturn(3L);

        // Act
        CompanyResponse result = companyService.getCompanyById(companyId);

        // Assert
        assertThat(result.getId()).isEqualTo(companyId);
        verify(jobRepository, times(1)).countByCompanyIdAndIsDeletedFalse(companyId);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenCompanyDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenCompanyDoesNotExist() {
        // Arrange
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> companyService.getCompanyById(companyId)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("company");
    }

    // =========================================================================
    // deleteCompany()
    // =========================================================================

    @Test
    @DisplayName("shouldDeleteCompanySuccessfully — no active jobs, soft-deletes entity")
    void shouldDeleteCompanySuccessfully() {
        // Arrange
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(company));
        when(jobRepository.countByCompanyIdAndStatusAndIsDeletedFalse(companyId, JobStatus.OPEN))
                .thenReturn(0L); // no active jobs
        when(companyRepository.save(any(Company.class))).thenReturn(company);

        // Act
        companyService.deleteCompany(companyId);

        // Assert — company is marked as deleted
        assertThat(company.getIsDeleted()).isTrue();
        verify(companyRepository, times(1)).save(company);
        verify(auditLogService, times(1)).logAction(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenDeletingCompanyWithActiveJobs — business rule enforced")
    void shouldThrowExceptionWhenDeletingCompanyWithActiveJobs() {
        // Arrange — 2 OPEN jobs exist for this company
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.of(company));
        when(jobRepository.countByCompanyIdAndStatusAndIsDeletedFalse(companyId, JobStatus.OPEN))
                .thenReturn(2L);

        // Act + Assert
        BusinessRuleViolationException ex = assertThrows(
                BusinessRuleViolationException.class,
                () -> companyService.deleteCompany(companyId)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("active job");

        // Company must NOT be soft-deleted
        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCompany")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCompany() {
        // Arrange
        when(companyRepository.findByIdAndIsDeletedFalse(companyId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> companyService.deleteCompany(companyId));

        verify(companyRepository, never()).save(any());
    }
}
