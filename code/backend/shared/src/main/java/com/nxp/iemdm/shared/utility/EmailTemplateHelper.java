package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.shared.intf.notification.EmailTemplate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class EmailTemplateHelper {

  private final Configuration freemarkerConfig;

  public EmailTemplateHelper(Configuration freemarkerConfig) {
    super();
    this.freemarkerConfig = freemarkerConfig;
  }

  public String buildBody(Map<String, Object> model, EmailTemplate emailTemplate) {
    try {
      Template freemarkerTemplate = freemarkerConfig.getTemplate(emailTemplate.getName());
      StringWriter result = new StringWriter();
      freemarkerTemplate.process(model, result);
      return result.toString();
    } catch (TemplateException | IOException e) {
      return "error generating email body " + e.getMessage();
    }
  }
}
