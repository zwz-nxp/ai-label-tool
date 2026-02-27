package com.nxp.iemdm.service;

import com.nxp.iemdm.model.user.Person;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PersonService {
  Person getPersonByWBI(String wbi, boolean includeRoles);

  Boolean checkIfWBIExists(String wbi);

  CompletableFuture<Void> processLoginForPerson(Person person);

  Person addPerson(Person person);

  List<Person> getAllPersons();

  List<Person> getAllPersonsForLocation(Integer locationid);

  List<Person> getAllPersonsWithRoleForLocation(Integer locationid, String rolename);

  List<Person> getAllPersonsWithGlobalRole(String rolename);

  List<Person> searchForPerson(String wbi);

  byte[] getProfilePicture(String wbi);
}
