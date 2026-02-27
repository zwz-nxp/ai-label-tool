package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.logging.SysJobLog;

public interface SysJobLogService {
  String START_JOB = "Start job";
  String END_JOB = "End job";

  SysJobLog save(SysJobLog sysJobLog);
}
