package com.nxp.iemdm.exception.landingai;

/**
 * Exception thrown when attempting to create a project with a name that already exists within the
 * same location.
 */
public class DuplicateProjectNameException extends RuntimeException {

  public DuplicateProjectNameException(String message) {
    super(message);
  }

  public DuplicateProjectNameException(String projectName, Long locationId) {
    super(
        String.format(
            "Project with name '%s' already exists in location %d", projectName, locationId));
  }
}
