/**
 * Validation — Custom Jakarta Validation constraints and validators.
 *
 * <p>Contains reusable custom validation annotations and their corresponding
 * {@code ConstraintValidator} implementations.
 *
 * <p>Examples of custom validators planned for future phases:
 * <ul>
 *   <li>{@code @ValidPasswordStrength} — ensures password contains uppercase,
 *       digit, and minimum 8 characters</li>
 *   <li>{@code @ValidFileExtension} — ensures uploaded file has an allowed extension</li>
 *   <li>{@code @FutureDateOrNull} — allows null but validates future date when present</li>
 * </ul>
 *
 * <p>Custom validators are applied as annotations on request DTO fields and
 * are triggered automatically by {@code @Valid} in controllers.
 */
package com.example.ats.validation;
