package com.nxp.iemdm.builder;

import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.location.Location;
import java.time.Instant;

public final class LocationBuilder {
  private Integer id;
  private String acronym;
  private String city;
  private String country;
  private String sapCode;
  private String tmdbCode;
  private String planningEngine;
  private LocationStatus status;
  private Instant lastUpdated;
  private String updatedBy;
  private String extendedSuffix;

  private LocationBuilder() {}

  public static LocationBuilder builder() {
    return new LocationBuilder();
  }

  public LocationBuilder withId(Integer id) {
    this.id = id;
    return this;
  }

  public LocationBuilder withAcronym(String acronym) {
    this.acronym = acronym;
    return this;
  }

  public LocationBuilder withCity(String city) {
    this.city = city;
    return this;
  }

  public LocationBuilder withCountry(String country) {
    this.country = country;
    return this;
  }

  public LocationBuilder withSapCode(String sapCode) {
    this.sapCode = sapCode;
    return this;
  }

  public LocationBuilder withTmdbCode(String tmdbCode) {
    this.tmdbCode = tmdbCode;
    return this;
  }

  public LocationBuilder withPlanningEngine(String planningEngine) {
    this.planningEngine = planningEngine;
    return this;
  }

  public LocationBuilder withStatus(LocationStatus status) {
    this.status = status;
    return this;
  }

  public LocationBuilder withLastUpdated(Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

  public LocationBuilder withUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public LocationBuilder withExtendedSuffix(String extendedSuffix) {
    this.extendedSuffix = extendedSuffix;
    return this;
  }

  /**
   * This populates all fields with some initial data
   *
   * @return LocationBuilder
   */
  public LocationBuilder withTestData() {
    this.id = 1;
    this.acronym = BuilderConstants.SITE_ATKH;
    this.city = "Kaohsiung";
    this.country = "Taiwan";
    this.sapCode = "TW70";
    this.tmdbCode = "1280";
    this.planningEngine = "I2";
    this.status = LocationStatus.ACTIVE;
    this.updatedBy = "nxf45643";
    this.lastUpdated = Instant.now();
    this.updatedBy = "nxf45643";
    this.extendedSuffix = "suffix";
    return this;
  }

  public Location build() {
    Location location = new Location();
    location.setId(this.id);
    location.setAcronym(this.acronym);
    location.setCity(this.city);
    location.setCountry(this.country);
    location.setSapCode(this.sapCode);
    location.setTmdbCode(this.tmdbCode);
    location.setPlanningEngine(this.planningEngine);
    location.setStatus(this.status);
    location.setLastUpdated(this.lastUpdated);
    location.setUpdatedBy(this.updatedBy);
    location.setExtendedSuffix(this.extendedSuffix);
    return location;
  }
}
