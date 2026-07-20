/**
 * Utility classes — Stateless helper methods.
 *
 * <p>Contains pure utility classes with static methods for common operations
 * that don't belong to any specific layer.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code DateUtils} — date formatting, offset calculations</li>
 *   <li>{@code StringUtils} — slug generation, sanitization</li>
 *   <li>{@code FileUtils} — file extension extraction, MIME type detection</li>
 *   <li>{@code SecurityUtils} — getting the currently authenticated user from
 *       the Spring Security context</li>
 * </ul>
 *
 * <p><strong>Rule:</strong> Utility classes must be stateless (no instance variables).
 * All methods should be {@code public static} and independently testable.
 */
package com.example.ats.util;
