package com.nxp.iemdm.services.spring.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class HttpStatusCodeExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(value = {HttpStatusCodeException.class})
  protected ResponseEntity<String> handleError(HttpStatusCodeException exception) {
    return new ResponseEntity<>(exception.getResponseBodyAsString(), exception.getStatusCode());
  }
}
