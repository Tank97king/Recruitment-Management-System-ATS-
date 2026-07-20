package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid salary is specified (e.g. negative or min > max).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidSalaryException extends AtsBaseException {

    public InvalidSalaryException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
