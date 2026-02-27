package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.controller.ErrorService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class MailExceptionsJob implements IemdmQuartzJob {
  private final ErrorService errorService;

  public MailExceptionsJob(ErrorService errorService) {
    this.errorService = errorService;
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    this.errorService.mailExceptions();
  }

  @Override
  public String getDescription() {
    return "Mail exceptions to global administrators";
  }
}
