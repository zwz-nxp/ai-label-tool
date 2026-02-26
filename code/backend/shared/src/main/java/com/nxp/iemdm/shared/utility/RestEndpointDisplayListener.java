package com.nxp.iemdm.shared.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class RestEndpointDisplayListener implements ApplicationListener<ContextRefreshedEvent> {

  private final boolean isEndpointDumpEnabled;

  public RestEndpointDisplayListener(
      @Value("${com.nxp.iemdm.application.enable.endpointdump:true}")
          boolean isEndpointDumpEnabled) {
    this.isEndpointDumpEnabled = isEndpointDumpEnabled;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // display endpoints in system out on startup in this environment
    if (this.isEndpointDumpEnabled) {
      ApplicationContext applicationContext = event.getApplicationContext();
      applicationContext
          .getBean(RequestMappingHandlerMapping.class)
          .getHandlerMethods()
          .forEach(this::dump);
    }
  }

  /**
   * Prints the endpoints to the output. This is what you see when starting up the application(s).
   *
   * @param key
   * @param val
   */
  private void dump(Object key, Object val) {
    System.out.printf("%s => %s%n", key, val);
  }
}
