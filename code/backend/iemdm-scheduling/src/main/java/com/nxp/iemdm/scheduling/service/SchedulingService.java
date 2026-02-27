package com.nxp.iemdm.scheduling.service;

import com.nxp.iemdm.model.scheduling.JobOverview;
import com.nxp.iemdm.scheduling.job.IemdmQuartzJob;
import com.nxp.iemdm.scheduling.util.ConversionUtil;
import com.nxp.iemdm.shared.repository.jpa.PersonRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SchedulingService {
  private final List<IemdmQuartzJob> jobs;
  private final Scheduler scheduler;
  private final JobOverviewAuditService jobOverviewAuditService;
  private final PersonRepository personRepository;

  public SchedulingService(
      List<IemdmQuartzJob> jobs,
      Scheduler scheduler,
      JobOverviewAuditService jobOverviewAuditService,
      PersonRepository personRepository) {
    this.jobs = jobs;
    this.scheduler = scheduler;
    this.jobOverviewAuditService = jobOverviewAuditService;
    this.personRepository = personRepository;
  }

  public List<JobOverview> getAvailableJobs() {
    List<JobOverview> jobOverviews = new ArrayList<>();
    this.jobs.forEach(
        job -> {
          JobOverview jobOverview = new JobOverview();
          jobOverview.setJobName(ClassUtils.getUserClass(job).getSimpleName());
          jobOverview.setJobDescription(job.getDescription());
          try {
            JobDetail jobDetail =
                this.scheduler.getJobDetail(
                    JobKey.jobKey(ClassUtils.getUserClass(job).getSimpleName()));
            if (StringUtils.hasText(jobDetail.getDescription())) {
              jobOverview.setJobDescription(jobDetail.getDescription());
            }
          } catch (SchedulerException e) {
            log.error(e.getMessage());
          }

          jobOverviews.add(jobOverview);
        });
    return jobOverviews;
  }

  @PostConstruct
  private void init() {
    for (IemdmQuartzJob job : this.jobs) {
      try {
        String key = ClassUtils.getUserClass(job).getSimpleName();
        String description = job.getDescription();
        JobKey jobKey = JobKey.jobKey(key);
        if (!this.scheduler.checkExists(jobKey)) {
          this.createJob(job, key, description);
        } else {
          this.unScheduleJobIfNotDisplayed(job);
        }
      } catch (SchedulerException schedulerException) {
        String message =
            String.format(
                "Unable to initialize job with name %s",
                ClassUtils.getUserClass(job).getSimpleName());
        log.error(message, schedulerException);
      }
    }
  }

  private void unScheduleJobIfNotDisplayed(IemdmQuartzJob job) throws SchedulerException {
    if (job.isDisplayed()) {
      return;
    }
    String key = ClassUtils.getUserClass(job).getSimpleName();
    JobKey jobKey = JobKey.jobKey(key);
    List<TriggerKey> triggersToBeUnscheduled =
        this.scheduler.getTriggerKeys(GroupMatcher.anyGroup()).stream()
            .map(this::getTrigger)
            .filter(Objects::nonNull)
            .filter(trigger -> jobKey.equals(trigger.getJobKey()))
            .map(Trigger::getKey)
            .collect(Collectors.toList());
    if (triggersToBeUnscheduled.isEmpty()) {
      return;
    }

    try {
      this.scheduler.unscheduleJobs(triggersToBeUnscheduled);
    } catch (SchedulerException schedulerException) {
      String message = String.format("Unable to unschedule job with name %s", key);
      log.error(message, schedulerException);
    }
  }

  public Job getJob(JobOverview jobOverview) throws ClassNotFoundException {
    return jobs.stream()
        .filter(
            searchJob ->
                jobOverview
                    .getClassName()
                    .equalsIgnoreCase(ClassUtils.getUserClass(searchJob).getSimpleName()))
        .findFirst()
        .orElseThrow(ClassNotFoundException::new);
  }

  public void createOrUpdateJob(Job job, JobOverview jobOverview, String user)
      throws SchedulerException {
    JobDetail jobDetail = this.scheduler.getJobDetail(JobKey.jobKey(jobOverview.getJobName()));
    if (jobDetail == null) {
      this.createJob(job, jobOverview, user);
    } else {
      this.updateJob(job, jobOverview, user);
    }
  }

  public void createOrUpdateTrigger(JobOverview jobOverview, String user)
      throws IllegalArgumentException, SchedulerException {
    if (!CronExpression.isValidExpression(jobOverview.getCronExpression())) {
      throw new IllegalArgumentException("Cron expression is not valid");
    }
    CronTrigger existingTrigger =
        (CronTrigger)
            this.scheduler.getTrigger(TriggerKey.triggerKey(jobOverview.getTriggerName()));
    if (existingTrigger == null) {
      this.addTrigger(jobOverview, user);
    } else {
      this.updateTrigger(jobOverview, existingTrigger, user);
    }
  }

  public List<JobOverview> getJobOverviews() throws SchedulerException {
    List<JobOverview> scheduledJobs = new ArrayList<>();
    for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.anyGroup())) {
      scheduledJobs.addAll(
          this.jobs.stream()
              .filter(IemdmQuartzJob::isDisplayed)
              .filter(
                  job ->
                      jobKey
                          .getName()
                          .equalsIgnoreCase(ClassUtils.getUserClass(job).getSimpleName()))
              .flatMap(job -> this.getJobOverviewFromJobKey(jobKey).stream())
              .toList());
    }
    return scheduledJobs;
  }

  public List<JobOverview> getJobOverviews(String jobName, String triggerName) {
    return this.jobOverviewAuditService.get(jobName, triggerName);
  }

  public void runJob(JobOverview jobOverview, String user) throws SchedulerException {
    JobKey jobKey = JobKey.jobKey(jobOverview.getJobName());
    JobDataMap jobData = new JobDataMap();
    String name = this.personRepository.findByWbiIgnoreCase(user).orElseThrow().getName();
    jobData.put("triggeredBy", String.format("User (%s - %s)", name, user));
    this.scheduler.triggerJob(jobKey, jobData);
    this.jobOverviewAuditService.execute(jobOverview, user);
  }

  public void deleteTrigger(String name, String user) throws SchedulerException {
    TriggerKey triggerKey = TriggerKey.triggerKey(name);
    CronTrigger trigger = (CronTrigger) this.scheduler.getTrigger(triggerKey);
    JobOverview jobOverview = this.getJobOverviewFromTrigger(trigger);
    this.scheduler.unscheduleJob(triggerKey);
    if (jobOverview != null) {
      this.jobOverviewAuditService.delete(jobOverview, user);
    }
  }

  public void pauseTrigger(String name, String user) throws SchedulerException {
    TriggerKey triggerKey = TriggerKey.triggerKey(name);
    CronTrigger trigger = (CronTrigger) this.scheduler.getTrigger(triggerKey);
    this.scheduler.pauseTrigger(trigger.getKey());
    JobOverview jobOverview = this.getJobOverviewFromTrigger(trigger);
    if (jobOverview != null) {
      this.jobOverviewAuditService.pause(jobOverview, user);
    }
  }

  public void pauseAllTriggers() throws SchedulerException {
    this.scheduler.pauseAll();
  }

  public void resumeTrigger(String name, String user) throws SchedulerException {
    CronTrigger trigger = (CronTrigger) this.scheduler.getTrigger(TriggerKey.triggerKey(name));
    this.scheduler.resumeTrigger(trigger.getKey());
    JobOverview jobOverview = this.getJobOverviewFromTrigger(trigger);
    if (jobOverview != null) {
      this.jobOverviewAuditService.resume(jobOverview, user);
    }
  }

  public void resumeAllTriggers() throws SchedulerException {
    this.scheduler.resumeAll();
  }

  private void createJob(Job job, String name, String description) throws SchedulerException {
    try {
      // these are always of class<? extends Job> (Quartz Jobs)
      @SuppressWarnings("unchecked")
      Class<? extends Job> jobClass = (Class<? extends Job>) ClassUtils.getUserClass(job);
      JobDetail jobDetail =
          JobBuilder.newJob()
              .ofType(jobClass)
              .storeDurably()
              .withIdentity(name)
              .withDescription(description)
              .build();
      this.scheduler.addJob(jobDetail, false, false);

    } catch (SchedulerException schedulerException) {
      log.error(
          String.format(
              "Exception occurred when trying to update description for job with name: '%s', %s",
              name, schedulerException));
      throw schedulerException;
    }
  }

  private void createJob(Job job, JobOverview jobOverview, String user) throws SchedulerException {
    this.createJob(job, jobOverview.getJobName(), jobOverview.getJobDescription());
    if (!StringUtils.hasText(jobOverview.getTriggerName())) {
      this.jobOverviewAuditService.create(jobOverview, user);
    }
  }

  private void updateJob(Job job, JobOverview jobOverview, String user) throws SchedulerException {
    try {
      JobDetail jobDetail =
          JobBuilder.newJob()
              .ofType(job.getClass())
              .storeDurably()
              .withIdentity(jobOverview.getJobName())
              .withDescription(jobOverview.getJobDescription())
              .build();
      this.scheduler.addJob(jobDetail, true);
      if (!StringUtils.hasText(jobOverview.getTriggerName())) {
        this.jobOverviewAuditService.edit(jobOverview, user);
      }
    } catch (SchedulerException schedulerException) {
      log.error(
          String.format(
              "Exception occurred when trying to update description for job with name: '%s', %s",
              jobOverview.getJobName(), schedulerException));
      throw schedulerException;
    }
  }

  private void addTrigger(JobOverview jobOverview, String user) throws SchedulerException {
    CronTrigger trigger =
        TriggerBuilder.newTrigger()
            .forJob(JobKey.jobKey(jobOverview.getJobName()))
            .withIdentity(jobOverview.getTriggerName())
            .withSchedule(
                CronScheduleBuilder.cronSchedule(jobOverview.getCronExpression())
                    .inTimeZone(TimeZone.getTimeZone(jobOverview.getTimeZone()))
                    .withMisfireHandlingInstructionDoNothing())
            .withDescription(jobOverview.getTriggerDescription())
            .usingJobData(
                "triggeredBy", String.format("Schedule (%s)", jobOverview.getTriggerName()))
            .build();

    this.scheduler.scheduleJob(trigger);
    this.jobOverviewAuditService.create(jobOverview, user);
  }

  private void updateTrigger(JobOverview jobOverview, CronTrigger existingTrigger, String user)
      throws SchedulerException {
    CronTrigger newTrigger =
        TriggerBuilder.newTrigger()
            .forJob(JobKey.jobKey(jobOverview.getJobName()))
            .withIdentity(existingTrigger.getKey())
            .withSchedule(
                CronScheduleBuilder.cronSchedule(jobOverview.getCronExpression())
                    .inTimeZone(TimeZone.getTimeZone(jobOverview.getTimeZone()))
                    .withMisfireHandlingInstructionDoNothing())
            .withDescription(jobOverview.getTriggerDescription())
            .usingJobData(
                "triggeredBy", String.format("Schedule (%s)", existingTrigger.getKey().getName()))
            .build();
    this.scheduler.unscheduleJob(existingTrigger.getKey());
    this.scheduler.scheduleJob(newTrigger);
    JobOverview newJobOverview = this.getJobOverviewFromTrigger(newTrigger);
    if (newJobOverview == null) {
      newJobOverview = jobOverview;
    }
    this.jobOverviewAuditService.edit(newJobOverview, user);
  }

  private JobOverview getJobOverviewFromTrigger(CronTrigger trigger) {
    try {
      JobKey jobKey = trigger.getJobKey();
      JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
      String triggerState = this.scheduler.getTriggerState(trigger.getKey()).name();
      return ConversionUtil.convert(jobDetail, trigger, triggerState);
    } catch (SchedulerException schedulerException) {
      log.error(
          "Cannot convert trigger to JobOverview for {}",
          trigger.getKey().getName(),
          schedulerException);
    } catch (NullPointerException nullPointerException) {
      log.error("Trigger did not exist", nullPointerException);
    }
    return null;
  }

  private List<JobOverview> getJobOverviewFromJobKey(JobKey jobKey) {
    try {
      JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
      List<CronTrigger> triggers =
          this.scheduler.getTriggersOfJob(jobKey).stream()
              .filter(CronTrigger.class::isInstance)
              .map(CronTrigger.class::cast)
              .toList();

      if (triggers.isEmpty()) {
        return List.of(ConversionUtil.convert(jobDetail));
      } else {
        List<JobOverview> result = new ArrayList<>();
        for (CronTrigger trigger : triggers) {
          String triggerState = this.scheduler.getTriggerState(trigger.getKey()).name();
          result.add(ConversionUtil.convert(jobDetail, trigger, triggerState));
        }
        return result;
      }
    } catch (SchedulerException schedulerException) {
      return List.of();
    }
  }

  /**
   * Tries to get trigger by key. If no trigger found or {@link org.quartz.SchedulerException} is
   * thrown, it returns null
   *
   * @param triggerKey trigger key
   * @return trigger if found, else null
   */
  private Trigger getTrigger(TriggerKey triggerKey) {
    try {
      return this.scheduler.getTrigger(triggerKey);
    } catch (SchedulerException e) {
      return null;
    }
  }
}
