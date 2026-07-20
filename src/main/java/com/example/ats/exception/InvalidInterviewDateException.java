package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid interview date is specified (e.g. date is not in the future).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidInterviewDateException extends AtsBaseException {

    public InvalidInterviewDateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
