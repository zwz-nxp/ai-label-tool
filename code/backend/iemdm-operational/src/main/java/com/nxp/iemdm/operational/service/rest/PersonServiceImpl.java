package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.enums.user.UserRoleType;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.operational.service.ldap.PersonLdapService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import com.nxp.iemdm.shared.intf.operational.ProfilePictureService;
import com.nxp.iemdm.shared.intf.operational.UserRoleRepository;
import com.nxp.iemdm.shared.repository.jpa.PersonRepository;
import jakarta.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/person")
public class PersonServiceImpl implements PersonService {

  private final PersonRepository personRepository;
  private final UserRoleRepository userRoleRepository;
  private final PersonLdapService personLdapService;
  private final Location global;
  private final ProfilePictureService profilePictureService;
  private final ConfigurationValueService configurationValueService;

  {
    this.global = new Location();
    this.global.setId(0);
  }

  public PersonServiceImpl(
      PersonRepository personRepository,
      UserRoleRepository userRoleRepository,
      PersonLdapService personLdapService,
      ProfilePictureService profilePictureService,
      ConfigurationValueService configurationValueService) {
    this.personRepository = personRepository;
    this.userRoleRepository = userRoleRepository;
    this.personLdapService = personLdapService;
    this.profilePictureService = profilePictureService;
    this.configurationValueService = configurationValueService;
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(
      path = "/{wbi}",
      params = {"includeroles"},
      produces = MediaType.APPLICATION_JSON)
  @Cacheable(value = "persons", condition = "#includeRoles == false")
  public Person getPersonByWBI(
      @PathVariable("wbi") String wbi, @RequestParam("includeroles") boolean includeRoles)
      throws NotFoundException {

    Person person =
        this.personRepository.findByWbiIgnoreCase(wbi).orElseThrow(NotFoundException::new);

    if (includeRoles) {
      this.addRolesToPerson(person);
    }

    return person;
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/{wbi}/exists", produces = MediaType.APPLICATION_JSON)
  public boolean doesWBIExist(@PathVariable("wbi") String wbi) throws NotFoundException {
    return this.personRepository.existsByWbiIgnoreCase(wbi);
  }

  @Override
  @MethodLog
  @Transactional
  @PutMapping(path = "/{wbi}", consumes = MediaType.APPLICATION_JSON)
  public void processLoginForPerson(@PathVariable("wbi") String wbi, @RequestBody Person person)
      throws NotFoundException {
    Person existingPerson =
        this.personRepository.findByWbiIgnoreCase(wbi).orElseThrow(NotFoundException::new);

    Person personFromLdap = this.personLdapService.searchForPersons(wbi).get(0);

    existingPerson.setLastLogin(person.getLastLogin());
    existingPerson.setName(personFromLdap.getName());
    existingPerson.setEmail(personFromLdap.getEmail());

    this.personRepository.save(existingPerson);
  }

  @Override
  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON)
  public Person savePerson(@RequestBody Person person) {
    return this.personRepository.save(person);
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<Person> getAllPersons() {
    Iterable<Person> persons = this.personRepository.findAll();

    persons.forEach(this::addRolesToPerson);

    return persons;
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/all/{locationid}", produces = MediaType.APPLICATION_JSON)
  public Iterable<Person> getAllPersonsWithRightsForLocation(
      @PathVariable("locationid") Integer locationId) {
    Location location = new Location();
    location.setId(locationId);

    List<UserRole> userRoles = this.userRoleRepository.findAllByLocation(location);

    return userRoles.stream()
        .map(UserRole::getUser)
        .distinct()
        .map(this::addRolesToPerson)
        .collect(Collectors.toSet());
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/{rolename}")
  public Set<Person> getAllPersonsWithGlobalRole(@PathVariable("rolename") String roleName) {
    List<UserRole> userRoles =
        this.userRoleRepository.findAllByRole_IdAndLocation(roleName, this.global);

    return userRoles.stream().map(UserRole::getUser).collect(Collectors.toSet());
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/{locationid}/{role}/all", produces = MediaType.APPLICATION_JSON)
  public Set<Person> getAllPersonsWithRoleForLocation(
      @PathVariable("locationid") Integer locationId, @PathVariable("role") String roleName) {
    Location location = new Location();
    location.setId(locationId);

    return this.getAllPersonsWithRoleForLocation(location, roleName);
  }

  @Override
  @MethodLog
  @Transactional
  public Set<Person> getAllPersonsWithRoleForLocation(Location location, String roleName) {
    List<UserRole> userRoles =
        this.userRoleRepository.findAllByRole_IdAndLocation(roleName, location);

    userRoles.addAll(this.userRoleRepository.findAllByRole_IdAndLocation(roleName, this.global));

    return userRoles.stream()
        .map(UserRole::getUser)
        .distinct()
        .map(this::addRolesToPerson)
        .collect(Collectors.toSet());
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/{role}/all", produces = MediaType.APPLICATION_JSON)
  public Set<Person> getAllPersonsWithRole(@PathVariable("role") String roleName) {
    List<UserRole> userRoles = this.userRoleRepository.findAllByRole_Id(roleName);

    return userRoles.stream().map(UserRole::getUser).collect(Collectors.toSet());
  }

  @Override
  @MethodLog
  @Transactional
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON, params = "wbi")
  public List<Person> searchForPersons(@RequestParam("wbi") String wbi) {
    List<Person> persons = this.personLdapService.searchForPersons(wbi);

    persons.forEach(
        person -> {
          Optional<Person> optionalPerson = this.personRepository.findById(person.getWbi());

          if (optionalPerson.isPresent()) {
            person.setLastLogin(optionalPerson.get().getLastLogin());
            person.setPrimaryLocation(optionalPerson.get().getPrimaryLocation());
          }
        });

    return persons;
  }

  @Override
  public Page<Person> findAll(Specification<Person> spec, Pageable pageable) {
    return this.personRepository.findAll(spec, pageable);
  }

  @Override
  @GetMapping(path = "/profilepicture/{wbi}", produces = "image/jpeg")
  public byte[] getProfilePicture(@PathVariable("wbi") String wbi) throws NotFoundException {
    Optional<Person> optionalPerson = this.personRepository.findByWbiIgnoreCase(wbi);

    return optionalPerson
        .map(Person::getEmail)
        .map(this.profilePictureService::getProfilePictureForUser)
        .orElseThrow(NotFoundException::new);
  }

  // ------------- private ----------------

  private Person addRolesToPerson(Person person) {
    Iterable<UserRole> userRoles = this.userRoleRepository.findAllByUser(person);

    userRoles.forEach(
        userRole -> {
          Integer locationId = userRole.getLocation() != null ? userRole.getLocation().getId() : 0;
          if (!person.getRoles().containsKey(locationId)) {
            person.getRoles().put(locationId, new HashSet<>());
          }

          person.getRoles().get(locationId).add(userRole.getRole());
        });

    this.adjustRolesForReadOnlyMode(person);

    return person;
  }

  private void adjustRolesForReadOnlyMode(Person person) {
    // System admins are never restricted by read-only mode
    Location globalLocation = new Location();
    globalLocation.setId(0);
    if (person.hasRoleForLocationOrGlobal(
        globalLocation, UserRoleType.ADMINISTRATOR_SYSTEM.getName())) {
      return;
    }
    // In read-only mode, non-admin users keep their roles so they can still log in
    // and see the application — write operations are restricted at the endpoint level.
    // Wiping roles here causes 404 on /currentuser and breaks the entire session.
  }
}
