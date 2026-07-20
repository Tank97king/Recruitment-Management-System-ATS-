/**
 * REST Controllers layer.
 *
 * <p>Contains all {@code @RestController} classes that handle incoming HTTP requests.
 * Controllers are responsible for:
 * <ul>
 *   <li>Parsing HTTP request parameters and request bodies</li>
 *   <li>Delegating all business logic to the Service layer</li>
 *   <li>Returning {@code ResponseEntity<ApiResponse<T>>} responses</li>
 *   <li>Swagger/OpenAPI annotations ({@code @Operation}, {@code @ApiResponse})</li>
 * </ul>
 *
 * <p><strong>Rule:</strong> Controllers must contain ZERO business logic.
 * They are thin HTTP adapters.
 */
package com.example.ats.controller;
