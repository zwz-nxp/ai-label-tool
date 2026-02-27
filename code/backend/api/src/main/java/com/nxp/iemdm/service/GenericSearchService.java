package com.nxp.iemdm.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.model.search.GenericSearchArguments;
import com.nxp.iemdm.model.user.Person;
import org.springframework.data.domain.Page;

public interface GenericSearchService {

  Page<Person> searchPerson(GenericSearchArguments searchArgs, int page, int size);

  Page<Location> searchLocation(GenericSearchArguments searchArgs, int page, int size);

  Page<Manufacturer> searchManufacturer(GenericSearchArguments searchArgs, int page, int size);
}
