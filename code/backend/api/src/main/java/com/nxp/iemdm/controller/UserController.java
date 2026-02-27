package com.nxp.iemdm.controller;

import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.service.LocationService;
import com.nxp.iemdm.service.PersonService;
import com.nxp.iemdm.service.UserRoleService;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodJobLog;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.utility.GenericExcelGenerator;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Value("${app.environment}")
  private String environment;

  private static final String PROD_ENVIRONMENT = "Production";
  public static final String SWITCH_USER_SESSION_KEY = "SWITCH_USER_WBI";

  private static final Logger log = LoggerFactory.getLogger(UserController.class);
  private final PersonService personService;
  private final LocationService locationService;

  private final UserRoleService userRoleService;

  @Autowired
  public UserController(
      PersonService personService,
      LocationService locationService,
      UserRoleService userRoleService) {

    this.personService = personService;
    this.locationService = locationService;
    this.userRoleService = userRoleService;
  }

  @MethodLog
  @GetMapping(path = "/currentuser", produces = MediaType.APPLICATION_JSON)
  public Person getUser(@AuthenticationPrincipal IEMDMPrincipal currentUser) {
    String wbi = currentUser.getUsername();
    return this.personService.getPersonByWBI(wbi, true);
  }

  @GetMapping(path = "/profilepicture", produces = "image/jpeg")
  public ResponseEntity<ByteArrayResource> getProfilePicture(
      @AuthenticationPrincipal IEMDMPrincipal currentUser) {
    byte[] imageData = this.personService.getProfilePicture(currentUser.getUsername());
    ByteArrayResource byteArrayResource = new ByteArrayResource(imageData);
    return ResponseEntity.ok()
        .contentLength(imageData.length)
        .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
        .body(byteArrayResource);
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  public List<Person> getAllUsers() {
    return this.personService.getAllPersons();
  }

  @MethodLog
  @GetMapping(path = "/all/{location}", produces = MediaType.APPLICATION_JSON)
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  public List<Person> getAllUsersForLocation(@PathVariable("location") Integer locationId) {
    return this.personService.getAllPersonsForLocation(locationId);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON, params = "wbi")
  public List<Person> searchForPerson(@RequestParam("wbi") String wbi) {
    return this.personService.searchForPerson(wbi);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public Person addPerson(
      @RequestBody @Valid Person person, @AuthenticationPrincipal IEMDMPrincipal currentUser) {
    if (personService.checkIfWBIExists(person.getWbi())) {
      throw new BadRequestException("User already exists");
    }

    person.setLastUpdated(Instant.now());
    person.setUpdatedBy(currentUser.getUsername());
    return this.personService.addPerson(person);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  @PostMapping(
      path = "/saveUser",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Person savePerson(
      @RequestBody @Valid Person person, @AuthenticationPrincipal IEMDMPrincipal currentUser) {
    person.setLastUpdated(Instant.now());
    person.setUpdatedBy(currentUser.getUsername());

    Person savedPerson = this.personService.addPerson(person);
    savedPerson
        .getRoles()
        .forEach(
            (integer, roles) ->
                roles.forEach(
                    role ->
                        this.userRoleService.addUserToRoleForLocation(
                            savedPerson.getWbi(),
                            role.getId(),
                            integer,
                            currentUser.getUsername())));

    return savedPerson;
  }

  @MethodJobLog
  @PreAuthorize("hasGlobalRole('Administrator_User')")
  @GetMapping(path = "/downloadExcel", produces = MediaType.APPLICATION_OCTET_STREAM)
  public ResponseEntity<ByteArrayResource> downloadGeneratedExcel() {

    try {
      String filename =
          String.format(
              "IE-MDM Userlist (%s) %s.xlsx",
              this.environment, LocalDate.now().format(IemdmConstants.DATE_TIME_FORMATTER));
      String headerValues = String.format("attachment; filename=%s", filename);
      List<Person> users = this.getAllUsers();
      ByteArrayResource byteArrayResource = this.generateExcel(users);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
          .contentLength(byteArrayResource.getByteArray().length)
          .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
          .body(byteArrayResource);
    } catch (Exception ex) {
      String errmsg = ex.getMessage();
      ByteArrayResource bas = new ByteArrayResource(errmsg.getBytes());
      String headerValues = String.format("attachment; filename=%s", "\"error-occurred.txt\"");
      return ResponseEntity.status(500)
          .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
          .contentLength(bas.getByteArray().length)
          .body(bas);
    }
  }

  @MethodLog
  @PostMapping(path = "/switch", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<String> switchUser(
      @RequestParam("wbi") String wbi, HttpServletRequest request, HttpServletResponse response) {
    if (PROD_ENVIRONMENT.equals(this.environment)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not available in production");
    }

    // Check if user exists in local database only
    if (!personService.checkIfWBIExists(wbi)) {
      log.info("Switch user failed: User '{}' not found in database", wbi);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("User '" + wbi + "' not found in user management");
    }

    // Create session if it doesn't exist and set the switched user
    HttpSession session = request.getSession(true);
    session.setAttribute(SWITCH_USER_SESSION_KEY, wbi);

    // Force session to be saved immediately
    session.setAttribute("FORCE_SAVE", System.currentTimeMillis());

    log.info(
        "User switched to: {} (Session ID: {}, MaxInactiveInterval: {})",
        wbi,
        session.getId(),
        session.getMaxInactiveInterval());

    return ResponseEntity.ok(wbi);
  }

  // ----------- private ------------

  private ByteArrayResource generateExcel(List<Person> users) throws IOException {
    List<Location> locations = this.locationService.getAllLocations();
    GenericExcelGenerator excelGenerator = new GenericExcelGenerator();
    excelGenerator.generateUsersSheet(users, "Users");
    excelGenerator.generateUserRolesSheet(users, locations, "User roles");
    Workbook workbook = excelGenerator.getWorkbook();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    workbook.write(byteArrayOutputStream);
    return new ByteArrayResource(byteArrayOutputStream.toByteArray());
  }
}
