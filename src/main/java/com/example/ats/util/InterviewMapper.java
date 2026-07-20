package com.example.ats.util;

import com.example.ats.dto.response.InterviewResponse;
import com.example.ats.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * MapStruct mapper for Interview entity and DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "candidateName", expression = "java(interview.getApplication().getCandidate().getFirstName() + (interview.getApplication().getCandidate().getLastName() == null || interview.getApplication().getCandidate().getLastName().isEmpty() ? \"\" : \" \" + interview.getApplication().getCandidate().getLastName()))")
    @Mapping(target = "jobTitle", source = "application.job.title")
    @Mapping(target = "companyName", source = "application.job.company.name")
    @Mapping(target = "interviewDate", expression = "java(mapInstantToLocalDateTime(interview.getScheduledAt()))")
    @Mapping(target = "interviewerName", source = "interviewerNames")
    @Mapping(target = "interviewerEmail", source = "interviewerEmail")
    @Mapping(target = "meetingLocation", source = "location")
    @Mapping(target = "meetingLink", source = "meetingLink")
    InterviewResponse toResponse(Interview interview);

    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
