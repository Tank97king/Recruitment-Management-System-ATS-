package com.example.ats.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to verify that two fields in a class match.
 *
 * <p>Typically used for password confirmation validation (e.g., verifying
 * that "password" and "confirmPassword" fields contain identical values).
 *
 * <p>Can be applied at the CLASS level only, as it needs access to multiple
 * fields within the target object.
 */
@Documented
@Constraint(validatedBy = FieldsMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsMatch {

    /**
     * Error message displayed if validation fails.
     * Default: "Fields do not match".
     */
    String message() default "Fields do not match";

    /** Used to group constraints for validation. */
    Class<?>[] groups() default {};

    /** Can be used to attach custom metadata to constraints. */
    Class<? extends Payload>[] payload() default {};

    /**
     * The first field to compare (e.g., "password").
     */
    String field();

    /**
     * The second field to compare (e.g., "confirmPassword").
     */
    String fieldMatch();

    /**
     * Defines several {@code @FieldsMatch} annotations on the same element.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FieldsMatch[] value();
    }
}
