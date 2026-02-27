package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import com.nxp.iemdm.service.ConfigurationFileService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConfigurationFileServiceREST implements ConfigurationFileService {
  private final RestTemplate restTemplate;
  private final String configurationServiceUri;

  public ConfigurationFileServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.configurationservice.uri}") String configurationServiceUri) {
    this.restTemplate = restTemplate;
    this.configurationServiceUri = configurationServiceUri;
  }

  @MethodLog
  @Override
  public ConfigurationFileItem getConfigurationItemByKey(String key) {
    Map<String, Object> params = new HashMap<>();

    params.put("key", key);

    ResponseEntity<ConfigurationFileItem> responseEntity =
        this.restTemplate.getForEntity(
            configurationServiceUri + "/configurationfile/{key}",
            ConfigurationFileItem.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public ConfigurationFileItem updateConfigurationItem(String key, byte[] value) {
    Map<String, Object> params = new HashMap<>();

    params.put("key", key);

    ResponseEntity<ConfigurationFileItem> responseEntity =
        this.restTemplate.postForEntity(
            configurationServiceUri + "/configurationfile/{key}",
            value,
            ConfigurationFileItem.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public List<ConfigurationFileItem> getAllConfigurationItems() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<ConfigurationFileItem[]> responseEntity =
        this.restTemplate.getForEntity(
            configurationServiceUri + "/configurationfile/all",
            ConfigurationFileItem[].class,
            params);

    return Arrays.asList(responseEntity.getBody());
  }
}
