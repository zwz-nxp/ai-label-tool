package com.nxp.iemdm.shared.exception.landingai;

public class DuplicateSnapshotNameException extends RuntimeException {
  public DuplicateSnapshotNameException(String message) {
    super(message);
  }

  public DuplicateSnapshotNameException(String message, Throwable cause) {
    super(message, cause);
  }
}
