package com.nxp.iemdm.initialaudit.configuration;

import oracle.jdbc.driver.OracleDriver;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:initial-audit.properties")
@PropertySource(
    value = "classpath:initial-audit.override.properties",
    ignoreResourceNotFound = true)
@ComponentScan(basePackages = "com.nxp.iemdm")
@EntityScan({"com.nxp.iemdm.model", "com.nxp.iemdm.initialaudit.model"})
public class SpringConfiguration {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }

  @Bean
  @ConfigurationProperties(prefix = "app.datasource")
  public DataSource dataSource() {
    SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
    //simpleDriverDataSource.setDriverClass(OracleDriver.class);
    simpleDriverDataSource.setDriverClass(org.postgresql.Driver.class);
    return simpleDriverDataSource;
  }
}
