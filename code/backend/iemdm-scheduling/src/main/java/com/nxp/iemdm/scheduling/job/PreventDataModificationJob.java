package com.nxp.iemdm.scheduling.job;

import com.nxp.iemdm.shared.intf.operational.AuthorizationAdapter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

@DisallowConcurrentExecution
public class PreventDataModificationJob implements IemdmQuartzJob {

  private final AuthorizationAdapter authorizationAdapter;

  public PreventDataModificationJob(AuthorizationAdapter authorizationAdapter) {
    this.authorizationAdapter = authorizationAdapter;
  }

  @Override
  public void execute(JobExecutionContext context) {
    this.authorizationAdapter.restrictToReadOnlyAccess();
  }

  @Override
  public String getDescription() {
    return "Prevents users to change items in the IEMDM application";
  }
}
