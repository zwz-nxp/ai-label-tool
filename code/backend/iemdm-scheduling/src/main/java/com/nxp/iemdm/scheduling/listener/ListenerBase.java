package com.nxp.iemdm.scheduling.listener;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@Slf4j
public class ListenerBase {
  public static final String JOB_DATA_MAP_KEY_TRIGGERED_BY = "triggeredBy";

  protected String getJobName(JobExecutionContext context) {
    return context.getJobDetail().getKey().getName();
  }

  protected String getTriggerName(JobExecutionContext context) {
    return context.getTrigger().getKey().getName();
  }

  protected String getTriggerNameIfCronJob(JobExecutionContext context) {
    Trigger trigger = context.getTrigger();
    if (trigger instanceof CronTrigger) {
      return this.getTriggerName(context);
    }
    return this.getJobName(context);
  }

  protected Optional<String> getTriggeredBy(JobExecutionContext context) {
    Object triggeredBy = context.getMergedJobDataMap().get(JOB_DATA_MAP_KEY_TRIGGERED_BY);
    if (triggeredBy instanceof String) {
      return Optional.of((String) triggeredBy);
    } else if (triggeredBy != null) {
      log.error(
          "MergedJobDataMap from JobExecutionContext contains object at key \"{}\" that is not a String: {}",
          JOB_DATA_MAP_KEY_TRIGGERED_BY,
          triggeredBy.getClass());
    }
    return Optional.empty();
  }

  protected String exceptionMessage(JobExecutionException jobException) {
    if (jobException == null) {
      return null;
    }
    Throwable cause = jobException.getCause();
    if (cause instanceof SchedulerException) {
      cause = cause.getCause();
    }
    if (cause != null && jobException.getMessage() != null) {
      String message = cause.getMessage();
      if (message.contains(cause.getClass().getSimpleName())) {
        return message;
      }
      return cause.getClass().getSimpleName() + ": " + message;
    }
    return jobException.getMessage();
  }
}
