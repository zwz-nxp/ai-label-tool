package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.logging.SysJobLog;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/i2scheduler")
public class Scheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
  private static final String I2_JOB = "CAP_STMT_TO_I2";

  private final LocationService locationService;
  private final ConfigurationService configurationService;
  private final String iemdmUserWbi;
  private final String i2VersionKey;
  private final SysJobLogService sysJobLogService;

  public Scheduler(
      LocationService locationService,
      ConfigurationService configurationService,
      @Value("${iemdm.user.wbi}") String iemdmUserWbi,
      @Value("${configuration.key.i2.version}") String i2VersionKey,
      SysJobLogService sysJobLogService) {

    this.locationService = locationService;
    this.configurationService = configurationService;
    this.iemdmUserWbi = iemdmUserWbi;
    this.i2VersionKey = i2VersionKey;
    this.sysJobLogService = sysJobLogService;
  }

  @MethodLog
  @GetMapping(path = "/sendupdate")
  public void sendI2CapacityStatements() {
    LOGGER.info("Started i2 capacity statement push for all locations");
    this.writeSysLogJob(I2_JOB, SysJobLogService.START_JOB);

    String i2Version = configurationService.getConfigurationItemByKey(i2VersionKey);
    locationService.getAllLocations().stream()
        .filter(this::isNotGlobalLocation)
        .filter(location -> "I2".equals(location.getPlanningEngine()))
        .forEach(
            location -> {
              LOGGER.info("Pushing i2 capacity statement for location {}", location.getAcronym());
            });

    this.writeSysLogJob(I2_JOB, SysJobLogService.END_JOB);
  }

  private boolean isNotGlobalLocation(Location location) {
    return !location.getId().equals(0);
  }

  private void writeSysLogJob(String jobName, String msg) {
    SysJobLog sysJobLog = new SysJobLog();
    sysJobLog.setJobName(jobName);
    sysJobLog.setLogMessage(msg);
    sysJobLog.setTimestamp(Instant.now());

    this.sysJobLogService.save(sysJobLog);
  }
}
