package com.example.ats.exception;

import java.util.UUID;

/**
 * Thrown when a Company entity is not found in the system.
 * Maps to HTTP 404 Not Found.
 */
public class CompanyNotFoundException extends ResourceNotFoundException {

    public CompanyNotFoundException(UUID id) {
        super("Company", "id", id);
    }

    public CompanyNotFoundException(String message) {
        super(message);
    }
}
