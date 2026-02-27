package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import com.nxp.iemdm.service.VersionInfoService;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.utility.VersionInfoHelper;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VersionInfoController {
  private final String appName;
  private final String commitHashShort;
  private final String version;
  private final String buildDate;
  private final VersionInfoService versionInfoService;

  public VersionInfoController(
      @Value(IemdmConstants.IEMDM_API) String appName,
      @Value("${git.commit.id.abbrev:}") String commitHashShort,
      @Value("${git.closest.tag.name}") String version,
      @Value("${git.build.time:}") String buildDate,
      VersionInfoService versionInfoService) {
    this.appName = appName;
    this.commitHashShort = commitHashShort;
    this.version = version;
    this.buildDate = buildDate;
    this.versionInfoService = versionInfoService;
  }

  @MethodLog
  @GetMapping(path = "/version/services", produces = MediaType.APPLICATION_JSON)
  public VersionInfo getServices() {
    return this.versionInfoService.getServicesVersionInfo();
  }

  @MethodLog
  @GetMapping(path = "/version/info", produces = MediaType.APPLICATION_JSON)
  public List<VersionInfo> getAbout() {
    List<VersionInfo> result = this.versionInfoService.getAllVersionInfos();
    result.add(getThisVersion());
    return result;
  }

  private VersionInfo getThisVersion() {
    return VersionInfoHelper.create(
            this.appName, this.version, this.commitHashShort, this.buildDate)
        .build();
  }
}
