package com.nxp.iemdm.notification.freemarker.templateloader;

import com.nxp.iemdm.model.configuration.Template;
import com.nxp.iemdm.notification.repository.jpa.TemplateNotificationRepository;
import freemarker.cache.TemplateLoader;
import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JPATemplateLoader implements TemplateLoader {
  private final TemplateNotificationRepository templateNotificationRepository;

  @Autowired
  public JPATemplateLoader(TemplateNotificationRepository templateNotificationRepository) {
    this.templateNotificationRepository = templateNotificationRepository;
  }

  @Override
  public Object findTemplateSource(String name) throws IOException {
    try {
      return templateNotificationRepository.findById(name).map(Template::getName).orElse(null);
    } catch (PersistenceException e) {
      throw new IOException(e);
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    return templateNotificationRepository
        .findById((String) templateSource)
        .map(Template::getLastUpdated)
        .map(Instant::toEpochMilli)
        .orElse(-1L);
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    try {
      String template =
          templateNotificationRepository
              .findById((String) templateSource)
              .map(Template::getTemplate)
              .orElseThrow(IOException::new);

      return new StringReader(template);
    } catch (PersistenceException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    // do nothing
  }
}
