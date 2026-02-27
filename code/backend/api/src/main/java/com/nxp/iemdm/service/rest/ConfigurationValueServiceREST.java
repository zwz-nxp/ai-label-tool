package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.enums.configuration.ConfigurationValueType;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import com.nxp.iemdm.service.ConfigurationValueService;
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
public class ConfigurationValueServiceREST implements ConfigurationValueService {
  private final RestTemplate restTemplate;
  private final String configurationServiceUri;

  public ConfigurationValueServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.configurationservice.uri}") String configurationServiceUri) {
    this.restTemplate = restTemplate;
    this.configurationServiceUri = configurationServiceUri;
  }

  @MethodLog
  @Override
  public ConfigurationValueItem getConfigurationItemByKey(String key) {
    Map<String, Object> params = new HashMap<>();

    params.put("key", key);

    ResponseEntity<ConfigurationValueItem> responseEntity =
        this.restTemplate.getForEntity(
            configurationServiceUri + "/configurationvalue/{key}",
            ConfigurationValueItem.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public ConfigurationValueItem updateConfigurationItem(String key, String value) {
    Map<String, Object> params = new HashMap<>();

    params.put("key", key);

    ResponseEntity<ConfigurationValueItem> responseEntity =
        this.restTemplate.postForEntity(
            configurationServiceUri + "/configurationvalue/{key}",
            value,
            ConfigurationValueItem.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public List<ConfigurationValueItem> getAllConfigurationItems() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<ConfigurationValueItem[]> responseEntity =
        this.restTemplate.getForEntity(
            configurationServiceUri + "/configurationvalue/all",
            ConfigurationValueItem[].class,
            params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public String getMaxNotifications() {
    return this.getConfigurationItemByKey(ConfigurationValueType.MAX_NOTIFICATIONS.getConfigItem())
        .getConfigurationValue();
  }

  @Override
  public String getDebounceTime() {
    return this.getConfigurationItemByKey(ConfigurationValueType.DEBOUNCE_TIME.getConfigItem())
        .getConfigurationValue();
  }

  @Override
  public boolean getReadOnlyMode() {
    String value =
        this.getConfigurationItemByKey(ConfigurationValueType.READ_ONLY_MODE.getConfigItem())
            .getConfigurationValue();
    return value.equalsIgnoreCase("true");
  }
}
