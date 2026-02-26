package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.model.logging.SysJobLog;

public interface SysJobLogService {
  void saveAsync(SysJobLog sysJobLog);
}
