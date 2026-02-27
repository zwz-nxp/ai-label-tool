package com.nxp.iemdm.builder;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PersonBuilder {
  private String wbi;
  private String name;
  private String pictureURL;
  private String email;
  private Location primaryLocation;
  private Map<Integer, Set<Role>> roles = new HashMap<>();
  private Instant lastLogin;
  private Instant lastUpdated;
  private String updatedBy;

  private PersonBuilder() {}

  public static PersonBuilder builder() {
    return new PersonBuilder();
  }

  public PersonBuilder withWbi(String wbi) {
    this.wbi = wbi;
    return this;
  }

  public PersonBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public PersonBuilder withPictureURL(String pictureURL) {
    this.pictureURL = pictureURL;
    return this;
  }

  public PersonBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public PersonBuilder withPrimaryLocation(Location primaryLocation) {
    this.primaryLocation = primaryLocation;
    return this;
  }

  public PersonBuilder withRoles(Map<Integer, Set<Role>> roles) {
    this.roles = roles;
    return this;
  }

  public PersonBuilder withLastLogin(Instant lastLogin) {
    this.lastLogin = lastLogin;
    return this;
  }

  public PersonBuilder withTestData() {
    this.lastLogin = Instant.now();
    this.wbi = "nxf45365";
    this.name = "Tester";
    this.pictureURL = null;
    this.email = "nxf45365@nxp.com";
    this.primaryLocation = LocationBuilder.builder().withAcronym("ATKH").withId(1).build();
    this.roles = new HashMap<>();
    this.lastUpdated = Instant.now();
    this.updatedBy = "iemdm";
    return this;
  }

  public Person build() {
    Person person = new Person();
    person.setWbi(wbi);
    person.setName(name);
    person.setPictureURL(pictureURL);
    person.setEmail(email);
    person.setPrimaryLocation(primaryLocation);
    person.setRoles(roles);
    person.setLastLogin(lastLogin);
    person.setLastUpdated(lastUpdated);
    person.setUpdatedBy(updatedBy);
    return person;
  }
}
