package com.example.ats.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all ATS application-level exceptions.
 *
 * <p>Extends {@link RuntimeException} (unchecked) so that service methods
 * don't need to declare {@code throws} clauses. The {@code GlobalExceptionHandler}
 * catches all subclasses and maps them to appropriate HTTP responses.
 *
 * <p>Each exception carries an {@link HttpStatus} which the handler uses to
 * set the response status code.
 */
@Getter
public abstract class AtsBaseException extends RuntimeException {

    private final HttpStatus status;

    protected AtsBaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected AtsBaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
