package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.VersionInfoService;
import com.nxp.iemdm.shared.utility.VersionInfoHelper;
import jakarta.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Log
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
@RestController
@RequestMapping("/version")
public class VersionInfoServiceImpl implements VersionInfoService {

  private static final Duration VERSION_INFO_EXPIRATION = Duration.ofHours(1);
  private final String appName;
  private final String databaseName;
  private final String databaseUrl;
  private final String commitHashShort;
  private final String version;
  private final String buildDate;
  private final String interfaceUri;
  private final String capacityStatementUri;
  private final RestTemplate restTemplate;

  @Autowired
  public VersionInfoServiceImpl(
      @Value("${spring.application.name}") String appName,
      @Value("${app.datasource.username}") String databaseName,
      @Value("${app.datasource.url}") String databaseUrl,
      @Value("${git.commit.id.abbrev:}") String commitHashShort,
      @Value("${git.build.time:}") String buildDate,
      @Value("${git.closest.tag.name}") String version,
      @Value("${rest.iemdm-interface.uri}") String interfaceUri,
      @Value("${rest.capacity-statement-service.uri}") String capacityStatementUri,
      RestTemplate restTemplate) {
    this.appName = appName;
    this.databaseName = databaseName;
    this.databaseUrl = databaseUrl;
    this.commitHashShort = commitHashShort;
    this.version = version;
    this.buildDate = buildDate;
    this.interfaceUri = interfaceUri;
    this.capacityStatementUri = capacityStatementUri;
    this.restTemplate = restTemplate;
  }

  @GetMapping(path = "/services", produces = MediaType.APPLICATION_JSON)
  public VersionInfo getServices() throws NotFoundException {
    VersionInfo versionInfo = this.getThisVersionInfo();
    versionInfo.setDatabaseName(this.databaseName);
    return versionInfo;
  }

  /**
   * This returns a list of VersionInfo, that will be populated from this WAR itself and some other
   * WAR files.
   *
   * @return List<VersionInfo>
   */
  @MethodLog
  @GetMapping(path = "/allinfos", produces = MediaType.APPLICATION_JSON)
  public List<VersionInfo> getAbout() throws NotFoundException {
    List<VersionInfo> result = new ArrayList<>();
    VersionInfo versionInfo = this.getThisVersionInfo();
    versionInfo.setDatabaseName(this.databaseName);
    versionInfo.setDatabaseUrl(this.databaseUrl);
    result.add(versionInfo);
    result.add(this.getCapacityStatementVersionInfo());
    result.add(this.getInterfaceVersionInfo());
    return result;
  }

  private VersionInfo getThisVersionInfo() {
    return VersionInfoHelper.create(
            this.appName, this.version, this.commitHashShort, this.buildDate)
        .build();
  }

  private VersionInfo getInterfaceVersionInfo() {
    return this.getVersionInfo(this.interfaceUri, IemdmConstants.IEMDM_INTERFACE);
  }

  private VersionInfo getCapacityStatementVersionInfo() {
    return this.getVersionInfo(this.capacityStatementUri, IemdmConstants.IEMDM_CAP_STMT);
  }

  private VersionInfo getVersionInfo(String applicationUrl, String application) {
    String url = String.format("%s/version/info", applicationUrl);
    try {
      ResponseEntity<VersionInfo> response = this.restTemplate.getForEntity(url, VersionInfo.class);
      return response.getBody();
    } catch (RestClientException restClientException) {
      log.log(
          Level.WARNING,
          String.format(
              "Failed to get version info from Interface Application: %s",
              restClientException.getMessage()));
      return this.buildUnknownVersionInfo(application);
    }
  }

  private VersionInfo buildUnknownVersionInfo(String name) {
    String unknown = "--";
    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setName(name);
    versionInfo.setVersion(unknown);
    versionInfo.setCommitHashShort(unknown);
    versionInfo.setBuildDate(unknown);
    versionInfo.setNode(unknown);
    return versionInfo;
  }
}
