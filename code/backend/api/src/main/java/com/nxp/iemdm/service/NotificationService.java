package com.nxp.iemdm.service;

import com.nxp.iemdm.model.notification.Notification;
import java.util.List;

public interface NotificationService {
  List<Notification> getAllUnreadNotificationsForUser(String username);

  List<Notification> getAllNotificationsForUser(String username);

  Notification saveNotification(Notification notification);

  Iterable<Notification> saveAllNotifications(List<Notification> notifications);

  String createWeeklyNotification(String wbi, Integer locationId);
}
