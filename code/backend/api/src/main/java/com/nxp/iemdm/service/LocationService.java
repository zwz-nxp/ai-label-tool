package com.nxp.iemdm.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import java.util.List;

public interface LocationService {
  List<Location> getAllLocations();

  Location getLocationById(Integer locationID);

  void saveLocation(Location location);

  List<Location> getHistoryForLocation(Integer locationId);

  List<Location> getAllLocationsWithTmdbCode(String tmdbCode);

  void saveManufacturerCode(Manufacturer manufacturer);

  void deleteManufacturerByManufacturerCode(String manufacturerCode);

  Location getLocationBySapCode(String sapCode);
}
