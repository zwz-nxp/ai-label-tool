package com.nxp.iemdm.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for model type validation. Validates that a string field contains one of the valid
 * model types. Validates: Requirements 7.3, 7.4
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ModelTypeValidator.class)
@Documented
public @interface ValidModelType {

  String message() default
      "Model type must be one of: Object Detection, Classification, Segmentation";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
