package com.example.ats.repository.specification;

import com.example.ats.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/**
 * JPA Specification builder for dynamic filtering of {@link AuditLog} records.
 */
public class AuditLogSpecification {

    public static Specification<AuditLog> filterAuditLogs(
            String action,
            String resourceType,
            String userEmail,
            Instant startDate,
            Instant endDate
    ) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (action != null && !action.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("action")), action.trim().toUpperCase()));
            }

            if (resourceType != null && !resourceType.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("resourceType")), resourceType.trim().toUpperCase()));
            }

            if (userEmail != null && !userEmail.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("userEmail")), "%" + userEmail.trim().toLowerCase() + "%"));
            }

            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return predicate;
        };
    }
}
