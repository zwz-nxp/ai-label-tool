package com.nxp.iemdm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidParameterException extends RuntimeException {
  public InvalidParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidParameterException(String message) {
    super(message);
  }
}
