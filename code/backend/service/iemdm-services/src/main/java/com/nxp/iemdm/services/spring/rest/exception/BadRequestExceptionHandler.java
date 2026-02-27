package com.nxp.iemdm.services.spring.rest.exception;

import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.exception.MassUploadInvalidConfigurationException;
import com.nxp.iemdm.exception.MassUploadInvalidExcelFileException;
import com.nxp.iemdm.exception.MassUploadInvalidExcelSheetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class BadRequestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(
      value = {
        BadRequestException.class,
        MassUploadInvalidExcelFileException.class,
        MassUploadInvalidExcelSheetException.class,
        MassUploadInvalidConfigurationException.class
      })
  protected ResponseEntity<String> handleError(RuntimeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
