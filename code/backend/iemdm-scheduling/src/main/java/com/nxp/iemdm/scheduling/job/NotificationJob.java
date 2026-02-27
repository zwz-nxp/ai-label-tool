package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.notification.NotificationModService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class NotificationJob implements IemdmQuartzJob {

  private final NotificationModService notificationModService;

  public NotificationJob(NotificationModService notificationModService) {
    this.notificationModService = notificationModService;
  }

  @Override
  public void execute(JobExecutionContext context) {
    this.notificationModService.setTwoWeeksOldNotificationsToReadEqualsTrue();
  }

  @Override
  public String getDescription() {
    return "SYSTEM mark notifications of two weeks old as read";
  }
}
