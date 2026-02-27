package com.nxp.iemdm.services.spring.rest.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ConstraintViolationExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(
      value = {
        ConstraintViolationException.class,
      })
  protected ResponseEntity<String> handleError(RuntimeException exception) {
    return new ResponseEntity<>(
        ((ConstraintViolationException) exception)
            .getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("\n")),
        HttpStatus.BAD_REQUEST);
  }
}
