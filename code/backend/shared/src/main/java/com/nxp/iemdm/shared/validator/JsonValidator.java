package com.nxp.iemdm.shared.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for JSON syntax validation. Validates: Requirements 7.5, 8.1, 8.4
 *
 * <p>Ensures that string fields contain valid JSON syntax that can be parsed.
 */
public class JsonValidator implements ConstraintValidator<ValidJson, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void initialize(ValidJson constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return true; // null/empty values are handled by @NotBlank
    }

    try {
      objectMapper.readTree(value);
      return true;
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format("Invalid JSON syntax: %s", e.getMessage()))
          .addConstraintViolation();
      return false;
    }
  }
}
