package com.example.ats.util;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.request.UpdateCandidateRequest;
import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for Candidate entity and DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface CandidateMapper {

    @Mapping(target = "fullName", expression = "java(candidate.getFirstName() + (candidate.getLastName() == null || candidate.getLastName().isEmpty() ? \"\" : \" \" + candidate.getLastName()))")
    @Mapping(target = "skills", expression = "java(mapTagsToSkills(candidate.getTags()))")
    @Mapping(target = "hasCv", expression = "java(candidate.getCv() != null)")
    @Mapping(target = "cvFileName", expression = "java(candidate.getCv() != null ? candidate.getCv().getOriginalFileName() : null)")
    @Mapping(target = "uploadedAt", expression = "java(candidate.getCv() != null ? candidate.getCv().getUploadedAt() : null)")
    CandidateResponse toResponse(Candidate candidate);

    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "portfolioUrl", ignore = true)
    @Mapping(target = "resumePath", ignore = true)
    @Mapping(target = "resumeOriginalName", ignore = true)
    @Mapping(target = "resumeUploadedAt", ignore = true)
    @Mapping(target = "cv", ignore = true)
    Candidate toEntity(CreateCandidateRequest request);

    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "portfolioUrl", ignore = true)
    @Mapping(target = "resumePath", ignore = true)
    @Mapping(target = "resumeOriginalName", ignore = true)
    @Mapping(target = "resumeUploadedAt", ignore = true)
    @Mapping(target = "cv", ignore = true)
    void updateEntityFromRequest(UpdateCandidateRequest request, @MappingTarget Candidate candidate);

    default List<String> mapTagsToSkills(List<CandidateTag> tags) {
        if (tags == null) {
            return java.util.Collections.emptyList();
        }
        return tags.stream().map(CandidateTag::getTag).toList();
    }
}
