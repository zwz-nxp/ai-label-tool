package com.nxp.iemdm.spring.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class RestTemplateExceptionHandler {

  @ExceptionHandler(value = {HttpClientErrorException.class})
  protected ResponseEntity<String> handleError(HttpClientErrorException exception) {
    return new ResponseEntity<>(exception.getResponseBodyAsString(), exception.getStatusCode());
  }

  @ExceptionHandler(value = {HttpServerErrorException.class})
  protected ResponseEntity<String> handleError(HttpServerErrorException exception) {
    return new ResponseEntity<>(exception.getResponseBodyAsString(), exception.getStatusCode());
  }
}
