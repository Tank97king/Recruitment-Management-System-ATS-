package com.example.ats.util;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting Job entities to/from DTOs.
 */
@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface JobMapper {

    @Mapping(target = "company", source = "company")
    @Mapping(target = "employmentType", expression = "java(job.getEmploymentType() != null ? job.getEmploymentType().name() : null)")
    @Mapping(target = "experienceLevel", expression = "java(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : null)")
    @Mapping(target = "status", expression = "java(job.getStatus() != null ? job.getStatus().name() : null)")
    JobResponse toResponse(Job job);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "employmentType", ignore = true)
    @Mapping(target = "experienceLevel", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "currency", ignore = true)
    Job toEntity(CreateJobRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "employmentType", ignore = true)
    @Mapping(target = "experienceLevel", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "currency", ignore = true)
    void updateEntityFromRequest(UpdateJobRequest request, @MappingTarget Job job);
}
