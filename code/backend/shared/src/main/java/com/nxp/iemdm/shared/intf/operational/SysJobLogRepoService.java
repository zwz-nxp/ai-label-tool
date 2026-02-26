package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.model.logging.SysJobLog;
import java.util.Set;

public interface SysJobLogRepoService {

  SysJobLog save(SysJobLog sysJobLog);

  Set<String> getDistinctTrackingIds(String jobName);

  void logJob(String jobName, String action);
}
