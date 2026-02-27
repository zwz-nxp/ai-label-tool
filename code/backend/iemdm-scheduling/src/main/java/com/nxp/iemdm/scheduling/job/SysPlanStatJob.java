package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.operational.SysPlanStatService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class SysPlanStatJob implements IemdmQuartzJob {

  private final SysPlanStatService sysPlanStatService;

  public SysPlanStatJob(SysPlanStatService sysPlanStatService) {
    this.sysPlanStatService = sysPlanStatService;
  }

  @Override
  public String getDescription() {
    return "Clears and populates the Sys Plan Stat Table";
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {}
}
