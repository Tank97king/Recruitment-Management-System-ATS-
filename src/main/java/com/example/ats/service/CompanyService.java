package com.example.ats.service;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.request.UpdateCompanyRequest;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing companies.
 */
public interface CompanyService {

    /**
     * Creates a new company profile.
     *
     * @param request details of the company to create
     * @return the created company response DTO
     */
    CompanyResponse createCompany(CreateCompanyRequest request);

    /**
     * Retrieves a paginated list of companies matching search query.
     *
     * @param search   optional search keyword for name, email, address filtering
     * @param pageable pagination and sorting information
     * @return page response containing company response DTOs
     */
    PageResponse<CompanyResponse> getCompanies(String search, Pageable pageable);

    /**
     * Retrieves a company's details by its ID.
     *
     * @param id the UUID of the company
     * @return the company details response DTO
     */
    CompanyResponse getCompanyById(UUID id);

    /**
     * Updates an existing company's details.
     *
     * @param id      the UUID of the company to update
     * @param request updated company details
     * @return the updated company response DTO
     */
    CompanyResponse updateCompany(UUID id, UpdateCompanyRequest request);

    /**
     * Deletes (soft-deletes) an existing company.
     * Fails if there are active (OPEN) jobs associated with the company.
     *
     * @param id the UUID of the company to delete
     */
    void deleteCompany(UUID id);
}
