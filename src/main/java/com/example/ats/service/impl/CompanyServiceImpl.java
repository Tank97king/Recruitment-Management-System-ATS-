package com.example.ats.service.impl;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.request.UpdateCompanyRequest;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.Company;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.service.CompanyService;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.CompanyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link CompanyService} for managing companies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final CompanyMapper companyMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        log.info("Creating company with name: {}", request.getCompanyName());
        
        if (companyRepository.existsByNameAndIsDeletedFalse(request.getCompanyName())) {
            throw new DuplicateResourceException("Company", "name", request.getCompanyName());
        }

        Company company = companyMapper.toEntity(request);
        Company savedCompany = companyRepository.save(company);

        auditLogService.logAction("COMPANY_CREATE", "COMPANY", savedCompany.getId().toString(), "Created company: " + savedCompany.getName());
        
        CompanyResponse response = companyMapper.toResponse(savedCompany);
        response.setTotalJobs(0L);
        return response;
    }

    @Override
    public PageResponse<CompanyResponse> getCompanies(String search, Pageable pageable) {
        log.info("Fetching companies list. Search: {}, Pageable: {}", search, pageable);
        
        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Company> companiesPage = companyRepository.findAllActiveWithSearch(searchKeyword, pageable);
        
        Page<CompanyResponse> dtoPage = companiesPage.map(company -> {
            CompanyResponse response = companyMapper.toResponse(company);
            long totalJobs = jobRepository.countByCompanyIdAndIsDeletedFalse(company.getId());
            response.setTotalJobs(totalJobs);
            return response;
        });

        return PageResponse.from(dtoPage);
    }

    @Override
    public CompanyResponse getCompanyById(UUID id) {
        log.info("Fetching company by ID: {}", id);
        
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
                
        CompanyResponse response = companyMapper.toResponse(company);
        long totalJobs = jobRepository.countByCompanyIdAndIsDeletedFalse(id);
        response.setTotalJobs(totalJobs);
        return response;
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request) {
        log.info("Updating company with ID: {}", id);
        
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        // Check company name uniqueness if changed
        if (!company.getName().equalsIgnoreCase(request.getCompanyName())) {
            if (companyRepository.existsByNameAndIsDeletedFalse(request.getCompanyName())) {
                throw new DuplicateResourceException("Company", "name", request.getCompanyName());
            }
        }

        companyMapper.updateEntityFromRequest(request, company);
        Company updatedCompany = companyRepository.save(company);

        auditLogService.logAction("COMPANY_UPDATE", "COMPANY", updatedCompany.getId().toString(), "Updated company: " + updatedCompany.getName());
        
        CompanyResponse response = companyMapper.toResponse(updatedCompany);
        long totalJobs = jobRepository.countByCompanyIdAndIsDeletedFalse(id);
        response.setTotalJobs(totalJobs);
        return response;
    }

    @Override
    @Transactional
    public void deleteCompany(UUID id) {
        log.info("Deleting company with ID: {}", id);
        
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        // Ensure there are no active (OPEN) jobs
        long activeJobs = jobRepository.countByCompanyIdAndStatusAndIsDeletedFalse(id, JobStatus.OPEN);
        if (activeJobs > 0) {
            throw new BusinessRuleViolationException("Cannot delete company with active job postings");
        }

        company.setIsDeleted(true);
        companyRepository.save(company);

        auditLogService.logAction("COMPANY_DELETE", "COMPANY", id.toString(), "Deleted company: " + company.getName());
        log.info("Company with ID: {} soft-deleted successfully", id);
    }
}
