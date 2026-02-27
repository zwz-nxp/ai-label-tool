package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import com.nxp.iemdm.service.VersionInfoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
@Service
public class VersionInfoServiceREST implements VersionInfoService {

  private final RestTemplate restTemplate;
  private final String syncServiceUri;

  @Autowired
  public VersionInfoServiceREST(
      RestTemplate restTemplate, @Value("${rest.syncservice.uri}") String syncServiceUri) {
    this.restTemplate = restTemplate;
    this.syncServiceUri = syncServiceUri;
  }

  @Override
  public VersionInfo getServicesVersionInfo() {
    String uri = this.syncServiceUri + "/version/services";
    ResponseEntity<VersionInfo> responseEntity =
        this.restTemplate.exchange(uri, HttpMethod.GET, null, VersionInfo.class);
    return responseEntity.getBody();
  }

  @Override
  public List<VersionInfo> getAllVersionInfos() {
    String uri = this.syncServiceUri + "/version/allinfos";
    return this.restTemplate
        .exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<VersionInfo>>() {})
        .getBody();
  }
}
