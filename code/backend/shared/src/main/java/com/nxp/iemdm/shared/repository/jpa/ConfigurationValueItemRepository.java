package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import org.springframework.data.repository.CrudRepository;

public interface ConfigurationValueItemRepository
    extends CrudRepository<ConfigurationValueItem, String> {}
