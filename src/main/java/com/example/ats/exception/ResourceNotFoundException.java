package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (entity) does not exist in the database.
 *
 * <p>Maps to HTTP 404 Not Found.
 *
 * <p>Usage examples:
 * <pre>{@code
 *   // In a service method:
 *   Job job = jobRepository.findById(jobId)
 *       .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
 * }</pre>
 */
public class ResourceNotFoundException extends AtsBaseException {

    /**
     * Creates a ResourceNotFoundException with a descriptive message.
     *
     * @param resourceName the name of the resource type (e.g., "Job", "User")
     * @param fieldName    the field used to look up the resource (e.g., "id", "email")
     * @param fieldValue   the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
