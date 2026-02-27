package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.DateIntToTimeObjectParser;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log
@RequestMapping(path = "/i2service")
public class I2Service {

  private static final DateTimeFormatter DATE_TIME_FORMATTER_FOR_SQL =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final DateTimeFormatter DATE_TIME_FORMATTER_FOR_PERIOD_START =
      DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US);

  private final PersonService personService;
  private final String outputDirectoryLocation;
  private final Configuration freemarkerConfiguration;
  private final DateIntToTimeObjectParser dateIntToTimeObjectParser;
  private final LocationService locationService;
  private final String iemdmUserWbi;

  public I2Service(
      PersonService personService,
      @Value("${i2.outputdirectory.location}") String outputDirectoryLocation,
      Configuration freemarkerConfiguration,
      DateIntToTimeObjectParser dateIntToTimeObjectParser,
      LocationService locationService,
      @Value("${iemdm.user.wbi}") String iemdmUserWbi) {
    this.personService = personService;
    this.outputDirectoryLocation = outputDirectoryLocation;
    this.freemarkerConfiguration = freemarkerConfiguration;
    this.dateIntToTimeObjectParser = dateIntToTimeObjectParser;
    this.locationService = locationService;
    this.iemdmUserWbi = iemdmUserWbi;
  }

  @MethodLog
  @PostMapping(path = "/sendupdate/{location}/{wbi}/{i2Version}")
  public void sendCapacityStatementToI2(
      @PathVariable("location") Integer locationId,
      @PathVariable("wbi") String wbi,
      @PathVariable("i2Version") String i2Version) {
    Location location = this.locationService.getLocationById(locationId);
    sendCapacityStatement(location, wbi, i2Version);
  }

  public void sendCapacityStatementToI2(Location location, String wbi, String i2Version) {
    sendCapacityStatement(location, wbi, i2Version);
  }

  public void sendCapacityStatement(Location location, String wbi, String i2Version) {

    LocalDateTime localDateTime = LocalDateTime.now();

    String timestamp = localDateTime.format(DATE_TIME_FORMATTER_FOR_SQL);

    Person requester = personService.getPersonByWBI(wbi);
    Person iemdmUser = personService.getPersonByWBI(iemdmUserWbi);

    try (BufferedWriter writer1 =
        Files.newBufferedWriter(
            Paths.get(
                outputDirectoryLocation
                    + File.separator
                    + location.getAcronym()
                    + "_"
                    + timestamp
                    + "_MU_508A_"
                    + iemdmUserWbi
                    + "_1.sql"))) {
      Template template1;

      // For JDA the resourceLocation needs to be set to vendorCode if it is a subcon.
      // This can be done in place for this location object since the object lifecycle is only
      // limited to this method.
      if (location.getIsSubContractor()) {
        location.setSapCode(location.getVendorCodeFormatted());
      }

      if (location.getExtendedSuffix() == null) {
        template1 = freemarkerConfiguration.getTemplate("i2capacitystatementtemplate_1");
      } else {
        template1 =
            freemarkerConfiguration.getTemplate("i2capacitystatementtemplate_1_extendedfactory");
      }

      Map<String, Object> model = new HashMap<>();

      model.put("timestamp", timestamp);
      model.put("iemdmUser", iemdmUser);
      model.put("i2Version", i2Version);
      model.put("location", location);

      template1.process(model, writer1);

    } catch (IOException | TemplateException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
