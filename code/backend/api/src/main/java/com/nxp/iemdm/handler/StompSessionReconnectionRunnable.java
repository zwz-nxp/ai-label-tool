package com.nxp.iemdm.handler;

import com.nxp.iemdm.spring.configuration.SpringConfiguration;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.session.SessionRegistry;

@Log
public class StompSessionReconnectionRunnable implements Runnable {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private final SessionRegistry sessionRegistry;
  private final String websocketUri;

  public StompSessionReconnectionRunnable(
      SimpMessagingTemplate simpMessagingTemplate,
      SessionRegistry sessionRegistry,
      String websocketUri) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.sessionRegistry = sessionRegistry;
    this.websocketUri = websocketUri;
  }

  @Override
  public void run() {
    log.log(
        Level.FINE,
        " Runnable Task with "
            + this.websocketUri
            + " in thread "
            + Thread.currentThread().getName());
    try {
      SpringConfiguration springConfiguration = new SpringConfiguration();
      springConfiguration.stompSession(
          this.websocketUri, this.simpMessagingTemplate, this.sessionRegistry);
    } catch (InterruptedException ex) {
      log.severe("error when trying to reconnect StompSession " + ex.getMessage());
    }
  }
}
