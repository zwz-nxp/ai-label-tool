package com.nxp.iemdm.exception;

/** Exception thrown when training operations fail */
public class TrainingException extends RuntimeException {

  public TrainingException(String message) {
    super(message);
  }

  public TrainingException(String message, Throwable cause) {
    super(message, cause);
  }
}
