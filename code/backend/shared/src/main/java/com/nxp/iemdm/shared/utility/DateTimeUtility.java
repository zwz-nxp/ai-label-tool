package com.nxp.iemdm.shared.utility;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateTimeUtility {

  private DateTimeUtility() {}

  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

  public static String toIsoString(LocalDateTime localDateTime) {
    return localDateTime.format(dateTimeFormatter);
  }

  public static String toIsoString(LocalDate localDate) {
    return localDate.format(dateFormatter);
  }

  public static LocalDateTime fromIsoDateTimeString(String dateTimeString) {
    return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
  }

  public static String toIsoString(Date date) {
    return toIsoString(date.toInstant());
  }

  public static String toIsoString(Instant instant) {
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter);
  }

  public static LocalDate fromIsoDateString(String dateString) {
    return LocalDate.parse(dateString, dateFormatter);
  }

  public static LocalDate nextMonday() {
    return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
  }

  public static boolean isDateEqualOrBetween(
      @NotNull LocalDate input, @NotNull LocalDate minDate, @NotNull LocalDate maxDate) {
    return input.isAfter(minDate.minusDays(2))
        && input.isBefore(
            maxDate.plusDays(2)); // dirty trick to compensate for timezone shift frontend backend
  }

  public static boolean isDateBetween(
      @NotNull LocalDate input, @NotNull LocalDate minDate, @NotNull LocalDate maxDate) {
    return input.isAfter(minDate.minusDays(1))
        && input.isBefore(
            maxDate.plusDays(1)); // dirty trick to compensate for timezone shift frontend backend
  }

  public static LocalDateTime convertToAmsLocalDateForUTC(LocalDateTime localDateTimeIn) {
    Instant utcInstant = localDateTimeIn.toInstant(ZoneOffset.UTC);
    return LocalDateTime.ofInstant(utcInstant, ZoneId.systemDefault());
  }

  public static Instant convertLocalDateToInstant(LocalDate localDate) {
    if (localDate == null) {
      return null;
    }
    return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
  }
}
