package com.nxp.iemdm.spring.rest.exception;

import com.nxp.iemdm.shared.dto.landingai.ErrorResponse;
import com.nxp.iemdm.shared.exception.landingai.ProjectCreationException;
import com.nxp.iemdm.shared.exception.landingai.SnapshotNotFoundException;
import com.nxp.iemdm.shared.exception.landingai.SnapshotRevertException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Centralized exception handler for snapshot-related operations. Handles custom exceptions and
 * returns structured error responses with appropriate HTTP status codes.
 */
@ControllerAdvice
public class SnapshotExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(SnapshotExceptionHandler.class);

  /**
   * Handle SnapshotNotFoundException. Returns 404 NOT FOUND status.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return error response with 404 status
   */
  @ExceptionHandler(SnapshotNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleSnapshotNotFoundException(
      SnapshotNotFoundException ex, HttpServletRequest request) {

    log.error("Snapshot not found: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse("SNAPSHOT_NOT_FOUND", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Handle ProjectCreationException. Returns 500 INTERNAL SERVER ERROR status.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return error response with 500 status
   */
  @ExceptionHandler(ProjectCreationException.class)
  public ResponseEntity<ErrorResponse> handleProjectCreationException(
      ProjectCreationException ex, HttpServletRequest request) {

    log.error("Project creation failed: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse("PROJECT_CREATION_FAILED", ex.getMessage(), request.getRequestURI());

    // Add cause details if available
    if (ex.getCause() != null) {
      errorResponse.addDetail("cause", ex.getCause().getMessage());
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handle SnapshotRevertException. Returns 500 INTERNAL SERVER ERROR status.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return error response with 500 status
   */
  @ExceptionHandler(SnapshotRevertException.class)
  public ResponseEntity<ErrorResponse> handleSnapshotRevertException(
      SnapshotRevertException ex, HttpServletRequest request) {

    log.error("Snapshot revert failed: {}", ex.getMessage(), ex);

    ErrorResponse errorResponse =
        new ErrorResponse("SNAPSHOT_REVERT_FAILED", ex.getMessage(), request.getRequestURI());

    // Add cause details if available
    if (ex.getCause() != null) {
      errorResponse.addDetail("cause", ex.getCause().getMessage());
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
