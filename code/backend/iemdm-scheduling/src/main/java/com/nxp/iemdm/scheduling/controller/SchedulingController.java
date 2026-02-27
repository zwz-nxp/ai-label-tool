package com.nxp.iemdm.scheduling.controller;

import com.nxp.iemdm.enums.quartz.JobAction;
import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.model.scheduling.JobExecutionLogRequest;
import com.nxp.iemdm.model.scheduling.JobOverview;
import com.nxp.iemdm.scheduling.service.JobExecutionLogService;
import com.nxp.iemdm.scheduling.service.SchedulingService;
import com.nxp.iemdm.scheduling.util.TimeZoneUtil;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.dto.scheduling.JobExecutionLogDto;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
  private final JobExecutionLogService jobExecutionLogService;
  private final SchedulingService schedulingService;

  public SchedulingController(
      JobExecutionLogService jobExecutionLogService, SchedulingService schedulingService) {
    this.jobExecutionLogService = jobExecutionLogService;
    this.schedulingService = schedulingService;
  }

  @GetMapping("/scheduled-job-overviews")
  public List<JobOverview> jobOverviews() {
    try {
      return this.schedulingService.getJobOverviews();
    } catch (SchedulerException | ClassCastException exception) {
      log.error(String.format("Could not query all scheduled jobs: %s", exception));
      throw new BadRequestException(
          "Could not query all scheduled jobs, report this as an incident.");
    }
  }

  @GetMapping("/available-jobs")
  public List<JobOverview> getAvailableJobs() {
    return this.schedulingService.getAvailableJobs();
  }

  @PostMapping(path = "/check-cron")
  public Boolean checkCronExpression(@RequestBody String cronExpression) {
    return CronExpression.isValidExpression(cronExpression);
  }

  @PostMapping("/jobDetail")
  public ResponseEntity<String> createJobDetail(
      @RequestBody JobOverview jobOverview,
      @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    Job job;
    try {
      job = this.schedulingService.getJob(jobOverview);
    } catch (ClassNotFoundException classNotFoundException) {
      log.error(String.format("CreateJobDetailAndTrigger: %s", classNotFoundException));
      return new ResponseEntity<>(
          "Could not create a new job. If you see this error please report it as an incident as soon as possible",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    try {
      this.schedulingService.createOrUpdateJob(job, jobOverview, wbi);
    } catch (SchedulerException schedulerException) {
      // This also happens if the job already exists.
      log.error(String.format("Could not add job: %s", schedulerException));
      return new ResponseEntity<>(
          String.format(
              "Could not add job with name '%s'. (It might already exist)",
              jobOverview.getJobName()),
          HttpStatus.BAD_REQUEST);
    }
    if (StringUtils.hasText(jobOverview.getTriggerName())) {
      try {
        this.schedulingService.createOrUpdateTrigger(jobOverview, wbi);
      } catch (IllegalArgumentException illegalArgumentException) {
        return new ResponseEntity<>(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
      } catch (SchedulerException schedulerException) {
        log.error(
            "Failed to create or update trigger: {}",
            jobOverview.getTriggerName(),
            schedulerException);
        return new ResponseEntity<>(
            String.format(
                "Could not create or update trigger: %s. Error: %s",
                jobOverview.getTriggerName(), schedulerException.getMessage()),
            HttpStatus.BAD_REQUEST);
      }
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/trigger/{name}")
  public ResponseEntity<Boolean> deleteTrigger(
      @PathVariable String name, @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    try {
      this.schedulingService.deleteTrigger(name, wbi);
    } catch (Exception exception) {
      log.error(String.format("Trigger with name '%s', could not be deleted", name), exception);
      return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @GetMapping("/trigger/pause/{name}")
  public ResponseEntity<Boolean> pauseByTriggerName(
      @PathVariable String name, @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    try {
      this.schedulingService.pauseTrigger(name, wbi);
      return new ResponseEntity<>(true, HttpStatus.OK);
    } catch (SchedulerException schedulerException) {
      log.error(
          String.format("Trigger with name '%s', could not be found and was not paused", name),
          schedulerException);
      return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/trigger/resume/{name}")
  public ResponseEntity<Boolean> resumeByTriggerName(
      @PathVariable String name, @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    try {
      this.schedulingService.resumeTrigger(name, wbi);
      return new ResponseEntity<>(true, HttpStatus.OK);
    } catch (SchedulerException schedulerException) {
      log.error(
          String.format("Trigger with name '%s', could not be found and was not resumed", name),
          schedulerException);
      return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/job-overview-audit/{jobName}/{triggerName}")
  public List<JobOverview> jobOverviewAudit(
      @PathVariable String jobName, @PathVariable String triggerName) {
    return this.schedulingService.getJobOverviews(jobName, triggerName).stream()
        .filter(
            jobOverview -> !jobOverview.getAction().equalsIgnoreCase(JobAction.EXECUTE.toString()))
        .collect(Collectors.toList());
  }

  @GetMapping("/job-overview-audit/{jobName}")
  public List<JobOverview> jobOverviewAudit(String jobName) {
    return this.schedulingService.getJobOverviews(jobName, null);
  }

  @PostMapping("/job-execution-logs")
  public List<JobExecutionLogDto> jobExecutionLogs(
      @RequestBody JobExecutionLogRequest jobExecutionLogRequest) {

    return this.jobExecutionLogService.jobExecutionLogs(
        jobExecutionLogRequest.getJobName(),
        jobExecutionLogRequest.getTimestampUpperBound() == null
            ? Instant.now()
            : jobExecutionLogRequest
                .getTimestampUpperBound()
                .atZone(TimeZoneUtil.TIME_ZONE_NL.toZoneId())
                .toInstant(),
        jobExecutionLogRequest.getMaxResults() == null
            ? 20
            : jobExecutionLogRequest.getMaxResults());
  }

  @PostMapping("/run-job")
  public Boolean runJob(
      @RequestBody JobOverview jobOverview,
      @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    try {
      this.schedulingService.runJob(jobOverview, wbi);
      return true;
    } catch (SchedulerException schedulerException) {
      log.error(String.format("Could not run job: %s", schedulerException));
      return false;
    }
  }
}
