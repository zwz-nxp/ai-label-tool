package com.nxp.iemdm.shared.exception.landingai;

/** Exception thrown when a snapshot revert operation fails. */
public class SnapshotRevertException extends RuntimeException {

  public SnapshotRevertException(String message) {
    super(message);
  }

  public SnapshotRevertException(String message, Throwable cause) {
    super(message, cause);
  }
}
