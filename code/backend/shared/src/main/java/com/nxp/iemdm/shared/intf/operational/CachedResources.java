package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CachedResources {

  Optional<Location> getLocation(int locationId);

  Collection<Location> getLocations();

  Optional<Person> getPerson(String wbi);

  String getSitAcronym(int locationId);

  void update(Update update);

  GlobalLookupData getGlobalLookupData();

  Map<String, String> getConfigValueItemMap();

  boolean isSubCon(int locationId);

  boolean isSubCon(String acronym);
}
