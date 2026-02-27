package com.nxp.iemdm.controller;

import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.service.LocationService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
public class LocationController {

  private final LocationService locationService;

  public LocationController(LocationService locationService) {
    this.locationService = locationService;
  }

  @MethodLog
  @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON)
  public List<Location> getHistoryForLocation(@PathVariable("id") Integer locationId) {
    return this.locationService.getHistoryForLocation(locationId);
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<Location> getAllLocations() {
    return this.locationService.getAllLocations();
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @PostMapping("/create")
  public void saveLocation(@RequestBody @Valid Location location) {
    this.locationService.saveLocation(location);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @DeleteMapping(path = "/{locationId}")
  public void deleteLocation(@PathVariable @NotNull Integer locationId) {
    Location location = this.locationService.getLocationById(locationId);
    location.setStatus(LocationStatus.DELETED);
    this.locationService.saveLocation(location);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @PostMapping("/saveManufacturerCode")
  public void saveManufacturerCode(
      @RequestBody @Valid Manufacturer manufacturer, @AuthenticationPrincipal IEMDMPrincipal user) {
    manufacturer.setUpdatedBy(user.getUsername());
    this.locationService.saveManufacturerCode(manufacturer);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @DeleteMapping(path = "/deleteManufacturerCode/{manufacturerCode}")
  public void deleteManufacturerCode(@PathVariable @NotNull String manufacturerCode) {
    this.locationService.deleteManufacturerByManufacturerCode(manufacturerCode);
  }
}
