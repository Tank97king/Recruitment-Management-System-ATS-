package com.example.ats.config;

import com.example.ats.config.ServerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI Configuration.
 *
 * <p>This bean configures the interactive API documentation served at:
 * <ul>
 *   <li>Swagger UI   : <a href="http://localhost:8080/swagger-ui/index.html">/swagger-ui/index.html</a></li>
 *   <li>OpenAPI JSON : <a href="http://localhost:8080/v3/api-docs">/v3/api-docs</a></li>
 * </ul>
 *
 * <p>A JWT Bearer token security scheme is registered globally so that
 * authenticated endpoints display the "Authorize" lock icon in the UI.
 * Testers can enter their JWT token once and it will be sent on all requests.
 *
 * <h2>How to Test APIs using Swagger UI</h2>
 * <ol>
 *   <li>Navigate to <strong>http://localhost:8080/swagger-ui/index.html</strong></li>
 *   <li>Call <strong>POST /api/auth/login</strong> with valid credentials to get a JWT token</li>
 *   <li>Copy the <code>accessToken</code> value from the response body</li>
 *   <li>Click the <strong>Authorize</strong> 🔒 button at the top of the page</li>
 *   <li>In the "bearerAuth" field, paste your JWT token (without "Bearer " prefix)</li>
 *   <li>Click "Authorize" then "Close"</li>
 *   <li>Now all protected endpoints will include the token automatically</li>
 * </ol>
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    /** The global name used to reference the Bearer auth security scheme. */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private final ServerProperties serverProperties;

    /**
     * Constructs and configures the main {@link OpenAPI} bean used by SpringDoc.
     *
     * @return fully configured OpenAPI specification
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // ─── API Metadata ──────────────────────────────────────────
                .info(buildApiInfo())
                // ─── Server List ───────────────────────────────────────────
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverProperties.getPort())
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.ats-system.com")
                                .description("Production Server (placeholder)")
                ))
                // ─── Security Scheme (JWT Bearer) ──────────────────────────
                // This registers the "bearerAuth" scheme so Swagger UI shows
                // the Authorize button with a Bearer token input field.
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "**JWT Bearer Authentication**\n\n" +
                                                "1. Call `POST /api/auth/login` with valid credentials\n" +
                                                "2. Copy the `accessToken` from the response\n" +
                                                "3. Click the **Authorize** button and paste the token\n\n" +
                                                "Format: `Bearer {your_jwt_token}` (the 'Bearer ' prefix is added automatically)"
                                        )
                        )
                )
                // ─── Global Security Requirement ───────────────────────────
                // Applies the JWT security scheme to ALL endpoints by default.
                // Individual public endpoints opt out with @SecurityRequirements({}).
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // ─── Tag Ordering (defines sidebar order in Swagger UI) ────
                .tags(List.of(
                        new Tag().name("Authentication")
                                .description("User registration, login, and token refresh endpoints. No JWT required."),
                        new Tag().name("Health Check")
                                .description("Application health and status verification endpoint. No JWT required."),
                        new Tag().name("User Management")
                                .description("Manage system users, view profiles, and administer accounts. Admin only."),
                        new Tag().name("Company Management")
                                .description("Create, retrieve, update, and delete company profiles."),
                        new Tag().name("Job Management")
                                .description("Post, search, update, and manage job postings."),
                        new Tag().name("Candidate Management")
                                .description("Manage candidate profiles including CV upload/download."),
                        new Tag().name("Job Application Management")
                                .description("Submit and manage job applications throughout the hiring workflow."),
                        new Tag().name("Interview Management")
                                .description("Schedule, update, and track interview sessions."),
                        new Tag().name("Recruitment Pipeline Management")
                                .description("Kanban board view and stage movement for the recruitment pipeline."),
                        new Tag().name("Dashboard & Statistics")
                                .description("Aggregate analytics, metrics, and summary statistics for the dashboard."),
                        new Tag().name("Audit Log Management")
                                .description("Query system audit trails. Admin only."),
                        new Tag().name("Global Search")
                                .description("Cross-entity search across Companies, Jobs, and Candidates.")
                ));
    }

    /**
     * Builds the {@link Info} section of the OpenAPI specification.
     *
     * @return configured API info block
     */
    private Info buildApiInfo() {
        return new Info()
                .title("ATS System API — Recruitment Management System")
                .description(
                        "## Recruitment Management System (ATS)\n\n" +
                        "A full-featured RESTful backend API for managing the complete lifecycle of " +
                        "a recruitment process — from job posting creation to candidate hiring.\n\n" +

                        "---\n\n" +

                        "### 🚀 Key Features\n" +
                        "- **JWT Authentication** — stateless, token-based auth with refresh token rotation\n" +
                        "- **Role-Based Access Control** — `ADMIN` and `RECRUITER` roles\n" +
                        "- **Company Management** — create and manage company profiles\n" +
                        "- **Job Postings** — full CRUD with filtering by location, salary, type, and experience\n" +
                        "- **Candidate Profiles** — rich profiles with CV (PDF) upload/download\n" +
                        "- **Job Applications** — submit and track applications through stage workflow\n" +
                        "- **Interviews** — schedule, update, and manage interview sessions\n" +
                        "- **Recruitment Pipeline** — Kanban-style board with stage transitions\n" +
                        "- **Dashboard Analytics** — entity counts and statistics\n" +
                        "- **Audit Logs** — full system action trail\n" +
                        "- **Global Search** — search across Companies, Jobs, and Candidates\n\n" +

                        "---\n\n" +

                        "### 🔐 Authentication Guide\n" +
                        "Most endpoints require a JWT Bearer token:\n" +
                        "1. Call **`POST /api/auth/login`** with your email and password\n" +
                        "2. Copy the `accessToken` from the response\n" +
                        "3. Click the **Authorize** 🔒 button at the top of this page\n" +
                        "4. Paste your token into the `bearerAuth` field and click **Authorize**\n" +
                        "5. All subsequent requests will include the token automatically\n\n" +

                        "---\n\n" +

                        "### 📋 Standard Response Formats\n\n" +
                        "**Success Response:**\n" +
                        "```json\n" +
                        "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Operation completed successfully\",\n" +
                        "  \"timestamp\": \"2026-07-19T10:00:00Z\",\n" +
                        "  \"data\": { ... }\n" +
                        "}\n" +
                        "```\n\n" +
                        "**Validation Error Response (400):**\n" +
                        "```json\n" +
                        "{\n" +
                        "  \"success\": false,\n" +
                        "  \"status\": 400,\n" +
                        "  \"message\": \"Validation failed\",\n" +
                        "  \"errors\": [\n" +
                        "    { \"field\": \"email\", \"message\": \"Email must be valid\" }\n" +
                        "  ],\n" +
                        "  \"timestamp\": \"2026-07-19T10:00:00Z\",\n" +
                        "  \"path\": \"/api/auth/login\"\n" +
                        "}\n" +
                        "```\n\n" +
                        "**Unauthorized Response (401):**\n" +
                        "```json\n" +
                        "{\n" +
                        "  \"success\": false,\n" +
                        "  \"status\": 401,\n" +
                        "  \"message\": \"Full authentication is required to access this resource\",\n" +
                        "  \"timestamp\": \"2026-07-19T10:00:00Z\",\n" +
                        "  \"path\": \"/api/jobs\"\n" +
                        "}\n" +
                        "```"
                )
                .version("1.0.0")
                .contact(new Contact()
                        .name("ATS Development Team")
                        .email("dev@ats-system.com")
                        .url("https://github.com/yourusername/ats-system")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
}
