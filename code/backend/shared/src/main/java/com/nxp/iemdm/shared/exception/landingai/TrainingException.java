package com.nxp.iemdm.shared.exception.landingai;

public class TrainingException extends RuntimeException {
  public TrainingException(String message) {
    super(message);
  }

  public TrainingException(String message, Throwable cause) {
    super(message, cause);
  }
}
