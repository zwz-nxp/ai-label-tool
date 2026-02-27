package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import com.nxp.iemdm.shared.repository.jpa.ConfigurationValueItemRepository;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configurationvalue")
public class ConfigurationValueServiceImpl implements ConfigurationValueService {

  private final ConfigurationValueItemRepository configurationValueItemRepository;
  private final UpdateService updateService;

  @Autowired
  public ConfigurationValueServiceImpl(
      ConfigurationValueItemRepository configurationValueItemRepository,
      UpdateService updateService) {
    this.configurationValueItemRepository = configurationValueItemRepository;
    this.updateService = updateService;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<ConfigurationValueItem> getAllConfigurationItems() {
    return configurationValueItemRepository.findAll();
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{key}", produces = MediaType.APPLICATION_JSON)
  public ConfigurationValueItem getConfigurationItemForKey(@PathVariable("key") String key)
      throws com.nxp.iemdm.exception.NotFoundException {
    return configurationValueItemRepository.findById(key).orElseThrow(NotFoundException::new);
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/{key}",
      consumes = MediaType.TEXT_PLAIN,
      produces = MediaType.APPLICATION_JSON)
  public ConfigurationValueItem updateConfigurationItem(
      @PathVariable("key") String key, @RequestBody String value) throws NotFoundException {
    ConfigurationValueItem configurationItem =
        configurationValueItemRepository.findById(key).orElseThrow(NotFoundException::new);

    configurationItem.setConfigurationValue(value);
    configurationItem.setLastUpdated(Instant.now());
    ConfigurationValueItem result = configurationValueItemRepository.save(configurationItem);

    Update update = new Update(UpdateType.CONFIG_VALUE_ITEM, 0, null, null);
    this.updateService.update(update);

    return result;
  }
}
