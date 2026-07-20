package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for company details response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "CompanyResponse",
    description = "Company profile details returned by company management endpoints."
)
public class CompanyResponse {

    @Schema(description = "Unique identifier of the company", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Official name of the company", example = "TechCorp Solutions Inc.")
    private String companyName;

    @Schema(description = "Contact email address of the company", example = "hr@techcorp.com")
    private String email;

    @Schema(description = "Contact phone number of the company", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Company website URL", example = "https://www.techcorp.com")
    private String website;

    @Schema(description = "Headquarters address of the company", example = "123 Tech Boulevard, San Francisco, CA 94105, USA")
    private String address;

    @Schema(description = "Detailed description of the company", example = "TechCorp Solutions is a leading software firm...")
    private String description;

    @Schema(description = "Total number of active job postings for this company", example = "12")
    private Long totalJobs;

    @Schema(description = "Timestamp when the company profile was created (UTC ISO 8601)", example = "2026-01-10T09:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the company profile was last updated (UTC ISO 8601)", example = "2026-07-15T14:30:00Z")
    private Instant updatedAt;
}
