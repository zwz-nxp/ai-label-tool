package com.nxp.iemdm.shared.utility;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.java.Log;

@Log
public class ResultSetHelper {

  private ResultSetHelper() {}

  public static LocalDate parseLocalDate(Object value) {
    if (value == null) return null;

    if (value instanceof Timestamp) {
      return LocalDateTime.ofInstant(parseInstant(value), ZoneOffset.UTC).toLocalDate();
    }

    return (LocalDate) handleUnknown("parseLocalDate", value);
  }

  public static Instant parseInstant(Object value) {
    if (value == null) return null;

    if (value instanceof Timestamp timestamp) {
      return timestamp.toInstant();
    } else if (value instanceof Instant instant) {
      return instant;
    }

    return (Instant) handleUnknown("parseLocalDate", value);
  }

  public static Integer parseInteger(Object object) {
    if (object == null) {
      return null;
    } else {
      Double value = (Double) object;
      return value.intValue();
    }
  }

  public static String parseString(Object object) {
    if (object == null) {
      return null;
    } else {
      return object.toString();
    }
  }

  private static Object handleUnknown(String methodName, Object value) {
    log.warning(methodName + " can not return " + value.getClass().getName());
    return null;
  }
}
