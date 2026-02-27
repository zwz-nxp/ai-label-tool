package com.nxp.iemdm.notification.service;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.notification.MailService;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.VersionInfoService;
import com.nxp.iemdm.shared.utility.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Log
public class MailServiceImpl implements MailService {
  private static final String TEMPLATE_BANNER_FILE = "banner_file";
  private static final String TEMPLATE_BANNER_FILE_NAME = "IE-MDM_Banner.jpg";
  private static final String TEMPLATE_MESSAGE_TITLE = "message_title";
  private static final String TEMPLATE_MESSAGE_TYPE = "message_type";
  private static final String TEMPLATE_APPLICATION_NAME = "application_name";
  private static final String TEMPLATE_APPLICATION_VERSION = "application_version";
  private static final String TEMPLATE_APPLICATION_YEAR = "application_year";
  private static final String TEMPLATE_APPLICATION_LINK = "application_link";

  private final JavaMailSender javaMailSender;
  private final Configuration freemarkerConfig;
  private final ConfigurationValueService configurationValueService;
  private final VersionInfoService versionInfoService;
  private final String mailTemplateName;
  private final String bannerName;
  private final String fromAddress;
  private final String mailTemplateLink;

  public MailServiceImpl(
      JavaMailSender javaMailSender,
      Configuration freemarkerConfig,
      ConfigurationValueService configurationValueService,
      VersionInfoService versionInfoService,
      @Value("${mail.template.name}") String mailTemplateName,
      @Value("${mail.template.banner}") String bannerName,
      @Value("${mail.from}") String fromAddress,
      @Value("${mail.template.link}") String mailTemplateLink) {
    this.javaMailSender = javaMailSender;
    this.freemarkerConfig = freemarkerConfig;
    this.configurationValueService = configurationValueService;
    this.versionInfoService = versionInfoService;
    this.mailTemplateName = mailTemplateName;
    this.bannerName = bannerName;
    this.fromAddress = fromAddress;
    this.mailTemplateLink = mailTemplateLink;
  }

  @MethodLog
  public void sendNotificationMail(Notification notification) {
    if (this.hasValidEmail(notification.getRecipient())) {
      try {
        Map<String, Object> model = this.makeMailModel(notification);

        Template template = freemarkerConfig.getTemplate(mailTemplateName);
        StringWriter result = new StringWriter();
        template.process(model, result);
        String html = result.toString();

        Map<String, byte[]> attachments = this.getBanner();

        int priority =
            URGENT_TYPES.contains(notification.getSeverityLevel())
                ? HIGH_PRIORITY
                : NORMAL_PRIORITY;

        String email = notification.getRecipient().getEmail();
        String subject = notification.getTitle();

        this.sendMail(html, attachments, priority, email, subject);
      } catch (IOException | TemplateException | MessagingException | MailException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }

  private boolean hasValidEmail(Person user) {
    if (StringUtil.isValidEmail(user.getEmail())) {
      return true;
    } else {
      String logMessage =
          String.format("MailService: %s did not have a valid email address", user.getWbi());
      log.severe(logMessage);
      return false;
    }
  }

  public void sendMail(
      String text, Map<String, byte[]> attachments, int priority, String email, String subject)
      throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper =
        new MimeMessageHelper(
            message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

    for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
      helper.addAttachment(
          entry.getKey(),
          new ByteArrayResourceWithFilename(entry.getValue(), entry.getKey(), entry.getKey()));
    }
    String applicationName =
        this.configurationValueService
            .getConfigurationItemForKey(TEMPLATE_APPLICATION_NAME)
            .getConfigurationValue();

    helper.setTo(email);
    helper.setText(text, true);
    helper.setSubject(String.format("%s - %s", applicationName, subject));
    helper.setFrom(fromAddress);
    helper.setPriority(priority);

    this.javaMailSender.send(message);
  }

  Map<String, Object> makeMailModel(Notification notification) throws NotFoundException {
    Map<String, Object> model = new HashMap<>();
    model.put("user", notification.getRecipient());
    model.put("message", notification.getMessage());
    String title =
        this.configurationValueService
            .getConfigurationItemForKey(TEMPLATE_MESSAGE_TITLE)
            .getConfigurationValue();
    model.put(TEMPLATE_MESSAGE_TITLE, title);
    String messageType =
        this.configurationValueService
            .getConfigurationItemForKey(TEMPLATE_MESSAGE_TYPE)
            .getConfigurationValue();
    model.put(TEMPLATE_MESSAGE_TYPE, messageType);
    String applicationName =
        this.configurationValueService
            .getConfigurationItemForKey(TEMPLATE_APPLICATION_NAME)
            .getConfigurationValue();
    model.put(TEMPLATE_APPLICATION_NAME, applicationName);
    String applicationVersion = this.versionInfoService.getServices().getVersion();
    model.put(TEMPLATE_APPLICATION_VERSION, applicationVersion);
    String applicationBuildYear =
        this.configurationValueService
            .getConfigurationItemForKey(TEMPLATE_APPLICATION_YEAR)
            .getConfigurationValue();
    model.put(TEMPLATE_BANNER_FILE, TEMPLATE_BANNER_FILE_NAME);
    model.put(TEMPLATE_APPLICATION_YEAR, applicationBuildYear);
    model.put(TEMPLATE_APPLICATION_LINK, this.mailTemplateLink);
    return model;
  }

  private Map<String, byte[]> getBanner() {
    String path = String.format("banner/%s", this.bannerName);
    try (InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(path)) {
      if (resourceStream == null) {
        return Map.of();
      }
      byte[] banner = resourceStream.readAllBytes();
      Map<String, byte[]> attachments = new HashMap<>();
      attachments.put(TEMPLATE_BANNER_FILE_NAME, banner);
      return attachments;
    } catch (IOException ioException) {
      log.severe("Could not load banner: " + ioException.getMessage());
      return Map.of();
    }
  }

  private static class ByteArrayResourceWithFilename extends ByteArrayResource {
    private final String filename;

    public ByteArrayResourceWithFilename(byte[] byteArray, String description, String filename) {
      super(byteArray, description);
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return filename;
    }

    @Override
    public boolean equals(Object other) {
      return super.equals(other);
    }
  }
}
