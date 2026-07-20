package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid transition between job application statuses is attempted.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidStatusTransitionException extends AtsBaseException {

    public InvalidStatusTransitionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
