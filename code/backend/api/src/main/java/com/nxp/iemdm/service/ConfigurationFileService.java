package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import java.util.List;

public interface ConfigurationFileService {
  ConfigurationFileItem getConfigurationItemByKey(String key);

  ConfigurationFileItem updateConfigurationItem(String key, byte[] value);

  List<ConfigurationFileItem> getAllConfigurationItems();
}
