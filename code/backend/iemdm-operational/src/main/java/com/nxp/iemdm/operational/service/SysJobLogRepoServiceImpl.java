package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.intf.operational.SysJobLogRepoService;
import com.nxp.iemdm.shared.intf.operational.SysJobLogService;
import com.nxp.iemdm.shared.repository.jpa.SysJobLogRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysJobLogRepoServiceImpl implements SysJobLogRepoService, SysJobLogService {

  private final SysJobLogRepository sysJobLogRepository;

  public SysJobLogRepoServiceImpl(SysJobLogRepository sysJobLogRepository) {
    this.sysJobLogRepository = sysJobLogRepository;
  }

  @Async("asyncExecutor")
  @Override
  public void saveAsync(SysJobLog sysJobLog) {
    this.sysJobLogRepository.save(sysJobLog);
  }

  @Override
  public SysJobLog save(SysJobLog sysJobLog) {
    return this.sysJobLogRepository.save(sysJobLog);
  }

  @Override
  public Set<String> getDistinctTrackingIds(String jobName) {
    return this.sysJobLogRepository.getDistinctTrackingIds(jobName);
  }

  @Override
  public void logJob(String jobName, String action) {
    this.save(new SysJobLog(Instant.now(), jobName, action, null));
  }
}
