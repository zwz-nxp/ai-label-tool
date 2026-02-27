package com.nxp.iemdm.scheduling.util;

import com.nxp.iemdm.model.scheduling.JobOverview;
import java.time.LocalDateTime;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;

public class ConversionUtil {

  /* Used when there are no triggers for a job. */
  public static JobOverview convert(JobDetail jobDetail) {
    var jobOverview = new JobOverview();

    jobOverview.setJobName(jobDetail.getKey().getName());
    jobOverview.setJobDescription(jobDetail.getDescription());
    jobOverview.setClassName(jobDetail.getJobClass().getName());

    return jobOverview;
  }

  public static JobOverview convert(JobDetail jobDetail, CronTrigger trigger, String triggerState) {
    var jobOverview = new JobOverview();

    jobOverview.setJobName(jobDetail.getKey().getName());
    jobOverview.setJobDescription(jobDetail.getDescription());
    jobOverview.setClassName(jobDetail.getJobClass().getSimpleName());
    jobOverview.setTriggerName(trigger.getKey().getName());
    jobOverview.setCronExpression(trigger.getCronExpression());
    jobOverview.setTriggerDescription(trigger.getDescription());
    jobOverview.setTimeZone(trigger.getTimeZone().getID());
    jobOverview.setTriggerState(triggerState);
    jobOverview.setPreviousFireTime(
        convertToLocalDateTimeViaInstant(trigger.getPreviousFireTime()));
    jobOverview.setNextFireTime(convertToLocalDateTimeViaInstant(trigger.getNextFireTime()));

    return jobOverview;
  }

  private static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
    // Previous fire time may be null
    if (dateToConvert == null) {
      return null;
    } else {
      return dateToConvert
          .toInstant()
          .atZone(TimeZoneUtil.TIME_ZONE_NL.toZoneId())
          .toLocalDateTime();
    }
  }
}
