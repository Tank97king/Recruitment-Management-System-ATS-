package com.example.ats.service;

import com.example.ats.dto.response.AuditLogResponse;
import com.example.ats.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for creating and querying audit logs.
 */
public interface AuditLogService {

    /**
     * Records an audit log event using the currently authenticated user context.
     */
    void logAction(String action, String resourceType, String resourceId, String description);

    /**
     * Records an audit log event with explicit user parameters (e.g. during authentication).
     */
    void logAction(UUID userId, String userEmail, String action, String resourceType, String resourceId, String description);

    /**
     * Retrieves a paginated list of audit logs matching optional filter criteria.
     */
    PageResponse<AuditLogResponse> getAuditLogs(
            String action,
            String resourceType,
            String userEmail,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );
}
