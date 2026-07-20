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
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for scheduling a new interview.
 *
 * <p>Enforces input validation rules using Jakarta Validation API annotations.
 * Business rules (e.g., job application existence) are enforced in the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CreateInterviewRequest",
    description = "Request payload for scheduling a new interview. " +
                  "The linked job application must be in INTERVIEW status."
)
public class CreateInterviewRequest {

    @NotNull(message = "Job Application ID is required")
    @Schema(
        description = "UUID of the job application this interview is linked to",
        example = "550e8400-e29b-41d4-a716-446655440003",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID jobApplicationId;

    @NotNull(message = "Interview date is required")
    @Future(message = "Interview date must be in the future")
    @Schema(
        description = "Scheduled date and time of the interview (ISO 8601 datetime, must be in the future)",
        example = "2026-08-15T10:30:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime interviewDate;

    @NotBlank(message = "Interview type is required")
    @Size(max = 100, message = "Interview type must not exceed 100 characters")
    @Schema(
        description = "Mode of interview (e.g., VIDEO_CALL, PHONE, IN_PERSON, TECHNICAL, HR)",
        example = "VIDEO_CALL",
        maxLength = 100,
        allowableValues = {"VIDEO_CALL", "PHONE", "IN_PERSON", "TECHNICAL", "HR"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewType;

    @NotBlank(message = "Interviewer name is required")
    @Size(max = 255, message = "Interviewer name must not exceed 255 characters")
    @Schema(
        description = "Full name of the interviewer",
        example = "Michael Johnson",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewerName;

    @NotBlank(message = "Interviewer email is required")
    @Email(message = "Interviewer email must be a valid email address")
    @Size(max = 255, message = "Interviewer email must not exceed 255 characters")
    @Schema(
        description = "Email address of the interviewer for calendar invites",
        example = "m.johnson@techcorp.com",
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String interviewerEmail;

    @Size(max = 500, message = "Meeting location must not exceed 500 characters")
    @Schema(
        description = "Physical location for in-person interviews (optional)",
        example = "TechCorp HQ, 3rd Floor Conference Room B, San Francisco, CA",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String meetingLocation;

    @Size(max = 500, message = "Meeting link must not exceed 500 characters")
    @Schema(
        description = "Video call link for remote interviews (optional)",
        example = "https://meet.google.com/abc-defg-hij",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String meetingLink;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Schema(
        description = "Internal notes or preparation instructions for the interview (optional)",
        example = "Focus on system design questions. Please review the candidate's GitHub profile beforehand.",
        maxLength = 2000,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String notes;
}
