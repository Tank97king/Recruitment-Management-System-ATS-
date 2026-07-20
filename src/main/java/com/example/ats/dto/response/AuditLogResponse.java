package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object representing an Audit Log entry in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "AuditLogResponse",
    description = "A single audit log entry recording a system action performed by a user."
)
public class AuditLogResponse {

    @Schema(description = "Unique identifier of the audit log entry", example = "550e8400-e29b-41d4-a716-446655440099")
    private UUID id;

    @Schema(description = "The action performed (e.g., CREATE, UPDATE, DELETE, LOGIN)", example = "CREATE")
    private String action;

    @Schema(description = "The type of resource that was acted upon (e.g., JOB, CANDIDATE, USER)", example = "JOB")
    private String resourceType;

    @Schema(description = "The UUID string of the resource that was acted upon", example = "550e8400-e29b-41d4-a716-446655440002")
    private String resourceId;

    @Schema(description = "Email address of the user who performed the action", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Human-readable description of what was done", example = "User john.doe@example.com created Job: Senior Java Backend Engineer")
    private String description;

    @Schema(description = "IP address from which the action was performed", example = "192.168.1.105")
    private String ipAddress;

    @Schema(description = "Timestamp when the action was performed (UTC ISO 8601)", example = "2026-07-19T09:45:00Z")
    private Instant createdAt;
}
