package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.operational.AuthorizationAdapter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

@DisallowConcurrentExecution
public class AllowDataModificationJob implements IemdmQuartzJob {

  private final AuthorizationAdapter authorizationAdapter;

  public AllowDataModificationJob(AuthorizationAdapter authorizationAdapter) {
    this.authorizationAdapter = authorizationAdapter;
  }

  @Override
  public void execute(JobExecutionContext context) {
    this.authorizationAdapter.removeReadOnlyRestriction();
  }

  @Override
  public String getDescription() {
    return "Allows users to change items in the IEMDM application";
  }
}
