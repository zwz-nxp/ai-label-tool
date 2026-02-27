package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import java.util.List;

public interface ConfigurationValueService {
  ConfigurationValueItem getConfigurationItemByKey(String key);

  ConfigurationValueItem updateConfigurationItem(String key, String value);

  List<ConfigurationValueItem> getAllConfigurationItems();

  String getMaxNotifications();

  String getDebounceTime();

  boolean getReadOnlyMode();
}
