package com.nxp.iemdm.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeXmlAdapter extends XmlAdapter<String, LocalDateTime> {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault());

  @Override
  public LocalDateTime unmarshal(String localDateTimeString) throws Exception {
    return LocalDateTime.parse(localDateTimeString, DATE_TIME_FORMATTER);
  }

  @Override
  public String marshal(LocalDateTime localDateTime) throws Exception {
    return localDateTime.format(DATE_TIME_FORMATTER);
  }
}
