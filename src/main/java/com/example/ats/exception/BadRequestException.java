package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request contains invalid arguments or malformed data.
 * Maps to HTTP 400 Bad Request.
 */
public class BadRequestException extends AtsBaseException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
