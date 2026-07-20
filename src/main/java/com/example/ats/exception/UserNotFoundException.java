package com.example.ats.exception;

import java.util.UUID;

/**
 * Thrown when a User entity is not found in the system.
 * Maps to HTTP 404 Not Found.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID id) {
        super("User", "id", id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
