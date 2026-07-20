package com.example.ats.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.math.BigDecimal;

/**
 * Validator for the {@link ValidSalaryRange} annotation.
 *
 * <p>Reads the {@code minField} and {@code maxField} properties from the target
 * object via Spring's {@link BeanWrapperImpl}. Validates that:
 * <ul>
 *   <li>Both values are {@code null} (range is optional) — passes.</li>
 *   <li>Both values are &ge; 0 — passes.</li>
 *   <li>{@code salaryMin} &le; {@code salaryMax} — passes.</li>
 *   <li>Otherwise — fails.</li>
 * </ul>
 *
 * <p>On failure the default class-level constraint message is suppressed and a
 * field-level message is added against {@code maxField} so that the response
 * clearly identifies which field was rejected.
 */
public class SalaryRangeValidator implements ConstraintValidator<ValidSalaryRange, Object> {

    private String minField;
    private String maxField;
    private String message;

    @Override
    public void initialize(ValidSalaryRange annotation) {
        this.minField = annotation.minField();
        this.maxField = annotation.maxField();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object minObj = wrapper.getPropertyValue(minField);
        Object maxObj = wrapper.getPropertyValue(maxField);

        // If either value is absent, no cross-field rule to enforce
        if (minObj == null || maxObj == null) {
            return true;
        }

        BigDecimal min = new BigDecimal(minObj.toString());
        BigDecimal max = new BigDecimal(maxObj.toString());

        // Both must be non-negative
        if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(BigDecimal.ZERO) < 0) {
            buildConstraintViolation(context, "Salary values must not be negative", maxField);
            return false;
        }

        // Min must not exceed max
        if (min.compareTo(max) > 0) {
            buildConstraintViolation(context, message, maxField);
            return false;
        }

        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context,
                                          String msg, String fieldName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}
