package com.nxp.iemdm.capacitystatementservice.service.integration;

import com.nxp.iemdm.capacitystatementservice.service.PersonService;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Log
public class I2FileHandler implements MessageHandler {
  private final PersonService personService;
  private final String iemdmUserWbi;
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());

  public I2FileHandler(
      PersonService personService, @Value("${iemdm.user.wbi}") String iemdmUserWbi) {
    this.personService = personService;
    this.iemdmUserWbi = iemdmUserWbi;
  }

  @Override
  public void handleMessage(Message<?> message) throws MessagingException {
    if (!(message instanceof File)) {
      throw new MessagingException("Message is not a file");
    }
    File content = (File) message.getPayload();

    String fileName = content.getName();
    String[] filenameParts = fileName.split("_");

    String timestampString = filenameParts[3];

    Instant timestampInstant = DATE_TIME_FORMATTER.parse(timestampString, Instant::from);
  }
}
