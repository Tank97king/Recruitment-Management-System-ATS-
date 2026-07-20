package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a candidate attempts to apply for a job without an uploaded CV.
 * Maps to HTTP 400 Bad Request.
 */
public class CvRequiredException extends AtsBaseException {

    public CvRequiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
