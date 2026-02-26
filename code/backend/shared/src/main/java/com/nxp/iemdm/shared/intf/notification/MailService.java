package com.nxp.iemdm.shared.intf.notification;

import com.nxp.iemdm.enums.notification.NotificationLevel;
import com.nxp.iemdm.model.notification.Notification;
import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Map;

public interface MailService {

  List<NotificationLevel> URGENT_TYPES =
      List.of(NotificationLevel.SYSTEM_WARNING, NotificationLevel.URGENT_ACTION);

  int NORMAL_PRIORITY = 3;
  int HIGH_PRIORITY = 1;

  void sendNotificationMail(Notification notification);

  void sendMail(
      String text, Map<String, byte[]> attachments, int priority, String email, String subject)
      throws MessagingException;
}
