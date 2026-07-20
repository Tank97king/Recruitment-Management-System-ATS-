package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user attempts to access or modify a resource
 * they do not have permission to access.
 *
 * <p>Maps to HTTP 403 Forbidden.
 *
 * <p>Note: Spring Security's own {@code AccessDeniedException} (caught by
 * {@code GlobalExceptionHandler}) handles most authorization failures automatically.
 * This exception is for manual authorization checks in service methods where
 * Spring Security method security is insufficient.
 *
 * <p>Usage:
 * <pre>{@code
 *   if (!job.getCreatedBy().equals(currentUser.getUsername())) {
 *       throw new UnauthorizedAccessException(
 *           "You are not authorized to update this job posting."
 *       );
 *   }
 * }</pre>
 */
public class UnauthorizedAccessException extends AtsBaseException {

    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
