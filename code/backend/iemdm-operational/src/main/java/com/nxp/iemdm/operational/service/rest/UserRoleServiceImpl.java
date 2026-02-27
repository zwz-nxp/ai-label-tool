package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.SapCode;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.model.user.RoleAllowed;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.operational.repository.jpa.RoleRepository;
import com.nxp.iemdm.operational.repository.jpa.SapCodeRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import com.nxp.iemdm.shared.intf.operational.UserRoleRepository;
import com.nxp.iemdm.shared.intf.operational.UserRoleService;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userrole")
public class UserRoleServiceImpl implements UserRoleService {
  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;
  private final SapCodeRepository sapCodeRepository;
  private final UpdateService updateService;
  private final ConfigurationValueService configurationValueService;
  private final PersonService personService;

  @Autowired
  public UserRoleServiceImpl(
      UserRoleRepository userRoleRepository,
      RoleRepository roleRepository,
      SapCodeRepository sapCodeRepository,
      UpdateService updateService,
      ConfigurationValueService configurationValueService,
      PersonService personService) {
    this.userRoleRepository = userRoleRepository;
    this.roleRepository = roleRepository;
    this.sapCodeRepository = sapCodeRepository;
    this.updateService = updateService;
    this.configurationValueService = configurationValueService;
    this.personService = personService;
  }

  private List<UserRole> findRolesTakingIntoAccountReadOnlyPeriod(String wbi) {
    Person person = this.personService.getPersonByWBI(wbi, true);
    // Always return the user's actual roles regardless of read-only mode.
    // Read-only restrictions are enforced at the endpoint/authorization level.
    return this.userRoleRepository.findAllByUser(person);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{wbi}/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<UserRole> getAllForUser(@PathVariable("wbi") String wbi) {
    Person person = new Person();
    person.setWbi(wbi);
    return this.userRoleRepository.findAllByUser(person);
  }

  @MethodLog
  @Transactional
  @PostMapping(
      path = "/{wbi}/{locationid}/{roleid}/{updatedBy}",
      produces = MediaType.APPLICATION_JSON)
  public UserRole addUserToRoleForLocation(
      @PathVariable("wbi") String wbi,
      @PathVariable("roleid") String roleId,
      @PathVariable("locationid") Integer locationId,
      @PathVariable("updatedBy") String updatedBy) {
    Person person = new Person();
    person.setWbi(wbi);

    Location location = new Location();
    location.setId(locationId);

    Role role = new Role();
    role.setId(roleId);

    UserRole userRole = new UserRole(person, location, role, Instant.now(), updatedBy);

    userRole = this.userRoleRepository.save(userRole);

    this.sendUserRole(person);

    return userRole;
  }

  @MethodLog
  @Transactional
  @DeleteMapping(path = "/{wbi}/{locationid}/{roleid}", produces = MediaType.APPLICATION_JSON)
  public void removeUserFromRoleForLocation(
      @PathVariable("wbi") String wbi,
      @PathVariable("roleid") String roleId,
      @PathVariable("locationid") Integer locationId) {
    Person person = new Person();
    person.setWbi(wbi);

    Location location = new Location();
    location.setId(locationId);

    Role role = new Role();
    role.setId(roleId);

    this.userRoleRepository.deleteByUserAndRoleAndLocation(person, role, location);

    Update update = new Update(UpdateType.USERROLE, 0, null, null);
    this.updateService.update(update);
  }

  /** return a list of RoleAllowed record(s), or an empty list */
  @MethodLog
  @Transactional
  @GetMapping(path = "/{wbi}/getAllRolesAllowed", produces = MediaType.APPLICATION_JSON)
  public List<RoleAllowed> getAllRolesAllowed(@PathVariable("wbi") String wbi) {
    return this.findRolesTakingIntoAccountReadOnlyPeriod(wbi).stream()
        .map(this::buildRoleAllowed)
        .toList();
  }

  @Override
  public boolean isUserAllowedForRole(String wbi, String roleId, Integer locationId) {
    List<UserRole> userRoles = this.findRolesTakingIntoAccountReadOnlyPeriod(wbi);
    for (UserRole userRole : userRoles) {
      if ((userRole.getLocation().getId().equals(0)
              || userRole.getLocation().getId().equals(locationId))
          && userRole.getRole().getId().equals(roleId)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<UserRole> findAll() {
    return StreamSupport.stream(this.userRoleRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public List<UserRole> findAllByUser(Person user) {
    return user == null ? List.of() : this.findRolesTakingIntoAccountReadOnlyPeriod(user.getWbi());
  }

  @Override
  public List<UserRole> findAllByRoleId(String roleName) {
    return this.userRoleRepository.findAllByRole_Id(roleName);
  }

  @Override
  public List<UserRole> findAllByLocation(Location location) {
    return this.userRoleRepository.findAllByLocation(location);
  }

  @Override
  public List<UserRole> findAllByUserAndRoleRoleId(Person user, String roleName) {
    return userRoleRepository.findByUserAndRole_IdAndLocation_Status(
        user, roleName, LocationStatus.ACTIVE);
  }

  @Override
  public Set<Person> findAllUsersWithRoleForLocation(String roleName, List<Location> locations) {
    return this.userRoleRepository.findAllUsersWithRoleForLocation(roleName, locations);
  }

  @Override
  public Optional<Role> findByRoleId(String roleId) {
    return this.roleRepository.findById(roleId);
  }

  // ----------- private ------------------

  private RoleAllowed buildRoleAllowed(UserRole userRole) {
    RoleAllowed result = new RoleAllowed();
    result.setRole(userRole.getRole());
    Location loc = userRole.getLocation();
    result.setSapCodes(new HashSet<>());
    if (loc == null) {
      result.setAllSites(false);
      result.setSite(null);
    } else if (loc.getId().equals(0)) {
      result.setAllSites(true);
      result.setSite("All Sites");
      result.getSapCodes().add("All Sapcodes");
    } else {
      result.setAllSites(false);
      result.setSite(loc.getAcronym());
      result
          .getSapCodes()
          .addAll(
              this.sapCodeRepository.findAllByManagedBy(loc).stream()
                  .map(SapCode::getPlantCode)
                  .toList());
      result.getSapCodes().add(loc.getSapCode());
    }

    return result;
  }

  @MethodLog
  @Transactional
  public void sendUserRole(Person person) {
    Update update = new Update(UpdateType.USERROLE, 0, null, null);
    this.updateService.update(update);
  }
}
