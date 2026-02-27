package com.nxp.iemdm.scheduling.listener;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

@Slf4j
public class JobUpdateListenerListener extends ListenerBase implements JobListener {

  public static final String NAME = JobUpdateListenerListener.class.getSimpleName();

  private final UpdateService updateService;

  public JobUpdateListenerListener(UpdateService updateService) {
    this.updateService = updateService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    this.getTriggeredBy(context)
        .ifPresent(
            triggeredBy -> {
              String triggerName = this.getTriggerNameIfCronJob(context);
              this.createUpdate(triggerName, "execute", triggeredBy);
            });
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    this.getTriggeredBy(context)
        .ifPresent(
            triggeredBy -> {
              String triggerName = this.getTriggerNameIfCronJob(context);
              this.createUpdate(triggerName, "veto", triggeredBy);
            });
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    this.getTriggeredBy(context)
        .ifPresent(
            triggeredBy -> {
              String triggerName = this.getTriggerNameIfCronJob(context);
              if (jobException == null) {
                this.createUpdate(triggerName, "success", triggeredBy);
              } else {
                this.createUpdate(triggerName, "failed", triggeredBy);
              }
            });
  }

  private void createUpdate(String name, String action, String user) {
    Update update = new Update(UpdateType.USER_ALERT, 0, String.join(";", name, action), user);
    this.updateService.update(update);
  }
}
