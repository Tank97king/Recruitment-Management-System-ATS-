package com.example.ats.exception;

import java.util.UUID;

/**
 * Thrown when a Job entity is not found in the system.
 * Maps to HTTP 404 Not Found.
 */
public class JobNotFoundException extends ResourceNotFoundException {

    public JobNotFoundException(UUID id) {
        super("Job", "id", id);
    }

    public JobNotFoundException(String message) {
        super(message);
    }
}
