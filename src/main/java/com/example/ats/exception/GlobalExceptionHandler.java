package com.example.ats.exception;

import com.example.ats.dto.response.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized Exception Handler for all REST controllers.
 * Intercepts application exceptions and converts them into standardized JSON error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    //  Custom Application / Business Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles all subclasses of {@link AtsBaseException} (e.g. ResourceNotFoundException,
     * DuplicateResourceException, InvalidStatusTransitionException, BusinessException).
     */
    @ExceptionHandler(AtsBaseException.class)
    public ResponseEntity<ApiErrorResponse> handleAtsBaseException(
            AtsBaseException ex, HttpServletRequest request) {

        log.warn("ATS application exception [{}] at {}: {}",
                ex.getStatus(), request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /**
     * Handles JPA {@link EntityNotFoundException}.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        log.warn("Entity not found at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles Spring Security {@link UsernameNotFoundException}.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {

        log.warn("User not found at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Spring MVC / Validation Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles DTO validation failures from {@code @Valid} body annotations.
     * Maps to HTTP 400 Bad Request with field-level errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> {
                    FieldError fieldError = (FieldError) error;
                    String rejectedValue = fieldError.getRejectedValue() != null
                            ? fieldError.getRejectedValue().toString()
                            : null;
                    return new ApiErrorResponse.FieldError(
                            fieldError.getField(),
                            rejectedValue,
                            fieldError.getDefaultMessage()
                    );
                })
                .toList();

        log.debug("Validation failed at {}: {} field error(s)", request.getRequestURI(), fieldErrors.size());

        ApiErrorResponse error = ApiErrorResponse.validationError(
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles parameter/constraint validation failures from {@link ConstraintViolationException}.
     * Maps to HTTP 400 Bad Request with field-level errors.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation ->
                fieldErrors.add(new ApiErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
        );

        log.debug("Constraint violation at {}: {} error(s)", request.getRequestURI(), fieldErrors.size());

        ApiErrorResponse error = ApiErrorResponse.validationError(
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles malformed or unparseable JSON payloads.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON request body at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request body or unparseable payload format.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles invalid argument exceptions.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles 404 for non-existent static endpoint paths.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "The requested URL was not found: " + request.getRequestURI(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles file size exceeded errors from Spring's multipart resolver.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("File upload size exceeded at {}", request.getRequestURI());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Uploaded file exceeds the maximum allowed size of 5MB.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Spring Security Exceptions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles authorization failures (authenticated user lacks required permission).
     * Maps to HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles bad credentials during authentication.
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Bad credentials at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles general authentication failures (missing or invalid token).
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication failure at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Fallback Handler
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Catches all unhandled exceptions as a last resort.
     * Maps to HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later or contact support.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
