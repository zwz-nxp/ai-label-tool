package com.nxp.iemdm.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
public class IEMdmServicesApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(IEMdmServicesApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(IEMdmServicesApplication.class, args);
  }
}
