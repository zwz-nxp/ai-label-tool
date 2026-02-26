package com.nxp.iemdm.shared.dto.landingai;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * DTO for job processing results. Contains statistics about successful and failed processing
 * attempts.
 */
@Data
public class ProcessingResult {

  /** Total number of records processed */
  private int totalRecords;

  /** Number of successfully processed records */
  private int successCount;

  /** Number of failed records */
  private int failureCount;

  /** List of error messages */
  private List<String> errors;

  public ProcessingResult() {
    this.errors = new ArrayList<>();
  }

  public ProcessingResult(
      int totalRecords, int successCount, int failureCount, List<String> errors) {
    this.totalRecords = totalRecords;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.errors = errors != null ? errors : new ArrayList<>();
  }

  /** Add an error message to the result */
  public void addError(String error) {
    this.errors.add(error);
  }

  /** Increment success count */
  public void incrementSuccess() {
    this.successCount++;
  }

  /** Increment failure count */
  public void incrementFailure() {
    this.failureCount++;
  }
}
