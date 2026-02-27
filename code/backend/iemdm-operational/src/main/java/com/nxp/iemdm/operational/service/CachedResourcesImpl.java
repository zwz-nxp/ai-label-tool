package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.ConfigurationValueItem;
import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.operational.service.rest.LookupDataService;
import com.nxp.iemdm.shared.intf.operational.CachedResources;
import com.nxp.iemdm.shared.repository.jpa.ConfigurationValueItemRepository;
import com.nxp.iemdm.shared.repository.jpa.LocationRepository;
import com.nxp.iemdm.shared.repository.jpa.PersonRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachedResourcesImpl implements CachedResources {

  private final LocationRepository locationRepository;
  private final PersonRepository personRepository;
  private final LookupDataService lookupDataService;

  private final Map<Integer, Location> sSiteMap = new HashMap<>();
  private final Map<String, Person> sPersonMap = new HashMap<>();
  private final ConfigurationValueItemRepository configurationValueItemRepository;
  private GlobalLookupData globalLookupData;
  private Map<String, String> configValuesMap;
  private final Set<Integer> subconLocationIds = new HashSet<>();
  private final Set<String> subconAcronyms = new HashSet<>();

  @Autowired
  public CachedResourcesImpl(
      LocationRepository locationRepository,
      PersonRepository personRepository,
      LookupDataService lookupDataService,
      ConfigurationValueItemRepository configurationValueItemRepository) {
    super();
    this.locationRepository = locationRepository;
    this.personRepository = personRepository;
    this.lookupDataService = lookupDataService;
    this.configurationValueItemRepository = configurationValueItemRepository;
  }

  @PostConstruct
  public void init() {
    this.updateLocations();
    this.globalLookupData = this.lookupDataService.getGlobalLookupData();
    this.configValuesMap =
        StreamSupport.stream(this.configurationValueItemRepository.findAll().spliterator(), false)
            .collect(
                Collectors.toMap(
                    ConfigurationValueItem::getConfigurationKey,
                    ConfigurationValueItem::getConfigurationValue));
  }

  @Override
  public Optional<Location> getLocation(int locationId) {
    if (this.sSiteMap.containsKey(locationId)) {
      return Optional.of(this.sSiteMap.get(locationId));
    } else {
      Optional<Location> locOpt = this.locationRepository.findById(locationId);

      if (locOpt.isPresent() && !this.sSiteMap.containsKey(locationId)) {
        this.sSiteMap.put(locationId, locOpt.get());
      }

      return locOpt;
    }
  }

  @Override
  public Optional<Person> getPerson(String wbi) {
    if (this.sPersonMap.containsKey(wbi)) {
      return Optional.of(this.sPersonMap.get(wbi));
    } else {
      Optional<Person> personOpt = this.personRepository.findByWbiIgnoreCase(wbi);

      if (personOpt.isPresent() && !this.sPersonMap.containsKey(wbi)) {
        this.sPersonMap.put(wbi, personOpt.get());
      }

      return personOpt;
    }
  }

  @Override
  public String getSitAcronym(int locationId) {
    return this.getLocation(locationId).get().getAcronym();
  }

  @Override
  public Collection<Location> getLocations() {
    return this.sSiteMap.values();
  }

  @Override
  public GlobalLookupData getGlobalLookupData() {
    return this.globalLookupData;
  }

  @Override
  public void update(Update update) {
    if (UpdateType.LOCATION.equals(update.getUpdatedType())) {
      this.updateLocations();
    } else if (UpdateType.PERSON.equals(
            update.getUpdatedType()) // because 'usernames' may be modified
        || UpdateType.CONFIG_VALUE_ITEM.equals(update.getUpdatedType())
        || UpdateType.EQUIPMENTCODE.equals(update.getUpdatedType())
        || UpdateType.SAPCODE.equals(update.getUpdatedType())) {
      this.globalLookupData = this.lookupDataService.getGlobalLookupData();
    } else {
      // do nothing
    }
  }

  @Override
  public Map<String, String> getConfigValueItemMap() {
    return this.configValuesMap;
  }

  @Override
  public boolean isSubCon(int locationId) {
    return this.subconLocationIds.contains(Integer.valueOf(locationId));
  }

  @Override
  public boolean isSubCon(String acronym) {
    return this.subconAcronyms.contains(acronym);
  }

  // ----------- private -----------

  private void updateLocations() {
    this.sSiteMap.clear();
    this.locationRepository.findAll().forEach(site -> this.sSiteMap.put(site.getId(), site));

    this.subconLocationIds.clear();
    this.subconAcronyms.clear();
    for (Location loc : this.sSiteMap.values()) {
      if (loc.getIsSubContractor()) {
        this.subconLocationIds.add(loc.getId());
        this.subconAcronyms.add(loc.getAcronym());
      }
    }
  }
}
