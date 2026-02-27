package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.Template;

public interface TemplateService {
  Template getTemplate(String name);

  Template saveTemplate(Template template);

  Iterable<Template> getAllTemplates();
}
