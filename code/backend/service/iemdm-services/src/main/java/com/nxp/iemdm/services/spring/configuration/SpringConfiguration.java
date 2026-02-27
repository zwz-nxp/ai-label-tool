package com.nxp.iemdm.services.spring.configuration;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.nxp.iemdm.notification.freemarker.templateloader.JPATemplateLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.client.RestTemplate;

@Log
@Configuration
@PropertySource("classpath:iemdm-services.properties")
@PropertySource(
    value = "classpath:iemdm-services.override.properties",
    ignoreResourceNotFound = true)
@EnableCaching
@EnableScheduling
@EntityScan({
  "com.nxp.iemdm.model",
  "com.nxp.iemdm.view",
  "com.nxp.iemdm.services.spring.configuration.audit.revision.model"
})
@ComponentScan(basePackages = "com.nxp.iemdm")
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
      "com.nxp.iemdm.shared.intf.operational",
      // "com.nxp.iemdm.shared.intf.consparams",
      "com.nxp.iemdm.shared.repository.jpa",
      // "com.nxp.iemdm.shared.intf.controller",
      "com.nxp.iemdm.operational.repository.jpa",
      "com.nxp.iemdm.notification.repository.jpa",
      // "com.nxp.iemdm.consparams.repository.jpa.sap.code",
      // "com.nxp.iemdm.usagerate.repository.jpa",
      // "com.nxp.iemdm.approval.repository.jpa",
      // "com.nxp.iemdm.planning.repository.jpa"
    })
public class SpringConfiguration {

  @Bean
  @Primary
  public DataSource dataSource(
      @Value("${app.datasource.url}") String jdbcUrl,
      @Value("${app.datasource.username}") String username,
      @Value("${app.datasource.password}") String password,
      @Value("${app.datasource.hikari.minimum.idle.connections:1}") int minimumIdle,
      @Value("${app.datasource.hikari.maximum.pool.size:20}") int maximumPoolSize,
      @Value("${app.datasource.hikari.idle.timeout.milliseconds:60000}")
          int idleTimeoutMilliseconds,
      @Value("${app.datasource.hikari.auto.commit:false}") boolean autoCommit) {

    HikariConfig hikariConfig = new HikariConfig();
    // hikariConfig.setDriverClassName(OracleDriver.class.getName());
    hikariConfig.setDriverClassName("org.postgresql.Driver");
    hikariConfig.setJdbcUrl(jdbcUrl);
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
    hikariConfig.setMinimumIdle(minimumIdle);
    hikariConfig.setMaximumPoolSize(maximumPoolSize);
    hikariConfig.setIdleTimeout(idleTimeoutMilliseconds);
    hikariConfig.setAutoCommit(autoCommit);
    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public Hibernate6Module hibernate6Module(@Value("${app.datasource.username}") String dsUsername) {
    log.warning("Datasource with username " + dsUsername);
    Hibernate6Module module = new Hibernate6Module();
    module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
    module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
    return module;
  }

  @Primary
  @Bean
  public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration(
      JPATemplateLoader templateLoader) {
    FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactory =
        new FreeMarkerConfigurationFactoryBean();
    freeMarkerConfigurationFactory.setPreTemplateLoaders(templateLoader);
    return freeMarkerConfigurationFactory;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }
}
