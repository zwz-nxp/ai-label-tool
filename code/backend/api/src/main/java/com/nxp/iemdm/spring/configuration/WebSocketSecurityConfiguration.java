package com.nxp.iemdm.spring.configuration;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;
import static com.nxp.iemdm.spring.constant.ApiConstants.SERVER_ENVIRONMENT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfiguration
    extends AbstractSecurityWebSocketMessageBrokerConfigurer {
  private final String environment;

  @Autowired
  public WebSocketSecurityConfiguration(@Value("${security.environment}") String environment) {
    this.environment = environment;
  }

  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }

  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    switch (environment) {
      case LOCAL_DEVELOPMENT_ENVIRONMENT:
        messages.anyMessage().permitAll();
        break;
      case SERVER_ENVIRONMENT:
      // fallthrough
      default:
        messages.simpDestMatchers("/**").authenticated().anyMessage().authenticated();
    }
  }
}
