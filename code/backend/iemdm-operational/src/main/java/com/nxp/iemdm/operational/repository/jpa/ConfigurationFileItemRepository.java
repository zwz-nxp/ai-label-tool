package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.configuration.ConfigurationFileItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfigurationFileItemRepository
    extends JpaRepository<ConfigurationFileItem, String> {

  @Query("FROM ConfigurationFileItem WHERE configurationKey LIKE lower(?1)")
  List<ConfigurationFileItem> findAllByConfigurationKeyLike(String key);
}
