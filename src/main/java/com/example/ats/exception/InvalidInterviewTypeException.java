package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid interview type is provided or required meeting details are missing for the type.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidInterviewTypeException extends AtsBaseException {

    public InvalidInterviewTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
