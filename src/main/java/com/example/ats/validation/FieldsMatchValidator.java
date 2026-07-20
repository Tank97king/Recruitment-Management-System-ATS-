package com.example.ats.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator implementation for the {@link FieldsMatch} custom annotation.
 *
 * <p>Retrieves the values of both fields dynamically using Spring's
 * {@link BeanWrapperImpl} and compares them for equality.
 */
public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private String field;
    private String fieldMatch;

    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
            Object fieldMatchValue = new BeanWrapperImpl(value).getPropertyValue(fieldMatch);

            boolean isValid = (fieldValue == null && fieldMatchValue == null)
                    || (fieldValue != null && fieldValue.equals(fieldMatchValue));

            if (!isValid) {
                // Attach error message to the fieldMatch field itself, rather than the class root.
                // This makes it display nicely in frontend form field errors.
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                       .addPropertyNode(fieldMatch)
                       .addConstraintViolation();
            }

            return isValid;
        } catch (Exception e) {
            // Log/ignore exception and return false if properties cannot be accessed
            return false;
        }
    }
}
