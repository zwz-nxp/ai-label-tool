package com.nxp.iemdm.scheduling.listener;

import com.nxp.iemdm.enums.notification.NotificationLevel;
import com.nxp.iemdm.enums.user.UserRoleType;
import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.shared.intf.notification.NotificationModService;
import com.nxp.iemdm.shared.intf.operational.UserRoleService;
import com.nxp.iemdm.shared.utility.DateTimeUtility;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class JobMailNotificationOnFailureListener extends ListenerBase implements JobListener {

  public static final String NAME = JobMailNotificationOnFailureListener.class.getSimpleName();

  private static final String TITLE_PREFIX = "IE-MDM Job failed:";

  private final NotificationModService notificationModService;
  private final UserRoleService userRoleService;

  public JobMailNotificationOnFailureListener(
      NotificationModService notificationModService, UserRoleService userRoleService) {
    this.notificationModService = notificationModService;
    this.userRoleService = userRoleService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    // not needed
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    // not needed
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    if (jobException != null) {
      Date start = context.getTrigger().getStartTime();
      String triggerName = this.getTriggerNameIfCronJob(context);
      String title = String.format("%s %s", TITLE_PREFIX, triggerName);
      String exceptionMessage = this.exceptionMessage(jobException);
      LocalDateTime now = LocalDateTime.now();
      String message = this.generateMessage(triggerName, start, now, exceptionMessage);
      List<UserRole> relevantUsers =
          this.userRoleService.findAllByRoleId(UserRoleType.ADMINISTRATOR_SYSTEM.getName());
      for (UserRole userRole : relevantUsers) {
        Person recipient = userRole.getUser();
        Notification notification = this.sendNotification(title, message, now, recipient);
        this.notificationModService.saveAndMailNotification(notification);
      }
    }
  }

  private Notification sendNotification(
      String title, String message, LocalDateTime timeStamp, Person recipient) {
    Notification notification = new Notification();
    notification.setTitle(title);
    notification.setMessage(message);
    notification.setTimestamp(timeStamp.toInstant(ZoneOffset.UTC));
    notification.setSeverityLevel(NotificationLevel.SYSTEM_WARNING);
    notification.setRecipient(recipient);
    return notification;
  }

  private String generateMessage(
      String triggerName, Date start, LocalDateTime now, String exceptionMessage) {
    return "Sceduled job failed to complete.<br>"
        + String.format("Job or Trigger Name:<br>%s<br>", triggerName)
        + String.format(
            "Run from: %s until %s<br>",
            DateTimeUtility.toIsoString(start), DateTimeUtility.toIsoString(now))
        + String.format("Reason:<br>%s", exceptionMessage);
  }
}
