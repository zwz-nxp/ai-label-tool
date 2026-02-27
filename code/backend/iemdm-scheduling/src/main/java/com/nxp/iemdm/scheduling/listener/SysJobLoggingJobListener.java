package com.nxp.iemdm.scheduling.listener;

import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.intf.operational.SysJobLogService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

@Slf4j
public class SysJobLoggingJobListener extends ListenerBase implements JobListener {

  public static final String NAME = SysJobLoggingJobListener.class.getSimpleName();
  public static final String SYS_JOB_LOG_JOB_NAME_PREFIX = "Quartz_";
  private static final String CONTEXT_KEY_STARTED_AT = "startedAt";
  private static final int MAX_LENGTH_SYS_JOB_LOG_JOB_NAME = 100;
  private static final int MAX_LENGTH_SYS_JOB_LOG_INFO = 1000;

  private final SysJobLogService sysJobLogService;

  public SysJobLoggingJobListener(SysJobLogService sysJobLogService) {
    this.sysJobLogService = sysJobLogService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    Instant startInstant = Instant.now();
    String triggeredByInfo =
        this.getTriggeredBy(context).map(userWbi -> "Triggered by " + userWbi).orElse("");
    SysJobLog sysJobLog = this.createJobLog(context, startInstant, "Start job; " + triggeredByInfo);
    context.put(CONTEXT_KEY_STARTED_AT, startInstant);
    this.sysJobLogService.saveAsync(sysJobLog);
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    Instant vetoedInstant = Instant.now();
    String triggeredByInfo =
        this.getTriggeredBy(context).map(triggerer -> "Triggered by " + triggerer).orElse("");
    SysJobLog sysJobLog =
        this.createJobLog(context, vetoedInstant, "Job execution was vetoed; " + triggeredByInfo);
    this.sysJobLogService.saveAsync(sysJobLog);
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    Instant endInstant = Instant.now();
    String logMessage;
    if (jobException == null) {
      logMessage = this.finishedMessage(context, endInstant);
    } else {
      logMessage = this.failedMessage(context, jobException, endInstant);
    }
    SysJobLog sysJobLog = this.createJobLog(context, endInstant, logMessage);
    this.sysJobLogService.saveAsync(sysJobLog);
  }

  private SysJobLog createJobLog(JobExecutionContext context, Instant instant, String logMessage) {
    SysJobLog sysJobLog = new SysJobLog();
    sysJobLog.setTimestamp(instant);
    sysJobLog.setJobName(this.jobName(context));
    if (logMessage != null && logMessage.length() > MAX_LENGTH_SYS_JOB_LOG_INFO) {
      log.warn(
          "SysJobLog info exceeds {} characters, logging truncated version of: \"{}\"",
          MAX_LENGTH_SYS_JOB_LOG_INFO,
          logMessage);
      String truncatedLogMessage = logMessage.substring(0, MAX_LENGTH_SYS_JOB_LOG_INFO);
      sysJobLog.setLogMessage(truncatedLogMessage);
    } else {
      sysJobLog.setLogMessage(logMessage);
    }
    return sysJobLog;
  }

  private String finishedMessage(JobExecutionContext context, Instant endInstant) {
    String message = "End job; ";
    String triggeredByInfo =
        this.getTriggeredBy(context)
            .map(triggerer -> "Triggered by " + triggerer + "; ")
            .orElse("");
    message = message + triggeredByInfo;
    String durationInfo = this.durationInfo(context, endInstant);
    return message + durationInfo;
  }

  private String failedMessage(
      JobExecutionContext context, JobExecutionException jobException, Instant endInstant) {

    String durationInfo = this.durationInfo(context, endInstant);
    String triggeredByInfo =
        this.getTriggeredBy(context).map(trigger -> "Triggered by " + trigger + "; ").orElse("");
    String exceptionMessage = this.exceptionMessage(jobException);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(
        String.format("Error executing job, %s %s", triggeredByInfo, durationInfo));
    if (exceptionMessage != null) {
      stringBuilder.append(" - caused by: ");
      stringBuilder.append(exceptionMessage);
    }
    return stringBuilder.toString();
  }

  private String jobName(JobExecutionContext context) {
    String quartzJobName = this.getJobName(context);
    String jobName = SYS_JOB_LOG_JOB_NAME_PREFIX + quartzJobName;
    if (jobName.length() > MAX_LENGTH_SYS_JOB_LOG_JOB_NAME) {
      String truncatedJobName = jobName.substring(0, MAX_LENGTH_SYS_JOB_LOG_JOB_NAME);
      log.warn(
          "Quartz Job \"{}\" with prefix \"{}\" exceeds {} characters available for SysJobLog, using: {}",
          quartzJobName,
          SYS_JOB_LOG_JOB_NAME_PREFIX,
          MAX_LENGTH_SYS_JOB_LOG_JOB_NAME,
          truncatedJobName);
      return truncatedJobName;
    }
    return jobName;
  }

  private String durationInfo(JobExecutionContext context, Instant endInstant) {
    return this.duration(context, endInstant)
        .map(duration -> String.format("duration: %s", duration))
        .orElse("duration: N/A");
  }

  private Optional<Duration> duration(JobExecutionContext context, Instant endInstant) {
    return this.startInstant(context)
        .map(startInstant -> Duration.between(startInstant, endInstant));
  }

  private Optional<Instant> startInstant(JobExecutionContext context) {
    Object startedAtFromJobDataMap = context.get(CONTEXT_KEY_STARTED_AT);
    if (startedAtFromJobDataMap instanceof Instant) {
      return Optional.of((Instant) startedAtFromJobDataMap);
    } else if (startedAtFromJobDataMap != null) {
      log.error(
          "JobExecutionContext contains object at key \"{}\" that is not an Instant: {}",
          CONTEXT_KEY_STARTED_AT,
          startedAtFromJobDataMap.getClass());
    }
    return Optional.empty();
  }
}
