package com.example.ats.util;

import com.example.ats.dto.response.JobApplicationResponse;
import com.example.ats.entity.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for JobApplication entity and DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateName", expression = "java(application.getCandidate().getFirstName() + (application.getCandidate().getLastName() == null || application.getCandidate().getLastName().isEmpty() ? \"\" : \" \" + application.getCandidate().getLastName()))")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.company.name")
    @Mapping(target = "applicationStatus", source = "status")
    JobApplicationResponse toResponse(JobApplication application);
}
