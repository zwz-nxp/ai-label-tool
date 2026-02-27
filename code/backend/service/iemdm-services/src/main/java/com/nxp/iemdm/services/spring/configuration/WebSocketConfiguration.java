package com.nxp.iemdm.services.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

  private final Hibernate6Module hibernate6Module;

  public WebSocketConfiguration(Hibernate6Module hibernate6Module) {
    this.hibernate6Module = hibernate6Module;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/socket").setAllowedOrigins("*").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry
        .setApplicationDestinationPrefixes("/", "/topic")
        .setCacheLimit(8192 * 1024)
        .enableSimpleBroker("/topic");
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
    registry.setSendBufferSizeLimit(8192 * 1024);
  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(hibernate6Module);
    objectMapper.findAndRegisterModules();

    MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
    messageConverter.setObjectMapper(objectMapper);

    messageConverters.add(messageConverter);

    return false;
  }
}
