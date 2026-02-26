package com.nxp.iemdm.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for JSON validation. Validates that a string field contains valid JSON syntax.
 * Validates: Requirements 7.5, 8.1, 8.4
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonValidator.class)
@Documented
public @interface ValidJson {

  String message() default "Invalid JSON format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
