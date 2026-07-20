package com.example.ats.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for phone number format.
 *
 * <p>Accepts {@code null} or empty string (phone is optional).
 * When a value is provided it must match the international phone number pattern:
 * an optional {@code +} prefix followed by 7–20 digits, spaces, hyphens, or parentheses.
 *
 * <p>Applied at the FIELD level.
 *
 * <p>Example usage:
 * <pre>{@code
 * @ValidPhoneNumber
 * private String phone;
 * }</pre>
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    /** Validation failure message. */
    String message() default "Invalid phone number format. Expected format: +1234567890 or (123) 456-7890";

    /** Groups for constraint categorisation. */
    Class<?>[] groups() default {};

    /** Payload for constraint metadata. */
    Class<? extends Payload>[] payload() default {};
}
