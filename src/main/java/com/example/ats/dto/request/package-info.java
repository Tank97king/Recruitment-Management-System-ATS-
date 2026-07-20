/**
 * Request DTOs — Incoming HTTP request body objects.
 *
 * <p>All classes in this package represent the structure of data sent by
 * API consumers in HTTP request bodies (POST, PUT, PATCH).
 *
 * <p>Request DTOs:
 * <ul>
 *   <li>Are annotated with Jakarta Bean Validation constraints
 *       ({@code @NotBlank}, {@code @Email}, {@code @Size}, etc.)</li>
 *   <li>Are validated automatically by Spring MVC when decorated with
 *       {@code @Valid} in the controller method</li>
 *   <li>Are never used as JPA entities — they are decoupled from the database schema</li>
 * </ul>
 *
 * <p>Naming convention: {@code CreateJobRequest}, {@code UpdateUserRequest},
 * {@code LoginRequest}, {@code SubmitApplicationRequest}
 */
package com.example.ats.dto.request;
