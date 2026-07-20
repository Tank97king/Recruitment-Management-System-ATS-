package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create a resource that already exists,
 * violating a uniqueness constraint.
 *
 * <p>Maps to HTTP 409 Conflict.
 *
 * <p>Usage examples:
 * <pre>{@code
 *   if (userRepository.existsByEmail(request.getEmail())) {
 *       throw new DuplicateResourceException("User", "email", request.getEmail());
 *   }
 * }</pre>
 */
public class DuplicateResourceException extends AtsBaseException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
