package com.example.ats.util;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.request.UpdateCompanyRequest;
import com.example.ats.dto.response.CompanyResponse;
import com.example.ats.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting Company entities to/from DTOs.
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "companyName", source = "name")
    @Mapping(target = "address", source = "headquartersLocation")
    @Mapping(target = "totalJobs", ignore = true)
    CompanyResponse toResponse(Company company);

    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "headquartersLocation", source = "address")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "industry", ignore = true)
    @Mapping(target = "companySize", ignore = true)
    @Mapping(target = "foundedYear", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    Company toEntity(CreateCompanyRequest request);

    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "headquartersLocation", source = "address")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "industry", ignore = true)
    @Mapping(target = "companySize", ignore = true)
    @Mapping(target = "foundedYear", ignore = true)
    @Mapping(target = "logoPath", ignore = true)
    void updateEntityFromRequest(UpdateCompanyRequest request, @MappingTarget Company company);
}
