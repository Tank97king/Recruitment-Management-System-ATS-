package com.example.ats.exception;

import java.util.UUID;

/**
 * Thrown when a Candidate entity is not found in the system.
 * Maps to HTTP 404 Not Found.
 */
public class CandidateNotFoundException extends ResourceNotFoundException {

    public CandidateNotFoundException(UUID id) {
        super("Candidate", "id", id);
    }

    public CandidateNotFoundException(String message) {
        super(message);
    }
}
