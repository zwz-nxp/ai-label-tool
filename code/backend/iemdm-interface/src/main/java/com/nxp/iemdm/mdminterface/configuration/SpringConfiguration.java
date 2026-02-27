package com.nxp.iemdm.mdminterface.configuration;

import com.tibco.tibjms.TibjmsConnectionFactory;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableJms
@EnableScheduling
@ComponentScan(basePackages = {"com.nxp.iemdm.mdminterface", "com.nxp.iemdm.shared.utility"})
@PropertySource("classpath:ie-mdm-interface.properties")
@PropertySource(
    value = "classpath:ie-mdm-interface.override.properties",
    ignoreResourceNotFound = true)
public class SpringConfiguration {
  @Bean
  public ConnectionFactory connectionFactory(@Value("${jms.url}") String jmsUrl) {
    return new TibjmsConnectionFactory(jmsUrl);
  }

  @Bean
  public Jaxb2Marshaller jaxb2Marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setPackagesToScan("com.nxp.iemdm.model");
    return marshaller;
  }

  @Bean
  public MessageConverter jmsMessageConverter(Jaxb2Marshaller jaxb2Marshaller) {
    MarshallingMessageConverter converter = new MarshallingMessageConverter();

    converter.setMarshaller(jaxb2Marshaller);
    converter.setUnmarshaller(jaxb2Marshaller);
    converter.setTargetType(MessageType.TEXT);

    return converter;
  }

  @Bean
  public JmsTemplate jmsTopicTemplate(
      ConnectionFactory connectionFactory, MessageConverter messageConverter) {

    JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

    jmsTemplate.setPubSubDomain(true);
    jmsTemplate.setMessageConverter(messageConverter);

    return jmsTemplate;
  }

  @ConditionalOnExpression("${jms.template.dummy:false}")
  @Bean
  public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
    return new JmsTemplate(connectionFactory) {
      private final Logger logger = LoggerFactory.getLogger("DummyJmsTemplate");

      @Override
      public void convertAndSend(
          String destinationName, Object message, MessagePostProcessor postProcessor)
          throws JmsException {

        this.logger.info(
            "convertAndSend called with destinationName: '{}' and message of type {}",
            destinationName,
            message == null ? "N/A" : message.getClass().getName());

        Message dummyMessage =
            new ActiveMQTextMessage() {
              @Override
              public void setStringProperty(String name, String value) {
                logger.info(
                    "postProcessor.setStringProperty called for property name: '{}' and value: '{}'",
                    name,
                    value);
              }
            };
        try {
          postProcessor.postProcessMessage(dummyMessage);
        } catch (JMSException e) {
          this.logger.error("Exception post-processing dummy JMS message", e);
        }
      }
    };
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }
}
