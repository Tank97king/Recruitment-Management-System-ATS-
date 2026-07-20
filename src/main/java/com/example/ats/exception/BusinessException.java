package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for domain and business logic rule violations.
 * Maps to HTTP 400 Bad Request by default.
 */
public class BusinessException extends AtsBaseException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message, status);
    }
}
