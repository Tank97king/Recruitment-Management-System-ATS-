package com.example.ats.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link ValidPhoneNumber} annotation.
 *
 * <p>Accepts null or blank values (phone is optional). When a value is provided,
 * it must match the pattern: optional {@code +}, followed by 7–20 characters
 * consisting of digits, spaces, hyphens, or parentheses.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    /**
     * Regex: allow empty string or a valid phone pattern.
     * - Starts with an optional {@code +}
     * - Followed by 7 to 20 characters: digits, spaces, hyphens, parentheses
     */
    private static final String PHONE_REGEX = "^$|^\\+?[0-9\\s\\-()]{7,20}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // phone is optional
        }
        return value.matches(PHONE_REGEX);
    }
}
