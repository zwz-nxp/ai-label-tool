package com.nxp.iemdm.notification.service.cron;

import com.nxp.iemdm.enums.notification.NotificationLevel;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.notification.NotificationConstants;
import com.nxp.iemdm.model.request.event.pojo.EmailTemplateEventData;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.notification.service.NotificationService;
import com.nxp.iemdm.notification.service.rest.NotificationModServiceImpl;
import com.nxp.iemdm.shared.intf.controller.NotificationJobService;
import com.nxp.iemdm.shared.intf.notification.TemplateProcessor;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationSchedule implements NotificationJobService {
  private final PersonService personService;
  private final TemplateProcessor templateProcessor;
  private final NotificationModServiceImpl notificationModService;
  private final NotificationService notificationService;

  public NotificationSchedule(
      PersonService personService,
      TemplateProcessor templateProcessor,
      NotificationModServiceImpl notificationModService,
      NotificationService notificationService) {
    this.personService = personService;
    this.templateProcessor = templateProcessor;
    this.notificationModService = notificationModService;
    this.notificationService = notificationService;
  }

  public void createNotifications() {
    Iterable<Person> persons = personService.getAllPersons();

    LocalDate start = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    LocalDate end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusWeeks(2);

    for (Person person : persons) {
      Map<Location, Map<String, List<EmailTemplateEventData>>> filteredLocationEvents =
          new HashMap<>();

      if (!filteredLocationEvents.isEmpty()) {
        Map<String, Object> templateData = new HashMap<>();

        templateData.put(NotificationConstants.LOCATION_EVENTS, filteredLocationEvents);

        Notification notification = new Notification();

        notification.setRecipient(person);
        notification.setTitle("Weekly work list");
        notification.setMessage(
            templateProcessor.createTextFromTemplate(
                NotificationConstants.WEEKLY_NOTIFICATIONS, templateData));
        notification.setTimestamp(Instant.now());
        notification.setSeverityLevel(NotificationLevel.INFORMATION);

        notificationModService.saveAndMailNotification(notification);
      }
    }
  }
}
