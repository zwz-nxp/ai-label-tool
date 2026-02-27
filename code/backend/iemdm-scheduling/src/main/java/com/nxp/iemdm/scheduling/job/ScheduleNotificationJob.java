package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.controller.NotificationJobService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class ScheduleNotificationJob implements IemdmQuartzJob {
  private final NotificationJobService notificationJobService;

  public ScheduleNotificationJob(NotificationJobService notificationJobService) {
    this.notificationJobService = notificationJobService;
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    this.notificationJobService.createNotifications();
  }

  @Override
  public String getDescription() {
    return "Send notifications for next week's scheduled planned persons or engineers";
  }
}
