package com.nxp.iemdm.controller;

import com.nxp.iemdm.enums.notification.NotificationLevel;
import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.notification.NotificationParent;
import com.nxp.iemdm.service.NotificationService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
  private final NotificationService notificationService;

  @Autowired
  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @MethodLog
  @GetMapping(path = "/unread", produces = MediaType.APPLICATION_JSON)
  public List<NotificationParent> getAllUnreadCapacityStatementReportsForUser(
      @AuthenticationPrincipal IEMDMPrincipal user) {
    List<NotificationParent> notifications = new ArrayList<>();

    notifications.addAll(notificationService.getAllUnreadNotificationsForUser(user.getUsername()));

    Collections.sort(notifications);
    return notifications;
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<NotificationParent> getAllCapacityStatementReportsForUser(
      @AuthenticationPrincipal IEMDMPrincipal user) {
    List<NotificationParent> notifications = new ArrayList<>();

    notifications.addAll(notificationService.getAllNotificationsForUser(user.getUsername()));

    return notifications;
  }

  @MethodLog
  @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
  public NotificationParent saveNotification(@RequestBody @Valid NotificationParent notification) {
    return switch (notification.getType()) {
      case "notification" -> notificationService.saveNotification((Notification) notification);
      default -> notification;
    };
  }

  @MethodLog
  @PostMapping(path = "/readAll", consumes = MediaType.APPLICATION_JSON)
  public void readAllNotifications(@RequestBody @Valid List<NotificationParent> notifications) {
    List<Notification> commonNotifications = new ArrayList<>();
    for (NotificationParent notification : notifications) {
      String type = notification.getType();
      if (type.equals("notification")) {
        commonNotifications.add((Notification) notification);
      }
    }

    if (!commonNotifications.isEmpty())
      notificationService.saveAllNotifications(commonNotifications);
  }

  @MethodLog
  @GetMapping(path = "/weekly/{location}", produces = MediaType.APPLICATION_JSON)
  public String createWeeklyNotification(
      @PathVariable("location") Integer locationId, @AuthenticationPrincipal IEMDMPrincipal user) {
    return notificationService.createWeeklyNotification(user.getUsername(), locationId);
  }

  @MethodLog
  @GetMapping(path = "/hasSysWarnings", produces = MediaType.APPLICATION_JSON)
  public boolean hasSystemWarnings(@AuthenticationPrincipal IEMDMPrincipal user) {
    return notificationService.getAllUnreadNotificationsForUser(user.getUsername()).stream()
        .anyMatch(
            notification ->
                notification.getSeverityLevel().equals(NotificationLevel.SYSTEM_WARNING));
  }
}
