package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.intf.operational.SysJobLogService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SysJobLogServiceREST implements SysJobLogService {

  private final RestTemplate restTemplate;
  private final String iemdmServicesUri;

  public SysJobLogServiceREST(
      RestTemplate restTemplate, @Value("${rest.syncservice.uri}") String iemdmServicesUri) {
    this.restTemplate = restTemplate;
    this.iemdmServicesUri = iemdmServicesUri;
  }

  @Async("asyncExecutor")
  @Override
  public void saveAsync(SysJobLog sysJobLog) {
    String uri = this.iemdmServicesUri + "sysjoblog/save";
    restTemplate.postForEntity(uri, sysJobLog, SysJobLog.class, Map.of());
  }
}
