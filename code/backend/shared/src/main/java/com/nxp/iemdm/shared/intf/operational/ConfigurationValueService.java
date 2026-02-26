package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;

public interface ConfigurationValueService {

  Iterable<ConfigurationValueItem> getAllConfigurationItems();

  ConfigurationValueItem getConfigurationItemForKey(String key) throws NotFoundException;

  ConfigurationValueItem updateConfigurationItem(String key, String value) throws NotFoundException;
}
