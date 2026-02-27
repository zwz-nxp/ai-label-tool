package com.nxp.iemdm.handler;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.pojo.Update;
import java.lang.reflect.Type;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

@Log
public final class UpdateHandler extends StompSessionHandlerAdapter {
  private static volatile UpdateHandler instance;
  private final SimpMessagingTemplate simpMessagingTemplate;

  private UpdateHandler(SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  public static UpdateHandler getInstance(SimpMessagingTemplate simpMessagingTemplate) {
    if (instance == null) {
      synchronized (UpdateHandler.class) {
        if (instance == null) {
          instance = new UpdateHandler(simpMessagingTemplate);
        }
      }
    }
    return instance;
  }

  private void logMessageIfNeeded(Object payload) {
    if (payload instanceof Update update) {
      if (UpdateType.IEMDM_ALERT.equals(update.getUpdatedType())) {
        log.info("Sending IE-MDM Alert " + update.getUpdateData().toString());
      }
    }
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    return Update.class;
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    this.logMessageIfNeeded(payload);
    this.simpMessagingTemplate.convertAndSend(Update.ADDRESS, payload);
  }

  @Override
  public void handleException(
      StompSession session,
      StompCommand command,
      StompHeaders headers,
      byte[] payload,
      Throwable throwable) {
    String message =
        String.format(
            "Websocket messaging failed for session %s to %s",
            session.getSessionId(), headers.getDestination());
    log.log(Level.SEVERE, message, throwable);
  }
}
