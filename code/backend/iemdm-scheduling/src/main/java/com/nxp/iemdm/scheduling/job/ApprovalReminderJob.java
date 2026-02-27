package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.controller.ReminderService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class ApprovalReminderJob implements IemdmQuartzJob {
  private final ReminderService reminderService;

  public ApprovalReminderJob(ReminderService reminderService) {
    this.reminderService = reminderService;
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    this.reminderService.sendApprovalReminders();
  }

  @Override
  public String getDescription() {
    return "Send approval reminders for initiated requests";
  }
}
