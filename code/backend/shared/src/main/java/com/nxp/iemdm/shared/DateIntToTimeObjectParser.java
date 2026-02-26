package com.nxp.iemdm.shared;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DateIntToTimeObjectParser {
  private static final Logger LOG = LoggerFactory.getLogger(DateIntToTimeObjectParser.class);

  public ZonedDateTime parseWeekIntToZonedDateTime(int value) {
    LocalDate localDate = parseWeekIntToLocalDate(value);

    return localDate.atStartOfDay(ZoneOffset.UTC);
  }

  public LocalDate parseWeekIntToLocalDate(int value) {
    int day = value % 100;
    int month = (value % 10000) / 100;
    int year = value / 10000;

    if (month > 0 && month < 13) {
      return LocalDate.of(year, month, day);
    } else {
      LOG.error("error parsing local date from {}", value);
      return LocalDate.now();
    }
  }
}
