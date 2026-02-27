package com.nxp.iemdm.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class InstantXmlAdapter extends XmlAdapter<String, Instant> {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

  @Override
  public Instant unmarshal(String instantString) {
    LocalDateTime parsed = LocalDateTime.parse(instantString, DATE_TIME_FORMATTER);
    return Instant.from(parsed.atZone(ZoneId.systemDefault()).toInstant());
  }

  @Override
  public String marshal(Instant instant) {
    return instant.atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
  }
}
