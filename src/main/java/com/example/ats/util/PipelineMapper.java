package com.example.ats.util;

import com.example.ats.dto.response.PipelineApplicationResponse;
import com.example.ats.entity.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for JobApplication to PipelineApplicationResponse.
 */
@Mapper(componentModel = "spring")
public interface PipelineMapper {

    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateName", expression = "java(application.getCandidate().getFirstName() + (application.getCandidate().getLastName() == null || application.getCandidate().getLastName().isEmpty() ? \"\" : \" \" + application.getCandidate().getLastName()))")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.company.name")
    @Mapping(target = "currentStatus", source = "status")
    PipelineApplicationResponse toApplicationResponse(JobApplication application);
}
