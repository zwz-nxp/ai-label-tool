package com.nxp.iemdm.spring.rest.exception;

import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Specialization for exceptions handled by ResponseEntityExceptionHandler. To avoid ambiguous
 * methods with @ExceptionHandler annotations, specialized handling methods from the base class can
 * be overridden here. Other ControllerAdvice for exceptions already handled by
 * ResponseEntityExceptionHandler should not extend from the ResponseEntityExceptionHandler.
 */
@ControllerAdvice
public class ApiResponseEntityExceptionHandler {

  /**
   * Specialization for MethodArgumentNotValidException.
   *
   * @param ex the exception
   * @param request the current request
   * @return response with multiline string of violations.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, WebRequest request) {
    String message =
        ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(System.lineSeparator()));
    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
  }
}
