package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.location.Location;
import java.util.List;

public interface LocationService {
  Iterable<Location> getAllLocations(String tmdbCode);

  Location getLocationById(Integer locationId) throws NotFoundException;

  List<Location> getHistoryForLocationById(Integer locationId);

  void saveLocation(Location location, String wbi);

  void deleteLocation(Integer locationid) throws NotFoundException;

  List<Location> findAllByStatusOrderByAcronym(LocationStatus status);

  List<Location> findAllActiveOrderByAcronymOmitGlobal();
}
