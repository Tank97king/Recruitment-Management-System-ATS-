package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for updating interview details.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "UpdateInterviewRequest",
    description = "Request payload for updating details of an existing interview."
)
public class UpdateInterviewRequest {

    @NotNull(message = "Interview date is required")
    @Future(message = "Interview date must be in the future")
    @Schema(
        description = "Updated date and time of the interview (ISO 8601 datetime, must be in the future)",
        example = "2026-08-15T10:30:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime interviewDate;

    @NotBlank(message = "Interview type is required")
    @Size(max = 100, message = "Interview type must not exceed 100 characters")
    @Schema(
        description = "Updated mode of interview",
        example = "VIDEO_CALL",
        maxLength = 100,
        allowableValues = {"VIDEO_CALL", "PHONE", "IN_PERSON", "TECHNICAL", "HR"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewType;

    @NotBlank(message = "Interviewer name is required")
    @Size(max = 255, message = "Interviewer name must not exceed 255 characters")
    @Schema(
        description = "Updated interviewer full name",
        example = "Michael Johnson",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewerName;

    @NotBlank(message = "Interviewer email is required")
    @Email(message = "Interviewer email must be a valid email address")
    @Size(max = 255, message = "Interviewer email must not exceed 255 characters")
    @Schema(
        description = "Updated interviewer email address",
        example = "m.johnson@techcorp.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewerEmail;

    @Size(max = 500, message = "Meeting location must not exceed 500 characters")
    @Schema(
        description = "Updated physical location for in-person interviews",
        example = "TechCorp HQ, 3rd Floor Conference Room B",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String meetingLocation;

    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    @Schema(
        description = "Updated video call meeting link",
        example = "https://meet.google.com/abc-defg-hij",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String meetingLink;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Schema(
        description = "Updated internal preparation notes",
        example = "Review the candidate's GitHub profile before the technical test.",
        maxLength = 2000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String notes;
}
