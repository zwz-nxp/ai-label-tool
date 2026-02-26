package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PersonService {
  Person getPersonByWBI(String wbi, boolean includeRoles) throws NotFoundException;

  boolean doesWBIExist(String wbi);

  void processLoginForPerson(String wbi, Person person) throws NotFoundException;

  Person savePerson(Person person);

  Iterable<Person> getAllPersons();

  Iterable<Person> getAllPersonsWithRightsForLocation(Integer locationId);

  Set<Person> getAllPersonsWithGlobalRole(String roleName);

  Set<Person> getAllPersonsWithRoleForLocation(Integer locationId, String roleName);

  Set<Person> getAllPersonsWithRoleForLocation(Location location, String roleName);

  Set<Person> getAllPersonsWithRole(String roleName);

  List<Person> searchForPersons(String wbi);

  Page<Person> findAll(@Nullable Specification<Person> spec, Pageable pageable);

  byte[] getProfilePicture(String wbi) throws NotFoundException;
}
