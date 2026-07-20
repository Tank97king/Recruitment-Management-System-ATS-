package com.example.ats.controller;

import com.example.ats.config.ApplicationProperties;
import com.example.ats.config.InfoProperties;
import com.example.ats.config.ProfileProperties;
import com.example.ats.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller.
 *
 * <p>Provides a simple, publicly accessible endpoint to verify the application
 * is running correctly. This endpoint:
 * <ul>
 *   <li>Requires NO authentication (explicitly excluded from security)</li>
 *   <li>Returns the application name, version, environment, and server timestamp</li>
 *   <li>Is the first endpoint to test after starting the application</li>
 * </ul>
 *
 * <p>Accessible at: {@code GET /api/v1/health}
 *
 * <p>Note: For production health checks integrated with Docker/Kubernetes,
 * use the Spring Boot Actuator endpoint at {@code GET /actuator/health} instead.
 * This controller is an application-level status endpoint for demonstration purposes.
 */
@RestController
@RequestMapping(ApiConstants.API_V1)
@RequiredArgsConstructor
@Tag(
        name = "Health Check",
        description = "Application health and status verification endpoint. No JWT required."
)
public class HealthController {

    private final ApplicationProperties applicationProperties;
    private final InfoProperties infoProperties;
    private final ProfileProperties profileProperties;

    /**
     * Returns application health status.
     *
     * <p>This endpoint is publicly accessible (no JWT required).
     *
     * @return HTTP 200 with application status information
     */
    @GetMapping("/health")
    @SecurityRequirements  // Disables the global JWT requirement for this endpoint
    @Operation(
            summary = "Application Health Check",
            description = "Returns the current application status, version, environment, and server timestamp. " +
                          "No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Application is running normally",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = com.example.ats.dto.response.ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Health Check Success",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Application is healthy",
                                              "timestamp": "2026-07-19T10:00:00Z",
                                              "data": {
                                                "application": "recruitment-ats-system",
                                                "version": "1.0.0",
                                                "status": "UP",
                                                "environment": "dev",
                                                "serverTime": "2026-07-19T10:00:00Z",
                                                "message": "ATS System is running successfully! 🚀"
                                              }
                                            }"""
                            )
                    )
            )
    })
    public ResponseEntity<com.example.ats.dto.response.ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("application", applicationProperties.getName());
        healthData.put("version", "1.0.1-diagnostic");
        healthData.put("status", "UP");
        healthData.put("environment", profileProperties.getActive());
        healthData.put("serverTime", Instant.now().toString());
        healthData.put("message", "ATS System is running successfully! 🚀");

        return ResponseEntity.ok(
                com.example.ats.dto.response.ApiResponse.success("Application is healthy", healthData)
        );
    }
}
