package com.nxp.iemdm.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Validator for model type validation. Validates: Requirements 7.3, 7.4
 *
 * <p>Ensures that model type is one of the allowed values: Object Detection, Classification,
 * Segmentation
 */
public class ModelTypeValidator implements ConstraintValidator<ValidModelType, String> {

  private static final List<String> VALID_MODEL_TYPES =
      Arrays.asList("Object Detection", "Classification", "Segmentation");

  @Override
  public void initialize(ValidModelType constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return true; // null/empty values are handled by @NotBlank
    }

    if (!VALID_MODEL_TYPES.contains(value)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Model type must be one of: %s. Received: %s",
                  String.join(", ", VALID_MODEL_TYPES), value))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
