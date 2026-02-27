package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import com.nxp.iemdm.operational.repository.jpa.ConfigurationFileItemRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.ConfigurationFileService;
import com.nxp.iemdm.shared.utility.SearchParameterHelper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configurationfile")
public class ConfigurationFileServiceImpl implements ConfigurationFileService {

  private final ConfigurationFileItemRepository configurationFileItemRepository;

  @Autowired
  public ConfigurationFileServiceImpl(
      ConfigurationFileItemRepository configurationFileItemRepository) {
    this.configurationFileItemRepository = configurationFileItemRepository;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<ConfigurationFileItem> getAllConfigurationItems() {
    return configurationFileItemRepository.findAll();
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{key}", produces = MediaType.APPLICATION_JSON)
  public ConfigurationFileItem getConfigurationItemForKey(@PathVariable("key") String key)
      throws NotFoundException {
    return configurationFileItemRepository.findById(key).orElseThrow(NotFoundException::new);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{key}/all", produces = MediaType.APPLICATION_JSON)
  public List<ConfigurationFileItem> getConfigurationItemsForKey(@PathVariable("key") String key) {
    String wildCardKey = SearchParameterHelper.formatSqlLikeWildcard(key);
    return configurationFileItemRepository.findAllByConfigurationKeyLike(wildCardKey);
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/{key}",
      consumes = MediaType.TEXT_PLAIN,
      produces = MediaType.APPLICATION_JSON)
  public ConfigurationFileItem updateConfigurationItem(
      @PathVariable("key") String key, @RequestBody byte[] value) throws NotFoundException {
    ConfigurationFileItem configurationItem =
        configurationFileItemRepository.findById(key).orElseThrow(NotFoundException::new);

    configurationItem.setConfigurationValue(value);
    configurationItem.setLastUpdated(Instant.now());

    return configurationFileItemRepository.save(configurationItem);
  }
}
