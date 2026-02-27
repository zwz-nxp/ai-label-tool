package com.nxp.iemdm.exception;

public class MassUploadInvalidConfigurationException extends RuntimeException {
  public MassUploadInvalidConfigurationException(String errorMessage) {
    super(errorMessage);
  }

  public MassUploadInvalidConfigurationException(String errorMessage, Throwable error) {
    super(errorMessage, error);
  }
}
