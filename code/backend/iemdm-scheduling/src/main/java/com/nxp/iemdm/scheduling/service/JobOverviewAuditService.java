package com.nxp.iemdm.scheduling.service;

import com.nxp.iemdm.enums.quartz.JobAction;
import com.nxp.iemdm.model.scheduling.JobOverview;
import com.nxp.iemdm.shared.repository.jpa.JobOverviewRepository;
import java.util.List;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Log
@Service
public class JobOverviewAuditService {
  private final JobOverviewRepository jobOverviewRepository;

  public JobOverviewAuditService(JobOverviewRepository jobOverviewRepository) {
    this.jobOverviewRepository = jobOverviewRepository;
  }

  void create(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.CREATE, wbi);
  }

  void edit(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.EDIT, wbi);
  }

  void delete(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.DELETE, wbi);
  }

  void execute(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.EXECUTE, wbi);
  }

  void pause(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.PAUSE, wbi);
  }

  void resume(JobOverview jobOverview, String wbi) {
    this.save(jobOverview, JobAction.RESUME, wbi);
  }

  List<JobOverview> get(String jobName, String triggerName) {
    if (StringUtils.hasText(triggerName)) {
      return this.jobOverviewRepository.findAllByJobNameAndTriggerNameOrderByIdDesc(
          jobName, triggerName);
    } else {
      return this.jobOverviewRepository.findAllByJobNameOrderByIdDesc(jobName);
    }
  }

  private void save(JobOverview jobOverview, JobAction action, String wbi) {
    jobOverview.setAction(action.toString());
    jobOverview.setUpdatedBy(wbi);
    this.jobOverviewRepository.save(jobOverview);
  }
}
