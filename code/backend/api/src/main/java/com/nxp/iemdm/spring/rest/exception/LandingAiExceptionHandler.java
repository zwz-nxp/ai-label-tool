package com.nxp.iemdm.spring.rest.exception;

import com.nxp.iemdm.exception.DuplicateResourceException;
import com.nxp.iemdm.exception.ResourceNotFoundException;
import com.nxp.iemdm.exception.TrainingException;
import com.nxp.iemdm.exception.ValidationException;
import com.nxp.iemdm.shared.dto.landingai.ErrorResponse;
import com.nxp.iemdm.shared.exception.landingai.InsufficientDataException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the LandingAI API endpoints. Provides standardized error responses
 * for all exceptions.
 *
 * <p>Requirement 22.6: IF validation fails, THEN THE System SHALL return a 400 Bad Request with
 * error details
 */
@RestControllerAdvice(basePackages = "com.nxp.iemdm.controller.landingai")
public class LandingAiExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(LandingAiExceptionHandler.class);

  /** Handle validation exceptions from @Valid annotations */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

    logger.warn("Validation failed for request {}: {}", request.getRequestURI(), errors);

    ErrorResponse response =
        ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("参数验证失败")
            .details(errors)
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle custom validation exceptions */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      ValidationException ex, HttpServletRequest request) {

    logger.warn("Validation error for request {}: {}", request.getRequestURI(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle resource not found exceptions */
  @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(
      RuntimeException ex, HttpServletRequest request) {

    logger.warn("Resource not found for request {}: {}", request.getRequestURI(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("NOT_FOUND")
            .message(ex.getMessage())
            .status(HttpStatus.NOT_FOUND.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /** Handle duplicate resource exceptions */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex, HttpServletRequest request) {

    logger.warn("Duplicate resource for request {}: {}", request.getRequestURI(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("DUPLICATE_RESOURCE")
            .message(ex.getMessage())
            .status(HttpStatus.CONFLICT.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /** Handle training-specific exceptions */
  @ExceptionHandler(TrainingException.class)
  public ResponseEntity<ErrorResponse> handleTrainingException(
      TrainingException ex, HttpServletRequest request) {

    logger.error("Training error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code("TRAINING_ERROR")
            .message(ex.getMessage())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  /** Handle illegal argument exceptions */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {

    logger.warn("Invalid argument for request {}: {}", request.getRequestURI(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("INVALID_ARGUMENT")
            .message(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle insufficient data exceptions (e.g., not enough training data) */
  @ExceptionHandler(InsufficientDataException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientDataException(
      InsufficientDataException ex, HttpServletRequest request) {

    logger.warn("Insufficient data for request {}: {}", request.getRequestURI(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code("INSUFFICIENT_DATA")
            .message(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.badRequest().body(response);
  }

  /** Handle all other unhandled exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {

    logger.error(
        "Unexpected error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("服务器内部错误，请稍后重试")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .path(request.getRequestURI())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
