package com.nxp.iemdm.notification.service;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import com.nxp.iemdm.model.configuration.pojo.EmailMetaInfo;
import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.notification.EmailMetaDataService;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.VersionInfoService;
import com.nxp.iemdm.shared.utility.VersionInfoHelper;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
public class EmailMetaDataServiceImpl implements EmailMetaDataService {

  private final ConfigurationValueService configurationValueService;
  private final VersionInfoService versionInfoService;

  public EmailMetaDataServiceImpl(
      ConfigurationValueService configurationValueService, VersionInfoService versionInfoService) {
    this.configurationValueService = configurationValueService;
    this.versionInfoService = versionInfoService;
  }

  @MethodLog
  @Transactional
  public EmailMetaInfo getEmailMetaDataInfo() {
    EmailMetaInfo emailMetaInfo = new EmailMetaInfo();
    VersionInfo versionInfo = this.versionInfoService.getServices();
    emailMetaInfo.setAppVersion(
        String.format("%s (%s)", versionInfo.getVersion(), versionInfo.getBuildDate()));
    emailMetaInfo.setAppDate(versionInfo.getBuildDate());
    emailMetaInfo.setAppYear(this.getApplicationYear(versionInfo));
    emailMetaInfo.setAppName(this.getApplicationName());
    return emailMetaInfo;
  }

  String getApplicationYear(VersionInfo versionInfo) {
    return VersionInfoHelper.getYear(versionInfo.getVersion());
  }

  private String getApplicationName() {
    return "IE-MDM " + this.getAppEnvironment();
  }

  private String getAppEnvironment() {
    try {
      ConfigurationValueItem configurationValueItem =
          this.configurationValueService.getConfigurationItemForKey("Environment");
      return configurationValueItem.getConfigurationValue();
    } catch (NotFoundException notFoundException) {
      log.warning("Error could not retrieve Environment from SYS_CONFIG ");
      return "PROD";
    }
  }
}
