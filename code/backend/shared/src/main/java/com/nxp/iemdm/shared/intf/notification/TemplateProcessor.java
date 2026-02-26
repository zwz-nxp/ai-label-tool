package com.nxp.iemdm.shared.intf.notification;

import java.util.Map;

public interface TemplateProcessor {
  /**
   * Generic template to text processor for freemarker templates
   *
   * @param templateName name of template to pull from the database
   * @param templateData map with data for the template to pull from the database
   * @return
   */
  String createBodyFromTemplate(String templateName, Map<String, Object> templateData);

  /**
   * Non-generic template to text processor for freemarker templates This converter is specific for
   * the event reminder
   *
   * @param templateName name of template to pull from the database
   * @param templateData map with data for the template to pull from the database
   * @return
   */
  String createTextFromTemplate(String templateName, Map<String, Object> templateData);
}
