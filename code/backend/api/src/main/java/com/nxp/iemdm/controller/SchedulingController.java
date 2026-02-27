package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.scheduling.JobExecutionLogRequest;
import com.nxp.iemdm.model.scheduling.JobOverview;
import com.nxp.iemdm.service.rest.SchedulingServiceREST;
import com.nxp.iemdm.shared.dto.scheduling.JobExecutionLogDto;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/scheduling")
public class SchedulingController {

  private final SchedulingServiceREST schedulingService;
  private final RestTemplate restTemplate;
  private final String iemdmServicesUri;

  public SchedulingController(
      SchedulingServiceREST schedulingService,
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String iemdmServicesUri) {
    this.schedulingService = schedulingService;
    this.restTemplate = restTemplate;
    this.iemdmServicesUri = iemdmServicesUri;
  }

  @GetMapping(path = "/available-jobs")
  public List<JobOverview> getAvailableJobs() {
    String uri = this.iemdmServicesUri + "scheduling/available-jobs";
    return this.restTemplate
        .exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<JobOverview>>() {})
        .getBody();
  }

  @GetMapping(path = "/check-cron")
  public Boolean checkCronExpression(@RequestParam(name = "cronExpression") String cronExpression) {
    String uri = this.iemdmServicesUri + "scheduling/check-cron";
    HttpEntity<String> request = new HttpEntity<>(cronExpression);
    return this.restTemplate.exchange(uri, HttpMethod.POST, request, Boolean.class).getBody();
  }

  @GetMapping(path = "/scheduled-job-overviews")
  public List<JobOverview> scheduledJobOverviews() {
    return this.schedulingService.getScheduledJobOverviews();
  }

  @PostMapping(path = "/jobDetail")
  public ResponseEntity<String> createJobDetail(@RequestBody JobOverview jobOverview) {
    String uri = this.iemdmServicesUri + "scheduling/jobDetail";

    HttpEntity<JobOverview> request = new HttpEntity<>(jobOverview);

    return this.restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
  }

  @DeleteMapping("/jobDetail/{name}")
  public ResponseEntity<String> deleteJob(@PathVariable String name) {
    String uri = this.iemdmServicesUri + "scheduling/jobDetail/" + name;

    return this.restTemplate.exchange(uri, HttpMethod.DELETE, null, String.class);
  }

  // Execute job immediately
  @PutMapping("/jobDetail/trigger/{name}")
  public ResponseEntity<String> triggerJobByName(@PathVariable String jobName) {
    String uri = this.iemdmServicesUri + "scheduling/jobDetail/" + jobName;

    return this.restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);
  }

  @DeleteMapping("/trigger/{name}")
  public ResponseEntity<Boolean> deleteTrigger(@PathVariable String name) {
    String uri = this.iemdmServicesUri + "scheduling/trigger/" + name;

    return this.restTemplate.exchange(uri, HttpMethod.DELETE, null, Boolean.class);
  }

  @PutMapping("/trigger/pause/{name}")
  public ResponseEntity<Boolean> pauseByTriggerName(@PathVariable String name) {
    String uri = this.iemdmServicesUri + "scheduling/trigger/pause/" + name;

    return this.restTemplate.exchange(uri, HttpMethod.GET, null, Boolean.class);
  }

  @GetMapping("/trigger/resume/{name}")
  public ResponseEntity<Boolean> resumeByTriggerName(@PathVariable String name) {
    String uri = this.iemdmServicesUri + "scheduling/trigger/resume/" + name;

    return this.restTemplate.exchange(uri, HttpMethod.GET, null, Boolean.class);
  }

  @PostMapping(path = "/job-execution-logs")
  public List<JobExecutionLogDto> jobExecutionLogs(
      @RequestBody JobExecutionLogRequest jobExecutionLogRequest) {
    return this.schedulingService.jobExecutionLogs(jobExecutionLogRequest);
  }

  @GetMapping("/job-overview-audit/{jobName}/{triggerName}")
  public JobOverview[] jobOverviewAudit(
      @PathVariable String jobName, @PathVariable String triggerName) {
    String uri =
        this.iemdmServicesUri
            + String.format("scheduling/job-overview-audit/%s/%s", jobName, triggerName);
    return this.restTemplate.getForEntity(uri, JobOverview[].class, Map.of()).getBody();
  }

  @GetMapping("/job-overview-audit/{jobName}")
  public JobOverview[] jobOverviewAudit(@PathVariable String jobName) {
    String uri = this.iemdmServicesUri + String.format("scheduling/job-overview-audit/%s", jobName);
    return this.restTemplate.getForEntity(uri, JobOverview[].class, Map.of()).getBody();
  }

  @PostMapping(path = "/run-job")
  public ResponseEntity<Boolean> runJob(@RequestBody JobOverview jobOverview) {
    String uri = this.iemdmServicesUri + "scheduling/run-job";

    HttpEntity<JobOverview> request = new HttpEntity<>(jobOverview);

    return this.restTemplate.exchange(uri, HttpMethod.POST, request, Boolean.class);
  }
}
