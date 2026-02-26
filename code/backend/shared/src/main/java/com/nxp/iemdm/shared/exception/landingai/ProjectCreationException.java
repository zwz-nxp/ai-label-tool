package com.nxp.iemdm.shared.exception.landingai;

/** Exception thrown when project creation from a snapshot fails. */
public class ProjectCreationException extends RuntimeException {

  public ProjectCreationException(String message) {
    super(message);
  }

  public ProjectCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
