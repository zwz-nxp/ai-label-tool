package com.nxp.iemdm.validation.validators;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.validation.annotations.NotGlobalLocation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotGlobalLocationValidator
    implements ConstraintValidator<NotGlobalLocation, Location> {
  @Override
  public void initialize(NotGlobalLocation constraintAnnotation) {
    // no-op
  }

  @Override
  public boolean isValid(Location location, ConstraintValidatorContext context) {
    return !location.getId().equals(0);
  }
}
