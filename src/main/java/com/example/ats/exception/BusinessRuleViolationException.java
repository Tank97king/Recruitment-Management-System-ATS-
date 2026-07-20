package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation violates a business rule.
 *
 * <p>Maps to HTTP 422 Unprocessable Entity — the request is syntactically valid
 * (passes validation) but cannot be processed due to a semantic constraint.
 *
 * <p>Usage examples:
 * <pre>{@code
 *   // Cannot submit an application to a CLOSED job
 *   if (job.getStatus() != JobStatus.PUBLISHED) {
 *       throw new BusinessRuleViolationException(
 *           "Cannot submit application: job is not PUBLISHED. Current status: " + job.getStatus()
 *       );
 *   }
 *
 *   // Cannot advance an application past HIRED
 *   if (application.getPipelineStage() == PipelineStage.HIRED) {
 *       throw new BusinessRuleViolationException("Application is already in the final stage.");
 *   }
 * }</pre>
 */
public class BusinessRuleViolationException extends AtsBaseException {

    public BusinessRuleViolationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
