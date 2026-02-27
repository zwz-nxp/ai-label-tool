package com.nxp.iemdm.spring.rest.exception;

import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.exception.InvalidParameterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BadRequestExceptionHandler {

  @ExceptionHandler(value = {BadRequestException.class, InvalidParameterException.class})
  protected ResponseEntity<String> handleError(RuntimeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
