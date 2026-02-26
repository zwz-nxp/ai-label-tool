package com.nxp.iemdm.shared.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileTypeConstraint {

  String message() default "File type must be JPG, PNG, or BMP";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] allowedTypes() default {"image/jpeg", "image/png", "image/bmp"};

  String[] allowedExtensions() default {".jpg", ".jpeg", ".png", ".bmp"};
}
