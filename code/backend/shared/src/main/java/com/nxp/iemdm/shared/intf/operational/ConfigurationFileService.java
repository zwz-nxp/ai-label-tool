package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import java.util.List;

public interface ConfigurationFileService {
  Iterable<ConfigurationFileItem> getAllConfigurationItems();

  ConfigurationFileItem getConfigurationItemForKey(String key) throws NotFoundException;

  List<ConfigurationFileItem> getConfigurationItemsForKey(String key);

  ConfigurationFileItem updateConfigurationItem(String key, byte[] value) throws NotFoundException;
}
