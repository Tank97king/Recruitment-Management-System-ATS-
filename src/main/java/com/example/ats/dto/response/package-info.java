/**
 * Response DTOs — Outgoing HTTP response body objects.
 *
 * <p>All classes in this package represent the data returned to API consumers.
 * Response DTOs decouple the API contract from the internal JPA entity model.
 *
 * <p>Key principle: Response DTOs contain ONLY the fields safe to expose.
 * For example, {@code UserResponse} never includes the hashed password field.
 *
 * <p>Response DTOs are produced by MapStruct mappers from entity objects
 * within the service layer.
 *
 * <p>Special shared response types in this package:
 * <ul>
 *   <li>{@link com.example.ats.dto.response.ApiResponse} — generic success wrapper</li>
 *   <li>{@link com.example.ats.dto.response.ApiErrorResponse} — structured error body</li>
 *   <li>{@link com.example.ats.dto.response.PageResponse} — paginated list wrapper</li>
 * </ul>
 *
 * <p>Naming convention: {@code JobResponse}, {@code UserResponse},
 * {@code ApplicationResponse}, {@code AuthResponse}
 */
package com.example.ats.dto.response;
