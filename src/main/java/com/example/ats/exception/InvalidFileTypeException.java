package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an uploaded file's type is invalid (e.g. not a PDF).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidFileTypeException extends AtsBaseException {

    public InvalidFileTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
