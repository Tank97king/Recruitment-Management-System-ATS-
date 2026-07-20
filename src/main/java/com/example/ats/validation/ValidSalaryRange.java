package com.example.ats.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom class-level validation annotation that enforces salary range consistency.
 *
 * <p>When both {@code salaryMin} and {@code salaryMax} are provided:
 * <ul>
 *   <li>{@code salaryMin} must be &ge; 0</li>
 *   <li>{@code salaryMax} must be &ge; 0</li>
 *   <li>{@code salaryMin} must be &le; {@code salaryMax}</li>
 * </ul>
 *
 * <p>Applied at the CLASS level because it must access two fields simultaneously.
 * The fields being compared are configurable through the annotation attributes
 * ({@code minField} and {@code maxField}).
 *
 * <p>Example usage:
 * <pre>{@code
 * @ValidSalaryRange(minField = "salaryMin", maxField = "salaryMax")
 * public class CreateJobRequest { ... }
 * }</pre>
 */
@Documented
@Constraint(validatedBy = SalaryRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSalaryRange {

    /** Validation failure message. */
    String message() default "Salary min must not be greater than salary max";

    /** Groups for constraint categorisation. */
    Class<?>[] groups() default {};

    /** Payload for constraint metadata. */
    Class<? extends Payload>[] payload() default {};

    /** Name of the field holding the minimum salary value. */
    String minField() default "salaryMin";

    /** Name of the field holding the maximum salary value. */
    String maxField() default "salaryMax";

    /**
     * Defines several {@code @ValidSalaryRange} annotations on the same element.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidSalaryRange[] value();
    }
}
