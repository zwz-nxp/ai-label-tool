package com.nxp.iemdm.validation.annotations;

import com.nxp.iemdm.validation.validators.NotGlobalLocationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NotGlobalLocationValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotGlobalLocation {
  String message() default "Global location is not allowed";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
