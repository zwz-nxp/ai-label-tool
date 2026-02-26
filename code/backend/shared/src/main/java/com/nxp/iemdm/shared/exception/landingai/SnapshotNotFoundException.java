package com.nxp.iemdm.shared.exception.landingai;

/** Exception thrown when a snapshot is not found. */
public class SnapshotNotFoundException extends RuntimeException {

  public SnapshotNotFoundException(Long snapshotId) {
    super("Snapshot not found with ID: " + snapshotId);
  }

  public SnapshotNotFoundException(String message) {
    super(message);
  }

  public SnapshotNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
