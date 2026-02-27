package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import com.nxp.iemdm.service.ConfigurationValueService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configurationvalue")
public class ConfigurationValueController {
  private final ConfigurationValueService configurationValueService;

  @Autowired
  public ConfigurationValueController(ConfigurationValueService configurationValueService) {
    this.configurationValueService = configurationValueService;
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<ConfigurationValueItem> getAllConfigurationItems() {
    return configurationValueService.getAllConfigurationItems();
  }

  @MethodLog
  @GetMapping(path = "/{key}", produces = MediaType.APPLICATION_JSON)
  public ConfigurationValueItem getConfigurationItemForKey(@PathVariable("key") String key) {
    return configurationValueService.getConfigurationItemByKey(key);
  }

  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @PostMapping(
      path = "/{key}",
      consumes = MediaType.TEXT_PLAIN,
      produces = MediaType.APPLICATION_JSON)
  public ConfigurationValueItem updateConfigurationItem(
      @PathVariable("key") String key, @RequestBody String value) {
    return configurationValueService.updateConfigurationItem(key, value);
  }
}
