package com.nxp.iemdm.handler;

import com.nxp.iemdm.model.configuration.pojo.Update;
import java.time.Instant;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.session.SessionRegistry;

@Log
public class SessionHandler extends StompSessionHandlerAdapter {
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final SessionRegistry sessionRegistry;
  private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
  private final String websocketUri;
  private final int retryTimeSeconds;

  public SessionHandler(
      SimpMessagingTemplate simpMessagingTemplate,
      SessionRegistry sessionRegistry,
      ThreadPoolTaskScheduler threadPoolTaskScheduler,
      String websocketUri,
      int retryTimeSeconds) {
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.sessionRegistry = sessionRegistry;
    this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    this.websocketUri = websocketUri;
    this.retryTimeSeconds = retryTimeSeconds;
    this.threadPoolTaskScheduler.initialize();
  }

  /*
   * Why we need two different instances that derive from StompSessionHandlerAdapter remains elusive
   * to me. Perhaps there is one that is handling updates only, and the second to respond to the UI.
   * However, that is my humble estimation.
   */
  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    session.subscribe(Update.ADDRESS, UpdateHandler.getInstance(this.simpMessagingTemplate));
  }

  @Override
  public void handleException(
      StompSession session,
      StompCommand command,
      StompHeaders headers,
      byte[] payload,
      Throwable throwable) {
    log.log(Level.WARNING, "Stompsession encountered an exception.", throwable);
    Instant retryTime = Instant.now().plusSeconds(this.retryTimeSeconds);
    this.tryToReconnectStompSession(retryTime);
  }

  @Override
  public void handleTransportError(StompSession session, Throwable throwable) {
    log.log(Level.WARNING, "Stompsession encountered a transport error.", throwable);
    Instant retryTime = Instant.now().plusSeconds(this.retryTimeSeconds);
    this.tryToReconnectStompSession(retryTime);
  }

  private void tryToReconnectStompSession(Instant atTime) {
    Runnable runnable =
        new StompSessionReconnectionRunnable(
            this.simpMessagingTemplate, this.sessionRegistry, this.websocketUri);
    this.threadPoolTaskScheduler.schedule(runnable, atTime);
  }
}
