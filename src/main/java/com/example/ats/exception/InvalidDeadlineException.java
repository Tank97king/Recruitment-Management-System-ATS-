package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid deadline is specified (e.g. not in the future).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidDeadlineException extends AtsBaseException {

    public InvalidDeadlineException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
