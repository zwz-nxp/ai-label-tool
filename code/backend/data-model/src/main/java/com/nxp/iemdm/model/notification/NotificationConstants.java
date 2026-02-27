package com.nxp.iemdm.model.notification;

import java.util.List;

public class NotificationConstants {
  private NotificationConstants() {}

  public static final String WEEKLY_NOTIFICATIONS = "weeklynotification";
  public static final String LOCATION_EVENTS = "locationEvents";
  public static final String NOTIFICATIONS_CALENDAR = "Calendar";
  public static final String NOTIFICATIONS_RG_PLANNING = "ResourceGroupPlanning";
  public static final String NOTIFICATIONS_PASSED = "DueDatePassedEvents";

  public static final List<String> NOTIFY_TYPES =
      List.of(
          NotificationConstants.NOTIFICATIONS_CALENDAR,
          NotificationConstants.NOTIFICATIONS_RG_PLANNING,
          NotificationConstants.NOTIFICATIONS_PASSED);
}
