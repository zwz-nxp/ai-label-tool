package com.nxp.iemdm.scheduling.service;

import com.nxp.iemdm.scheduling.listener.SysJobLoggingJobListener;
import com.nxp.iemdm.scheduling.util.TimeZoneUtil;
import com.nxp.iemdm.shared.dto.scheduling.JobExecutionLogDto;
import com.nxp.iemdm.shared.repository.jpa.SysJobLogRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class JobExecutionLogService {

  private final SysJobLogRepository sysJobLogRepository;

  public JobExecutionLogService(SysJobLogRepository sysJobLogRepository) {
    this.sysJobLogRepository = sysJobLogRepository;
  }

  public List<JobExecutionLogDto> jobExecutionLogs(
      String jobName, Instant upperBound, int maxResults) {
    String fullJobName = SysJobLoggingJobListener.SYS_JOB_LOG_JOB_NAME_PREFIX + jobName;
    return this.sysJobLogRepository
        .findAllByJobNameBeforeTimestampLimitToMaxResults(fullJobName, upperBound, maxResults)
        .stream()
        .map(
            sysJobLog ->
                new JobExecutionLogDto(
                    this.dutchLocalDateTime(sysJobLog.getTimestamp()), sysJobLog.getLogMessage()))
        .collect(Collectors.toList());
  }

  private LocalDateTime dutchLocalDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, TimeZoneUtil.TIME_ZONE_NL.toZoneId());
  }
}
