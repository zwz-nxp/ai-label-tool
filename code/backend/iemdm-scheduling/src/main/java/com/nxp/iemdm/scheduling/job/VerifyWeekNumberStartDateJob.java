package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.controller.WatchDogService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class VerifyWeekNumberStartDateJob implements IemdmQuartzJob {
  private final WatchDogService watchDogService;

  public VerifyWeekNumberStartDateJob(WatchDogService watchDogService) {
    this.watchDogService = watchDogService;
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    this.watchDogService.verifyWeekNumberStartDate();
  }

  @Override
  public String getDescription() {
    return "Verify if start of production year is correct otherwise notify admins";
  }
}
