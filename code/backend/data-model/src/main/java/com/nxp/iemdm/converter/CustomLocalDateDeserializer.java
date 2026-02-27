package com.nxp.iemdm.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {

  @Serial private static final long serialVersionUID = 1L;

  public CustomLocalDateDeserializer() {
    this(null);
  }

  public CustomLocalDateDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public LocalDate deserialize(JsonParser jsonparser, DeserializationContext context)
      throws IOException {
    String stringDate = jsonparser.getText();
    try {
      return LocalDate.parse(stringDate.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeException e) {
      throw new IOException(e);
    }
  }
}
