package com.nxp.iemdm.exception.landingai;

/**
 * Exception thrown when an error occurs during image processing operations such as thumbnail
 * generation, metadata extraction, or file storage.
 */
public class ImageProcessingException extends RuntimeException {

  public ImageProcessingException(String message) {
    super(message);
  }

  public ImageProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageProcessingException(String fileName, String operation, Throwable cause) {
    super(String.format("Failed to %s for image '%s'", operation, fileName), cause);
  }
}
