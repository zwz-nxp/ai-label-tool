package com.nxp.iemdm.capacitystatementservice.configuration;

import com.nxp.iemdm.capacitystatementservice.freemarker.RestTemplateLoader;
import com.nxp.iemdm.capacitystatementservice.service.PersonService;
import com.nxp.iemdm.capacitystatementservice.service.integration.I2FileHandler;
import java.io.File;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:capacity-statement-service.properties")
@PropertySource(
    value = "classpath:capacity-statement-service.override.properties",
    ignoreResourceNotFound = true)
@ComponentScan(basePackages = "com.nxp.iemdm")
@EnableScheduling
@EnableIntegration
public class SpringConfiguration {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }

  @Primary
  @Bean
  public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration(
      RestTemplateLoader templateLoader) {
    FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactory =
        new FreeMarkerConfigurationFactoryBean();
    freeMarkerConfigurationFactory.setPreTemplateLoaders(templateLoader);
    Properties settings = new Properties();

    settings.put("localized_lookup", "false");

    freeMarkerConfigurationFactory.setFreemarkerSettings(settings);
    return freeMarkerConfigurationFactory;
  }

  @Bean
  @InboundChannelAdapter(
      channel = "fileInputChannel",
      poller = @Poller(fixedDelay = "${i2.logdirectory.pollinginterval}"))
  public MessageSource<File> fileReadingMessageSource(
      @Value("${iemdm.user.wbi}") String iemdmUserWBI,
      @Value("${i2.logdirectory.location}") String i2LogDirectory) {
    CompositeFileListFilter<File> filters = new CompositeFileListFilter<>();
    filters.addFilter(new RegexPatternFileListFilter(".*" + iemdmUserWBI + ".*\\.csv"));
    filters.addFilter(new AcceptOnceFileListFilter<>());

    FileReadingMessageSource source = new FileReadingMessageSource();
    source.setAutoCreateDirectory(true);
    source.setWatchEvents(FileReadingMessageSource.WatchEventType.CREATE);
    source.setDirectory(new File(i2LogDirectory));
    source.setFilter(filters);

    return source;
  }

  @Bean
  @ServiceActivator(inputChannel = "fileInputChannel")
  public MessageHandler fileProcessor(
      PersonService personService, @Value("${iemdm.user.wbi}") String iemdmUserWbi) {
    return new I2FileHandler(personService, iemdmUserWbi);
  }
}
