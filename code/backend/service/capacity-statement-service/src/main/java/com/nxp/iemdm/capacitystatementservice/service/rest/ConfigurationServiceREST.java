package com.nxp.iemdm.capacitystatementservice.service.rest;

import com.nxp.iemdm.capacitystatementservice.service.ConfigurationService;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConfigurationServiceREST implements ConfigurationService {
  private final RestTemplate restTemplate;
  private final String configurationServiceUri;

  public ConfigurationServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.configurationservice.uri}") String configurationServiceUri) {
    this.restTemplate = restTemplate;
    this.configurationServiceUri = configurationServiceUri;
  }

  @Override
  public String getConfigurationItemByKey(String key) {
    Map<String, Object> params = new HashMap<>();

    params.put("key", key);

    ResponseEntity<ConfigurationValueItem> responseEntity =
        this.restTemplate.getForEntity(
            configurationServiceUri + "/configurationvalue/{key}",
            ConfigurationValueItem.class,
            params);

    return responseEntity.getBody().getConfigurationValue();
  }
}
