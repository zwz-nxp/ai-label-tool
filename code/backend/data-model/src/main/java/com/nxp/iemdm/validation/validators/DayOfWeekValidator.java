package com.nxp.iemdm.validation.validators;

import com.nxp.iemdm.validation.annotations.DayOfWeek;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DayOfWeekValidator implements ConstraintValidator<DayOfWeek, LocalDate> {
  private java.time.DayOfWeek dayOfWeek;

  @Override
  public void initialize(DayOfWeek constraintAnnotation) {
    this.dayOfWeek = constraintAnnotation.dayOfWeek();
  }

  @Override
  public boolean isValid(
      LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
    if (localDate != null) {
      return this.dayOfWeek == localDate.getDayOfWeek();
    } else {
      return true;
    }
  }
}
