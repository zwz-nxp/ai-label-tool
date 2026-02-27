package com.nxp.iemdm.capacitystatementservice.service.rest;

import com.nxp.iemdm.capacitystatementservice.service.LocationService;
import com.nxp.iemdm.model.location.Location;
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
public class LocationServiceREST implements LocationService {

  private final RestTemplate restTemplate;
  private final String locationServiceURI;

  @Autowired
  public LocationServiceREST(
      RestTemplate restTemplate, @Value("${rest.locationservice.uri}") String locationServiceURI) {
    this.restTemplate = restTemplate;
    this.locationServiceURI = locationServiceURI;
  }

  @MethodLog
  @Override
  public List<Location> getAllLocations() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(
            locationServiceURI + "/location/all", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public Location getLocationById(Integer locationID) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", locationID);

    ResponseEntity<Location> responseEntity =
        this.restTemplate.getForEntity(
            locationServiceURI + "/location/{id}", Location.class, params);

    return responseEntity.getBody();
  }

  @Override
  public List<Location> getAllLocationsWithTmdbCode(String tmdbCode) {
    Map<String, Object> params = new HashMap<>();

    params.put("tmdbcode", tmdbCode);

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(
            locationServiceURI + "/location/all?tmdbcode={tmdbcode}", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }
}
