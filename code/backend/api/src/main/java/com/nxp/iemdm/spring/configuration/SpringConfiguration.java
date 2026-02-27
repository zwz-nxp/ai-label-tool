package com.nxp.iemdm.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.handler.SessionHandler;
import com.nxp.iemdm.shared.IemdmConstants;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@Configuration
@PropertySource("classpath:ie-mdm.properties")
@PropertySource(value = "classpath:ie-mdm.override.properties", ignoreResourceNotFound = true)
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = "com.nxp.iemdm")
@Slf4j
public class SpringConfiguration {

  private static final int BUFFER_SIZE = 8192 * 1024;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    restTemplate.getInterceptors().add(this.authorizationHeaderInterceptor());
    return restTemplate;
  }

  // Todo: Might be unnecessary if we can @Authenticated something
  private ClientHttpRequestInterceptor authorizationHeaderInterceptor() {
    return (request, body, clientHttpRequestExecution) -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
          String wbi = ((UserDetails) principal).getUsername();
          HttpHeaders headers = request.getHeaders();
          if (!headers.containsKey(IemdmConstants.USER_WBI_HEADER)) {
            headers.set(IemdmConstants.USER_WBI_HEADER, wbi);
          }
        }
      }
      return clientHttpRequestExecution.execute(request, body);
    };
  }

  @Bean
  public StompSession stompSession(
      @Value("${websocket.service.url}") String websocketServerUrl,
      SimpMessagingTemplate template,
      SessionRegistry sessionRegistry)
      throws InterruptedException {
    List<Transport> transports = new ArrayList<>(1);

    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    container.setDefaultMaxBinaryMessageBufferSize(BUFFER_SIZE);
    container.setDefaultMaxTextMessageBufferSize(BUFFER_SIZE);

    transports.add(new WebSocketTransport(new StandardWebSocketClient(container)));
    WebSocketClient transport = new SockJsClient(transports);
    WebSocketStompClient stompClient = new WebSocketStompClient(transport);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
    messageConverter.setObjectMapper(objectMapper);

    stompClient.setMessageConverter(messageConverter);
    stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

    stompClient.setInboundMessageSizeLimit(BUFFER_SIZE);

    SessionHandler sessionHandler =
        new SessionHandler(
            template, sessionRegistry, this.threadPoolTaskScheduler(), websocketServerUrl, 30);

    long sleepTime = 30000;
    ListenableFuture<StompSession> connect =
        stompClient.connect(websocketServerUrl, sessionHandler);
    StompSession session;
    while (true) {
      try {
        session = connect.get();
        return session;
      } catch (Exception exception) {
        Thread.sleep(sleepTime);
        log.error("API subscription to sync-service failed", exception);
      }
    }
  }

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    return threadPoolTaskScheduler;
  }

  @Bean
  SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }
}
