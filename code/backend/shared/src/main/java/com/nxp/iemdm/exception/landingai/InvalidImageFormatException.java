package com.nxp.iemdm.exception.landingai;

/**
 * Exception thrown when attempting to upload an image with an invalid or unsupported format. Only
 * PNG, JPG, and JPEG formats are supported.
 */
public class InvalidImageFormatException extends RuntimeException {

  public InvalidImageFormatException(String message) {
    super(message);
  }

  public InvalidImageFormatException(String fileName, String format) {
    super(
        String.format(
            "Invalid image format '%s' for file '%s'. Only PNG, JPG, and JPEG formats are supported.",
            format, fileName));
  }
}
