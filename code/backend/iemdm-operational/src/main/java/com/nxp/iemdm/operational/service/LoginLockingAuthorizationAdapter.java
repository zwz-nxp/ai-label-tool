package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.enums.configuration.ConfigurationValueType;
import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.shared.intf.operational.AuthorizationAdapter;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginLockingAuthorizationAdapter implements AuthorizationAdapter {

  private final ConfigurationValueService configurationValueService;
  private final UpdateService updateService;

  public LoginLockingAuthorizationAdapter(
      ConfigurationValueService configurationValueService, UpdateService updateService) {
    this.configurationValueService = configurationValueService;
    this.updateService = updateService;
  }

  @Override
  public void restrictToReadOnlyAccess() {
    this.configurationValueService.updateConfigurationItem(
        ConfigurationValueType.READ_ONLY_MODE.getConfigItem(), "true");
    log.info("Read-only restriction is activated.");
    this.notifyFrontEndOfReadOnlyModeChange();
  }

  @Override
  public void removeReadOnlyRestriction() {
    this.configurationValueService.updateConfigurationItem(
        ConfigurationValueType.READ_ONLY_MODE.getConfigItem(), "false");
    log.info("Read-only restriction is deactivated.");
    this.notifyFrontEndOfReadOnlyModeChange();
  }

  private void notifyFrontEndOfReadOnlyModeChange() {
    Update updatePerson = new Update(UpdateType.PERSON, 0, null, null);
    Update updateConfig = new Update(UpdateType.CONFIG_VALUE_ITEM, 0, null, null);
    this.updateService.update(updatePerson);
    this.updateService.update(updateConfig);
  }
}
