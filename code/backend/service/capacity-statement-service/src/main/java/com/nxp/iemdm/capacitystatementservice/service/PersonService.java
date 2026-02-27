package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import java.util.List;

public interface PersonService {
  Person getPersonByWBI(String wbi);

  List<Person> getAllPlannersForLocation(Location location);
}
