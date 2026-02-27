package com.nxp.iemdm.notification.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.notification.NotificationConstants;
import com.nxp.iemdm.model.request.event.pojo.EmailTemplateEventData;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserSetting;
import com.nxp.iemdm.notification.service.NotificationService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.notification.MailService;
import com.nxp.iemdm.shared.intf.notification.NotificationModService;
import com.nxp.iemdm.shared.intf.notification.TemplateProcessor;
import com.nxp.iemdm.shared.intf.operational.LocationService;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import com.nxp.iemdm.shared.intf.operational.UserSettingService;
import com.nxp.iemdm.shared.repository.jpa.NotificationRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping("/notification")
public class NotificationModServiceImpl implements NotificationModService {
  private final NotificationRepository notificationRepository;
  private final UserSettingService userSettingService;
  private final MailService mailService;
  private final NotificationService notificationService;
  private final TemplateProcessor templateProcessor;
  private final LocationService locationService;
  private final PersonService personService;

  public NotificationModServiceImpl(
      NotificationRepository notificationRepository,
      UserSettingService userSettingService,
      MailService mailService,
      NotificationService notificationService,
      TemplateProcessor templateProcessor,
      LocationService locationService,
      PersonService personService) {
    this.notificationRepository = notificationRepository;
    this.userSettingService = userSettingService;
    this.mailService = mailService;
    this.notificationService = notificationService;
    this.templateProcessor = templateProcessor;
    this.locationService = locationService;
    this.personService = personService;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/unread/{wbi}", produces = MediaType.APPLICATION_JSON)
  public Iterable<Notification> getAllUnreadNotificationsForUser(@PathVariable("wbi") String wbi) {
    Person user = new Person();

    user.setWbi(wbi);

    return notificationRepository.findAllByRecipientAndRead(user, false);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all/{wbi}", produces = MediaType.APPLICATION_JSON)
  public Iterable<Notification> getAllNotificationsForUser(@PathVariable("wbi") String wbi) {
    Person user = new Person();

    user.setWbi(wbi);

    return notificationRepository.findAllByRecipient(user);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{reportid}", produces = MediaType.APPLICATION_JSON)
  public Notification getNotificationById(@PathVariable("reportid") Integer reportid)
      throws NotFoundException {
    return notificationRepository.findById(reportid).orElseThrow(NotFoundException::new);
  }

  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public Notification saveNotification(@RequestBody Notification notification) {
    return notificationRepository.save(notification);
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/readAll",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Iterable<Notification> saveAllNotifications(
      @RequestBody List<Notification> notifications) {
    return notificationRepository.saveAll(notifications);
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/mail",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Notification saveAndMailNotification(@RequestBody Notification notification) {
    Notification savedNotification = notificationRepository.save(notification);
    if (shouldMailNotification(notification)) {
      mailService.sendNotificationMail(notification);
    }
    return savedNotification;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/weekly/{wbi}/{location}", produces = MediaType.TEXT_PLAIN)
  public String createWeeklyNotification(
      @PathVariable("wbi") String wbi, @PathVariable("location") Integer locationId)
      throws NotFoundException {
    List<Location> locations = new ArrayList<>();

    if (locationId != 0) {
      locations.add(locationService.getLocationById(locationId));
    } else {
      locationService.getAllLocations(null).forEach(locations::add);
    }

    Person person = personService.getPersonByWBI(wbi, true);

    LocalDate start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate end = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusWeeks(2);

    Map<String, Object> templateData = new HashMap<>();
    return templateProcessor.createTextFromTemplate(
        NotificationConstants.WEEKLY_NOTIFICATIONS, templateData);
  }

  @Transactional
  @Override
  public void setTwoWeeksOldNotificationsToReadEqualsTrue() {
    // ChronoUnit weeks was not allowed here :(
    Instant twoWeeksAgo = Instant.now().minus(14, ChronoUnit.DAYS);
    List<Notification> notifications =
        notificationRepository.findByReadAndTimestampBefore(false, twoWeeksAgo);
    notifications.forEach(notification -> notification.setRead(true));
    this.notificationRepository.saveAll(notifications);
  }

  // ----------- private -------------

  private boolean shouldMailNotification(Notification notification) {
    try {
      UserSetting settingForUser =
          userSettingService.getSettingForUser(
              notification.getRecipient().getWbi(), "notificationmail");
      return "true".equalsIgnoreCase(settingForUser.getValue());
    } catch (NotFoundException e) {
      return false;
    }
  }

  private Map<Location, Map<String, List<EmailTemplateEventData>>> convertToEmailDataEvents(
      Map<Location, Map<String, List>> inputEventDataMap) {
    Map<Location, Map<String, List<EmailTemplateEventData>>> result = new HashMap<>();

    inputEventDataMap.forEach(
        (location, eventMap) -> {
          Map<String, List<EmailTemplateEventData>> convertedEventMap = new HashMap<>();
          result.put(location, convertedEventMap);
          // add missing list if needed
          for (String notifyType : NotificationConstants.NOTIFY_TYPES) {
            if (!convertedEventMap.containsKey(notifyType)) {
              convertedEventMap.put(notifyType, new ArrayList<>());
            }
          }
        });
    return result;
  }
}
