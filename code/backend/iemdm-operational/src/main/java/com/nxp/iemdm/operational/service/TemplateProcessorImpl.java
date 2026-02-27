package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.shared.intf.notification.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TemplateProcessorImpl implements TemplateProcessor {
  private static final Logger logger =
      LoggerFactory.getLogger(TemplateProcessorImpl.class.getName());

  private final Configuration freemarkerConfig;
  private final ErrorServiceImpl errorService;

  public TemplateProcessorImpl(Configuration freemarkerConfig, ErrorServiceImpl errorService) {
    this.freemarkerConfig = freemarkerConfig;
    this.errorService = errorService;
  }

  public String createTextFromTemplate(String templateName, Map<String, Object> templateData) {
    try {
      Template template = freemarkerConfig.getTemplate(templateName);

      if (this.containsEvents(templateData)) {
        StringWriter result = new StringWriter();
        template.process(templateData, result);
        return result.toString();
      } else {
        return "No events";
      }
    } catch (IOException | TemplateException e) {
      logger.warn("Exception during template processing", e);
      errorService.handleException(e);
      return "There was an error during template processing";
    }
  }

  public String createReminderBody(Map<String, Object> templateData) {
    try {
      Template template = freemarkerConfig.getTemplate("approval-request");
      StringWriter result = new StringWriter();
      template.process(templateData, result);
      return result.toString();
    } catch (IOException | TemplateException e) {
      logger.warn("Exception during template processing", e);
      errorService.handleException(e);
      return "There was an error during template processing";
    }
  }

  public String createBodyFromTemplate(String templateName, Map<String, Object> templateData) {
    try {
      Template template = freemarkerConfig.getTemplate(templateName);
      StringWriter result = new StringWriter();
      template.process(templateData, result);
      return result.toString();
    } catch (IOException | TemplateException e) {
      logger.warn("Exception during template processing", e);
      errorService.handleException(e);
      return "There was an error during template processing";
    }
  }

  // ---------------- private ------------------------------------------
  @SuppressWarnings("unchecked")
  private boolean containsEvents(Map<String, Object> templateData) {
    return false;
  }
}
