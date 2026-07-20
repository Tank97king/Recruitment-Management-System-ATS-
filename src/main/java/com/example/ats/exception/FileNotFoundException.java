package com.example.ats.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an expected file is not found on the local filesystem.
 * Maps to HTTP 404 Not Found.
 */
public class FileNotFoundException extends AtsBaseException {

    public FileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}
