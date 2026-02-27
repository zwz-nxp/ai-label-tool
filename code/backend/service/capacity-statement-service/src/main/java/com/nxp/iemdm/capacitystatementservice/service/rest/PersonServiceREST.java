package com.nxp.iemdm.capacitystatementservice.service.rest;

import com.nxp.iemdm.capacitystatementservice.service.PersonService;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PersonServiceREST implements PersonService {

  private final RestTemplate restTemplate;
  private final String personServiceURI;

  @Autowired
  public PersonServiceREST(
      RestTemplate restTemplate, @Value("${rest.personservice.uri}") String personServiceURI) {
    this.restTemplate = restTemplate;
    this.personServiceURI = personServiceURI;
  }

  @Override
  @MethodLog
  public Person getPersonByWBI(String wbi) {
    ResponseEntity<Person> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/{wbi}?includeroles=false", Person.class, wbi);

    return responseEntity.getBody();
  }

  @Override
  public List<Person> getAllPlannersForLocation(Location location) {
    Map<String, Object> params = new HashMap<>();

    params.put("locationid", location.getId());
    params.put("role", "Manager_Planning_Site");

    ResponseEntity<Person[]> responseEntity =
        this.restTemplate.getForEntity(
            personServiceURI + "/person/{locationid}/{role}/all", Person[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }
}
