package com.nxp.iemdm.exception;

public class MassUploadInvalidExcelSheetException extends RuntimeException {
  public MassUploadInvalidExcelSheetException(String errorMessage) {
    super(errorMessage);
  }

  public MassUploadInvalidExcelSheetException(String errorMessage, Throwable error) {
    super(errorMessage, error);
  }
}
