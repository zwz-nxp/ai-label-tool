package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.model.location.SapCode;
import com.nxp.iemdm.service.LocationService;
import com.nxp.iemdm.service.SapCodeService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationServiceREST implements LocationService {

  private final RestTemplate restTemplate;
  private final String locationServiceURI;
  private final SapCodeService sapCodeService;

  @Autowired
  public LocationServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.locationservice.uri}") String locationServiceURI,
      SapCodeService sapCodeService) {
    this.restTemplate = restTemplate;
    this.locationServiceURI = locationServiceURI + "/location";
    this.sapCodeService = sapCodeService;
  }

  @MethodLog
  @Override
  public List<Location> getAllLocations() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(locationServiceURI + "/all", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public Location getLocationById(Integer locationID) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", locationID);

    ResponseEntity<Location> responseEntity =
        this.restTemplate.getForEntity(locationServiceURI + "/{id}", Location.class, params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public void deleteManufacturerByManufacturerCode(String manufacturerCode) {
    this.restTemplate.delete(locationServiceURI + "/deleteManufacturerCode/" + manufacturerCode);
  }

  @Override
  public void saveLocation(Location location) {
    this.restTemplate.exchange(
        locationServiceURI + "/create",
        HttpMethod.POST,
        new HttpEntity<>(location),
        ResponseEntity.class);
  }

  @Override
  public List<Location> getHistoryForLocation(Integer locationId) {
    Map<String, Object> params = new HashMap<>();
    params.put("id", locationId);

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(
            locationServiceURI + "/{id}/history", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public List<Location> getAllLocationsWithTmdbCode(String tmdbCode) {
    Map<String, Object> params = new HashMap<>();

    params.put("tmdbcode", tmdbCode);

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(
            locationServiceURI + "/all?tmdbcode={tmdbcode}", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public void saveManufacturerCode(Manufacturer manufacturer) {
    this.restTemplate.exchange(
        locationServiceURI + "/saveManufacturerCode",
        HttpMethod.POST,
        new HttpEntity<>(manufacturer),
        ResponseEntity.class);
  }

  public Location getLocationBySapCode(String sapCode) {
    List<Location> locations = this.getAllLocations();
    return locations.stream()
        .filter(location -> sapCode.equalsIgnoreCase(location.getSapCode()))
        .findFirst()
        .or(
            () -> {
              SapCode sap = this.sapCodeService.getSapCode(sapCode);
              if (sap == null || sap.getManagedBy() == null) {
                return Optional.empty();
              }
              return Optional.of(sap.getManagedBy());
            })
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format("No location found for sap code %s", sapCode)));
  }
}
