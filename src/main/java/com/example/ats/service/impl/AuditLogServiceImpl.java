package com.example.ats.service.impl;

import com.example.ats.dto.response.AuditLogResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.AuditLog;
import com.example.ats.entity.User;
import com.example.ats.repository.AuditLogRepository;
import com.example.ats.repository.specification.AuditLogSpecification;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation for managing audit log persistence and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void logAction(String action, String resourceType, String resourceId, String description) {
        UUID userId = null;
        String userEmail = "system";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof User user) {
                userId = user.getId();
                userEmail = user.getEmail();
            } else if (!"anonymousUser".equals(auth.getPrincipal()) && auth.getName() != null) {
                userEmail = auth.getName();
            }
        }

        logAction(userId, userEmail, action, resourceType, resourceId, description);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID userId, String userEmail, String action, String resourceType, String resourceId, String description) {
        try {
            String ipAddress = resolveClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail != null ? userEmail : "system")
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.info("Audit Event | Action: {} | Resource: {}:{} | User: {} | IP: {} | Description: {}",
                    action, resourceType, resourceId, userEmail, ipAddress, description);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(
            String action,
            String resourceType,
            String userEmail,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    ) {
        log.info("Fetching audit logs | Action: {}, ResourceType: {}, UserEmail: {}, StartDate: {}, EndDate: {}, Page: {}",
                action, resourceType, userEmail, startDate, endDate, pageable.getPageNumber());

        Specification<AuditLog> spec = AuditLogSpecification.filterAuditLogs(action, resourceType, userEmail, startDate, endDate);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(auditLogMapper::toResponse));
    }

    private String resolveClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.trace("Failed to resolve request IP address: {}", e.getMessage());
        }
        return "127.0.0.1";
    }
}
