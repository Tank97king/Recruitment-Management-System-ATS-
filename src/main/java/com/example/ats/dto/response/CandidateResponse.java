package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing candidate details response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CandidateResponse",
    description = "Candidate profile details returned by candidate management endpoints."
)
public class CandidateResponse {

    @Schema(description = "Unique identifier of the candidate", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @Schema(description = "Full name of the candidate", example = "Jane Smith")
    private String fullName;

    @Schema(description = "Email address of the candidate", example = "jane.smith@email.com")
    private String email;

    @Schema(description = "Contact phone number of the candidate", example = "+1-555-987-6543")
    private String phone;

    @Schema(description = "Candidate's date of birth (ISO 8601: YYYY-MM-DD)", example = "1990-05-15", type = "string", format = "date")
    private LocalDate dateOfBirth;

    @Schema(description = "Candidate's gender", example = "Female")
    private String gender;

    @Schema(description = "Current residential address of the candidate", example = "456 Oak Avenue, Austin, TX 78701")
    private String address;

    @Schema(description = "Total years of professional work experience", example = "7")
    private Integer yearsOfExperience;

    @Schema(description = "Highest educational qualification", example = "Bachelor of Computer Science — MIT (2012)")
    private String highestEducation;

    @Schema(description = "List of candidate skills", example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Docker\"]")
    private List<String> skills;

    @Schema(description = "Professional summary / bio", example = "Experienced software engineer with 7+ years in Java microservices...")
    private String summary;

    @Schema(description = "Whether the candidate has an uploaded CV/resume", example = "true")
    private Boolean hasCv;

    @Schema(description = "Original filename of the uploaded CV (null if no CV uploaded)", example = "jane_smith_resume.pdf")
    private String cvFileName;

    @Schema(description = "Timestamp when the CV was uploaded (null if no CV)", example = "2026-05-20T11:00:00Z")
    private Instant uploadedAt;

    @Schema(description = "Timestamp when the candidate profile was created (UTC ISO 8601)", example = "2026-03-10T09:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the candidate profile was last updated (UTC ISO 8601)", example = "2026-07-18T16:00:00Z")
    private Instant updatedAt;
}
