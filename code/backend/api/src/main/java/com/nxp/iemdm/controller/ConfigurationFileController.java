package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import com.nxp.iemdm.service.ConfigurationFileService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/configurationfile")
public class ConfigurationFileController {
  private final ConfigurationFileService configurationFileService;

  @Autowired
  public ConfigurationFileController(ConfigurationFileService configurationFileService) {
    this.configurationFileService = configurationFileService;
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<ConfigurationFileItem> getAllConfigurationItems() {
    return configurationFileService.getAllConfigurationItems();
  }

  @MethodLog
  @GetMapping(path = "/{key}", produces = MediaType.APPLICATION_JSON)
  public ConfigurationFileItem getConfigurationItemForKey(@PathVariable("key") String key) {
    return configurationFileService.getConfigurationItemByKey(key);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @PostMapping(
      path = "/{key}",
      consumes = MediaType.TEXT_PLAIN,
      produces = MediaType.APPLICATION_JSON)
  public ConfigurationFileItem updateConfigurationItem(
      @PathVariable("key") String key, @RequestParam("file") MultipartFile file)
      throws IOException {
    return configurationFileService.updateConfigurationItem(key, file.getBytes());
  }
}
