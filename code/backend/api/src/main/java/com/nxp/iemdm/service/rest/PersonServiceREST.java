package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.service.PersonService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Log
public class PersonServiceREST implements PersonService {

  private final RestTemplate restTemplate;
  private final String personServiceURI;

  @Autowired
  public PersonServiceREST(
      RestTemplate restTemplate, @Value("${rest.personservice.uri}") String personServiceURI) {
    this.restTemplate = restTemplate;
    this.personServiceURI = personServiceURI;
  }

  @MethodLog
  @Override
  public Person getPersonByWBI(String wbi, boolean includeRoles) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);
    params.put("includeRoles", includeRoles);

    ResponseEntity<Person> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/{wbi}?includeroles={includeRoles}", Person.class, params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public Boolean checkIfWBIExists(String wbi) {
    ResponseEntity<Boolean> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/{wbi}/exists", Boolean.class, Map.of("wbi", wbi));

    return Boolean.TRUE.equals(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public CompletableFuture<Void> processLoginForPerson(Person person) {
    person.setLastLogin(Instant.now());
    return CompletableFuture.runAsync(() -> sendUpdateToRestService(person))
        .exceptionally(this::logError);
  }

  @MethodLog
  @Override
  public Person addPerson(Person person) {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Person> responseEntity =
        this.restTemplate.postForEntity(personServiceURI + "/person", person, Person.class, params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public List<Person> getAllPersons() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(personServiceURI + "/person/all", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public List<Person> getAllPersonsForLocation(Integer locationid) {
    Map<String, Object> params = new HashMap<>();

    params.put("locationid", locationid);

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/all/{locationid}", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public List<Person> getAllPersonsWithRoleForLocation(Integer locationid, String rolename) {
    Map<String, Object> params = new HashMap<>();

    params.put("locationid", locationid);
    params.put("role", rolename);

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/{locationid}/{role}/all", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public List<Person> getAllPersonsWithGlobalRole(String rolename) {
    Map<String, Object> params = new HashMap<>();

    params.put("role", rolename);

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(personServiceURI + "/person/{role}", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public List<Person> searchForPerson(String wbi) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/search?wbi={wbi}", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public byte[] getProfilePicture(String wbi) {
    try {
      ResponseEntity<byte[]> responseEntity =
          this.restTemplate.getForEntity(
              personServiceURI + "/person/profilepicture/{wbi}", byte[].class, Map.of("wbi", wbi));

      return responseEntity.getBody();
    } catch (HttpClientErrorException httpClientErrorException) {
      return new byte[0];
    }
  }

  private void sendUpdateToRestService(Person person) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", person.getWbi());

    this.restTemplate.put(personServiceURI + "/person/{wbi}", person, params);
  }

  private Void logError(Throwable throwable) {
    log.log(Level.WARNING, "Error occurred during update of user", throwable);
    return null;
  }
}
