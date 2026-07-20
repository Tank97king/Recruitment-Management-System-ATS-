package com.example.ats.dto.response;

import com.example.ats.enums.InterviewStatus;
import com.example.ats.enums.InterviewType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing interview details response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "InterviewResponse",
    description = "Interview details returned by interview management endpoints."
)
public class InterviewResponse {

    @Schema(description = "Unique identifier of the interview", example = "550e8400-e29b-41d4-a716-446655440004")
    private UUID id;

    @Schema(description = "Full name of the candidate being interviewed", example = "Jane Smith")
    private String candidateName;

    @Schema(description = "Title of the job the interview is for", example = "Senior Java Backend Engineer")
    private String jobTitle;

    @Schema(description = "Name of the company conducting the interview", example = "TechCorp Solutions Inc.")
    private String companyName;

    @Schema(description = "Scheduled date and time of the interview (ISO 8601)", example = "2026-08-15T10:30:00")
    private LocalDateTime interviewDate;

    @Schema(
        description = "Mode of the interview",
        example = "VIDEO_CALL",
        allowableValues = {"VIDEO_CALL", "PHONE", "IN_PERSON", "TECHNICAL", "HR"}
    )
    private InterviewType interviewType;

    @Schema(description = "Full name of the interviewer", example = "Michael Johnson")
    private String interviewerName;

    @Schema(description = "Email address of the interviewer", example = "m.johnson@techcorp.com")
    private String interviewerEmail;

    @Schema(description = "Physical location for in-person interviews (null for remote)", example = "TechCorp HQ, 3rd Floor Conference Room B")
    private String meetingLocation;

    @Schema(description = "Video call link for remote interviews (null for in-person)", example = "https://meet.google.com/abc-defg-hij")
    private String meetingLink;

    @Schema(
        description = "Current status of the interview",
        example = "SCHEDULED",
        allowableValues = {"SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"}
    )
    private InterviewStatus status;

    @Schema(description = "Internal notes or instructions for the interview", example = "Focus on system design questions")
    private String notes;

    @Schema(description = "Timestamp when the interview was created (UTC ISO 8601)", example = "2026-07-20T08:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the interview was last updated (UTC ISO 8601)", example = "2026-07-25T10:00:00Z")
    private Instant updatedAt;
}
