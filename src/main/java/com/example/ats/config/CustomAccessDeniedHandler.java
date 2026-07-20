package com.example.ats.config;

import com.example.ats.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Access Denied Handler.
 *
 * <p>Invoked when an authenticated user attempts to access a protected URL
 * for which they do not possess the required authorities (e.g. Recruiter trying
 * to access Admin-only management endpoints).
 *
 * <p>Converts the error into our standardized {@link ApiErrorResponse} JSON format.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("Access denied for user at: {}. Error: {}", 
                request.getRequestURI(), accessDeniedException.getMessage());

        // Construct standard error response DTO (403 Forbidden)
        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request.getRequestURI()
        );

        // Serialize DTO to response output stream
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
