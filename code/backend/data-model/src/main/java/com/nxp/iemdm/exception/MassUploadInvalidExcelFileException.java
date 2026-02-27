package com.nxp.iemdm.exception;

public class MassUploadInvalidExcelFileException extends RuntimeException {
  public MassUploadInvalidExcelFileException(String errorMessage) {
    super(errorMessage);
  }

  public MassUploadInvalidExcelFileException(String errorMessage, Throwable error) {
    super(errorMessage, error);
  }
}
