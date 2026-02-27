package com.nxp.iemdm.capacitystatementservice.service.rest;

import com.nxp.iemdm.capacitystatementservice.service.SysJobLogService;
import com.nxp.iemdm.model.logging.SysJobLog;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SysJobLogServiceREST
    implements SysJobLogService, com.nxp.iemdm.shared.intf.operational.SysJobLogService {

  private final RestTemplate restTemplate;
  private final String iemdmServicesUri;

  @Autowired
  public SysJobLogServiceREST(
      RestTemplate restTemplate, @Value("${rest.sysjoblogservice.uri}") String locationServiceURI) {

    this.restTemplate = restTemplate;
    this.iemdmServicesUri = locationServiceURI;
  }

  @Override
  public SysJobLog save(SysJobLog sysJobLog) {

    String uri = iemdmServicesUri + "/sysjoblog/save";
    ResponseEntity<SysJobLog> responseEntity =
        this.restTemplate.postForEntity(uri, sysJobLog, SysJobLog.class, new HashMap<>());

    return responseEntity.getBody();
  }

  @Override
  public void saveAsync(SysJobLog sysJobLog) {
    this.save(sysJobLog);
  }
}
