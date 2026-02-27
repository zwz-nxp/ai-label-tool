package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.SysJobLogRepoService;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/sysjoblog")
public class SysJobLogService {

  private final SysJobLogRepoService sysJobLogRepoService;

  @Autowired
  public SysJobLogService(SysJobLogRepoService sysJobLogRepoService) {
    this.sysJobLogRepoService = sysJobLogRepoService;
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/save",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public SysJobLog saveSysJobLogRepoService(@RequestBody SysJobLog sysJobLog) {
    return sysJobLogRepoService.save(sysJobLog);
  }
}
