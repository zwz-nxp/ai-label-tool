package com.nxp.iemdm.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateXmlAdapter extends XmlAdapter<String, LocalDate> {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE;

  @Override
  public LocalDate unmarshal(String localDateString) throws Exception {
    return LocalDate.parse(localDateString, DATE_TIME_FORMATTER);
  }

  @Override
  public String marshal(LocalDate localDate) throws Exception {
    return localDate.format(DATE_TIME_FORMATTER);
  }
}
