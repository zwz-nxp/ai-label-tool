package com.nxp.iemdm.initialaudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class InitialAuditApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(InitialAuditApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(InitialAuditApplication.class, args);
  }
}
