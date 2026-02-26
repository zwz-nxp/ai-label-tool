package com.nxp.iemdm.shared.intf.notification;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.notification.Notification;

public interface NotificationModService {
  Iterable<Notification> getAllUnreadNotificationsForUser(String wbi);

  Iterable<Notification> getAllNotificationsForUser(String wbi);

  Notification getNotificationById(Integer reportid) throws NotFoundException;

  Notification saveNotification(Notification notification);

  Notification saveAndMailNotification(Notification notification);

  String createWeeklyNotification(String wbi, Integer locationId) throws NotFoundException;

  void setTwoWeeksOldNotificationsToReadEqualsTrue();
}
