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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Entry Point.
 *
 * <p>Invoked when an unauthenticated request attempts to access a protected URL.
 * Converts the exception into our standardized {@link ApiErrorResponse} JSON format.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Unauthorized access attempt to: {}. Error: {}", 
                request.getRequestURI(), authException.getMessage());

        // Determine specific error message based on attributes set by JwtAuthenticationFilter
        String message = (String) request.getAttribute("jwt_exception");
        if (message == null || message.isBlank()) {
            message = "Authentication required. Please provide a valid Bearer token.";
        }

        // Construct standard error response DTO
        ApiErrorResponse error = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                message,
                request.getRequestURI()
        );

        // Serialize DTO to response output stream
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
