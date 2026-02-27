package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.utility.VersionInfoHelper;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
@RestController
@RequestMapping("/version")
public class VersionInfoService {

  private final String appName;
  private final String commitHashShort;
  private final String version;
  private final String buildDate;

  public VersionInfoService(
      @Value(IemdmConstants.IEMDM_CAP_STMT) String appName,
      @Value("${git.commit.id.abbrev:}") String commitHashShort,
      @Value("${git.closest.tag.name}") String version,
      @Value("${git.build.time:}") String buildDate) {
    this.appName = appName;
    this.commitHashShort = commitHashShort;
    this.version = version;
    this.buildDate = buildDate;
  }

  @MethodLog
  @GetMapping(path = "/info", produces = MediaType.APPLICATION_JSON)
  public VersionInfo getVersionInfo() {
    return VersionInfoHelper.create(
            this.appName, this.version, this.commitHashShort, this.buildDate)
        .build();
  }
}
