package com.nxp.iemdm.operational.service;

import static com.nxp.iemdm.shared.intf.notification.MailService.HIGH_PRIORITY;

import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.operational.service.rest.PersonServiceImpl;
import com.nxp.iemdm.shared.intf.controller.ErrorService;
import com.nxp.iemdm.shared.intf.notification.MailService;
import jakarta.mail.MessagingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Scope("singleton")
@Service
public class ErrorServiceImpl implements ErrorService {
  private static final Logger LOG = LoggerFactory.getLogger(ErrorServiceImpl.class);
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd-MM-yyyy 'at' HH:mm:ss")
          .withLocale(Locale.UK)
          .withZone(ZoneId.systemDefault());

  private final Queue<ExceptionContainer> exceptions = new LinkedBlockingQueue<>();

  private final PersonServiceImpl personService;
  private final MailService mailService;

  public ErrorServiceImpl(PersonServiceImpl personService, MailService mailService) {
    this.personService = personService;
    this.mailService = mailService;
  }

  public void handleException(Exception exception) {
    exceptions.add(new ExceptionContainer(exception));
  }

  public void mailExceptions() {
    if (!exceptions.isEmpty()) {

      StringBuilder stringBuilder = new StringBuilder();

      stringBuilder.append(
          "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
      stringBuilder.append(
          "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">");
      stringBuilder.append("<body>\n");
      stringBuilder.append("<p>Hi %s,</p>\n");
      stringBuilder.append("<p>");
      stringBuilder.append("Some errors have occurred in the IE-MDM system:");

      stringBuilder.append("<ul>\n");
      while (!exceptions.isEmpty()) {
        ExceptionContainer exceptionContainer = exceptions.remove();

        stringBuilder.append("<li>\n");
        stringBuilder.append("<p><b>Occurred on: </b>");
        stringBuilder.append(DATE_TIME_FORMATTER.format(exceptionContainer.getTimestamp()));
        stringBuilder.append("</p>\n");
        stringBuilder.append("<p><b>Exception: </b>");
        stringBuilder.append(
            exceptionContainer.getException().getMessage().replaceAll("\n", "<br/>"));
        stringBuilder.append("</p>\n");
        stringBuilder.append("</li>\n");
      }

      stringBuilder.append("</ul>");
      stringBuilder.append("<p>Regards,<br/>\n");
      stringBuilder.append("IE-MDM</p>\n");
      stringBuilder.append("</body>");
      stringBuilder.append("</html>");

      String errorMail = stringBuilder.toString();

      Set<Person> administrators =
          personService.getAllPersonsWithGlobalRole("Administrator_System");

      for (Person person : administrators) {
        String mail = String.format(errorMail, person.getName());

        try {
          mailService.sendMail(
              mail, new HashMap<>(), HIGH_PRIORITY, person.getEmail(), "IE-MDM Error message");
        } catch (MessagingException e) {
          LOG.error("An error occurred during error mail sending!", e);
        }
      }
    }
  }

  @Getter
  private static class ExceptionContainer {
    private final Exception exception;
    private final Instant timestamp;

    private ExceptionContainer(Exception exception) {
      this.exception = exception;
      this.timestamp = Instant.now();
    }
  }
}
