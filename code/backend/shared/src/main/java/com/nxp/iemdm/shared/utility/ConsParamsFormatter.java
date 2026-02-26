package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.model.consumption.ConsumptionConstants;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/** Helper class with common formatting utilities */
public class ConsParamsFormatter {
  private ConsParamsFormatter() {}

  public static String parseValue(Date value) {
    return value == null ? "" : ConsumptionConstants.DATE_FORMAT.format(value);
  }

  public static String parseValue(LocalDate value) {
    return value == null ? "" : ConsumptionConstants.DATE_TIME_FORMATTER.format(value);
  }

  public static String parseValue(Instant value) {
    return value == null ? "" : ConsumptionConstants.DATE_TIME_FORMATTER.format(value);
  }

  public static String parseValue(String value) {
    return value == null ? "" : value;
  }

  public static String parseValue(Float value) {
    if (value == null) {
      return "";
    }
    return new DecimalFormat("###0.00000").format(RoundUtil.round(value, 5));
  }

  public static String parseValue(Double value) {
    if (value == null) {
      return "";
    }
    return new DecimalFormat("###0.00000").format(RoundUtil.round(value, 5));
  }

  public static String parseValue(Integer value) {
    return value == null ? "" : value.toString();
  }

  public static String parseValue(Boolean value) {
    return value == null ? "Y" : "N";
  }

  public static String parseValue(boolean value) {
    return value ? "Y" : "N";
  }

  public static String parseValue(BigDecimal value) {
    return value == null ? "" : value.toPlainString();
  }

  public static Integer parsePk(BigDecimal value) {
    if (value == null) {
      return null;
    } else {
      return Integer.valueOf(value.intValue());
    }
  }

  public static LocalDate parseLocalDate(Timestamp value) {
    if (value == null) {
      return null;
    } else {
      return LocalDateTime.ofInstant(parseInstant(value), ZoneOffset.UTC).toLocalDate();
    }
  }

  public static LocalDateTime parseLocalDateTime(Timestamp value) {
    if (value == null) {
      return null;
    } else {
      return LocalDateTime.ofInstant(parseInstant(value), ZoneOffset.UTC);
    }
  }

  public static Instant parseInstant(Timestamp value) {
    if (value == null) {
      return null;
    } else {
      return value.toInstant();
    }
  }

  public static Integer parseInteger(BigDecimal value) {
    if (value == null) {
      return null;
    } else {
      return Integer.valueOf(value.intValue());
    }
  }
}
