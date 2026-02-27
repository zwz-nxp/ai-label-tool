package com.nxp.iemdm.model.configuration.pojo;

import java.util.List;
import java.util.Map;

public class ColumnsConfiguration {
  private String defaultConfigurationName = "";
  private Map<String, List<String>> configurationsByName;

  public ColumnsConfiguration(
      String defaultConfigurationName, Map<String, List<String>> configurationsByName) {
    this.defaultConfigurationName = defaultConfigurationName;
    this.configurationsByName = configurationsByName;
  }

  public ColumnsConfiguration() {}

  public String getDefaultConfigurationName() {
    return defaultConfigurationName;
  }

  public void setDefaultConfigurationName(String defaultConfigurationName) {
    this.defaultConfigurationName = defaultConfigurationName;
  }

  public Map<String, List<String>> getConfigurationsByName() {
    return configurationsByName;
  }

  public void setConfigurationsByName(Map<String, List<String>> configurationsByName) {
    this.configurationsByName = configurationsByName;
  }
}
