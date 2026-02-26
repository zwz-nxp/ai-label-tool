package com.nxp.iemdm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionUtils.class);

  private ExceptionUtils() {}

  public static void logAndRethrowAsBadRequest(Exception exception, String message)
      throws BadRequestException {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    String extendedMessage = String.format("%s%s", prefixMessage(stackTraceElements), message);
    LOG.error(extendedMessage, exception);
    throw new BadRequestException(message);
  }

  private static String prefixMessage(StackTraceElement[] stackTraceElements) {
    if (stackTraceElements.length < 2) {
      return "";
    }

    String className = stackTraceElements[2].getClassName();
    String methodName = stackTraceElements[2].getMethodName();
    Integer lineNumber = stackTraceElements[2].getLineNumber();
    return String.format("%s - %s (%d) : ", className, methodName, lineNumber);
  }
}
