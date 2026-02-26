package com.nxp.iemdm.exception.landingai;

/**
 * Exception thrown when attempting to create a project with an invalid or unsupported type.
 * Currently, only "Object Detection" is supported.
 */
public class InvalidProjectTypeException extends RuntimeException {

  public InvalidProjectTypeException(String projectType) {
    super(
        String.format(
            "Invalid project type '%s'. Only 'Object Detection' projects are currently supported.",
            projectType));
  }

  public InvalidProjectTypeException(String projectType, Throwable cause) {
    super(
        String.format(
            "Invalid project type '%s'. Only 'Object Detection' projects are currently supported.",
            projectType),
        cause);
  }
}
