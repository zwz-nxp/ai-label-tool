package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.operational.repository.audit.LocationAuditRepository;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.LocationService;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import com.nxp.iemdm.shared.repository.jpa.LocationRepository;
import com.nxp.iemdm.shared.repository.jpa.ManufacturerRepository;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.history.Revision;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/location")
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;
  private final LocationAuditRepository locationAuditRepository;
  private final UpdateService updateService;
  private final ManufacturerRepository manufacturerRepository;

  public LocationServiceImpl(
      LocationRepository locationRepository,
      LocationAuditRepository locationAuditRepository,
      UpdateService updateService,
      ManufacturerRepository manufacturerRepository) {
    this.locationRepository = locationRepository;
    this.locationAuditRepository = locationAuditRepository;
    this.updateService = updateService;
    this.manufacturerRepository = manufacturerRepository;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<Location> getAllLocations(
      @RequestParam(value = "tmdbcode", required = false) String tmdbCode) {
    Iterable<Location> locations;
    if (StringUtils.hasText(tmdbCode)) {
      locations =
          this.locationRepository.findAllByTmdbCodeAndStatus(tmdbCode, LocationStatus.ACTIVE);
    } else {
      locations = this.locationRepository.findAllByStatusOrderByAcronym(LocationStatus.ACTIVE);
    }
    return locations;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  public Location getLocationById(@PathVariable("id") Integer locationId) throws NotFoundException {
    Location location =
        this.locationRepository.findById(locationId).orElseThrow(NotFoundException::new);
    return location;
  }

  @MethodLog
  @DeleteMapping(path = "/deleteManufacturerCode/{manufacturerCode}")
  public void deleteManufacturerCodeById(@PathVariable String manufacturerCode) {
    this.manufacturerRepository.deleteByManufacturerCode(manufacturerCode);
    this.handleLocationUpdate(null);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON)
  public List<Location> getHistoryForLocationById(@PathVariable("id") Integer locationId) {
    return this.locationAuditRepository.findRevisions(locationId).stream()
        .map(Revision::getEntity)
        .collect(Collectors.toList());
  }

  @MethodLog
  @Transactional
  @PostMapping("/create")
  public void saveLocation(
      @RequestBody Location location, @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    location.setLastUpdated(Instant.now());
    location.setUpdatedBy(wbi);
    if (location.getStatus() == null) {
      location.setStatus(LocationStatus.ACTIVE);
    }
    this.validateLocation(location);
    Location savedLocation = this.locationRepository.save(location);
    this.handleLocationUpdate(savedLocation.getId());
  }

  @MethodLog
  @Transactional
  @PostMapping("/saveManufacturerCode")
  public void saveManufacturerCode(@RequestBody Manufacturer manufacturer) {
    try {
      this.manufacturerRepository.save(manufacturer);
    } catch (DataIntegrityViolationException exception) {
      throw new BadRequestException(
          String.format(
              "Manufacturer Code '%s' has already been used", manufacturer.getManufacturerCode()));
    }
    this.handleLocationUpdate(null);
  }

  @MethodLog
  @Transactional
  @DeleteMapping(
      path = "/{id}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public void deleteLocation(@PathVariable("id") Integer locationId) throws NotFoundException {
    Location location =
        this.locationRepository.findById(locationId).orElseThrow(NotFoundException::new);
    location.setStatus(LocationStatus.DELETED);
    this.locationRepository.save(location);
    this.handleLocationUpdate(locationId);
  }

  @Override
  public List<Location> findAllByStatusOrderByAcronym(LocationStatus status) {
    return this.locationRepository.findAllByStatusOrderByAcronym(status);
  }

  @Override
  public List<Location> findAllActiveOrderByAcronymOmitGlobal() {
    return this.locationRepository.findAllByStatusOrderByAcronym(LocationStatus.ACTIVE).stream()
        .filter(Location::isNotGlobalLocation)
        .collect(Collectors.toList());
  }

  private void handleLocationUpdate(Integer locationId) {
    Update update =
        new Update(UpdateType.LOCATION, Objects.requireNonNullElse(locationId, 0), null, null);
    this.updateService.update(update);
  }

  private void validateLocation(Location location) {
    StringBuilder errorBuilder = new StringBuilder();
    boolean hasError = false;

    if (!StringUtils.hasText(location.getAcronym())) {
      hasError = true;
      errorBuilder.append("Acronym cannot be empty").append(System.lineSeparator());
    }

    Optional<Location> existingLocation = locationRepository.findByAcronym(location.getAcronym());

    if (existingLocation.isPresent()
        && !Objects.equals(existingLocation.get().getId(), location.getId())) {
      hasError = true;
      errorBuilder.append("Acronym must be unique").append(System.lineSeparator());
    }

    if (location.getIsSubContractor()) {
      if (!StringUtils.hasText(location.getVendorCode())) {
        hasError = true;
        errorBuilder
            .append("Vendor Code cannot be empty for SubCon Locations")
            .append(System.lineSeparator());
      }
    } else {
      if (StringUtils.hasText(location.getVendorCode())) {
        hasError = true;
        errorBuilder
            .append("A non-SubCon Location can not have a Vendor Code")
            .append(System.lineSeparator());
      }
    }

    if (hasError) {
      throw new BadRequestException(errorBuilder.toString());
    }
  }
}
