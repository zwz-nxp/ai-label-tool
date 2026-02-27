package com.nxp.iemdm.capacitystatementservice.service;

import com.nxp.iemdm.model.location.Location;
import java.util.List;

public interface LocationService {
  List<Location> getAllLocations();

  Location getLocationById(Integer locationID);

  List<Location> getAllLocationsWithTmdbCode(String tmdbCode);
}
