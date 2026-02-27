package com.nxp.iemdm.capacitystatementservice.freemarker;

import com.nxp.iemdm.capacitystatementservice.service.TemplateService;
import com.nxp.iemdm.model.configuration.Template;
import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class RestTemplateLoader implements TemplateLoader {

  private final TemplateService templateService;

  @Autowired
  public RestTemplateLoader(TemplateService templateService) {
    this.templateService = templateService;
  }

  @Override
  public Object findTemplateSource(String name) throws IOException {
    try {
      Template template = templateService.getTemplate(name);

      if (template != null) {
        return template.getName();
      } else {
        return null;
      }
    } catch (RestClientException e) {
      throw new IOException(e);
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    Template template = templateService.getTemplate((String) templateSource);

    if (template != null) {
      return template.getLastUpdated().toEpochMilli();
    } else {
      return -1L;
    }
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    try {
      Template template = templateService.getTemplate((String) templateSource);

      return new StringReader(template.getTemplate());
    } catch (RestClientException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    // do nothing
  }
}
