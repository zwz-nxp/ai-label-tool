package com.nxp.iemdm.initialaudit.service;

import com.nxp.iemdm.initialaudit.service.jpa.EplanUploadRepository;
import com.nxp.iemdm.initialaudit.service.jpa.EquipmentCodeRepository;
import com.nxp.iemdm.initialaudit.service.jpa.EquipmentRepository;
import com.nxp.iemdm.initialaudit.service.jpa.LocalPlanningRepository;
import com.nxp.iemdm.initialaudit.service.jpa.LocationRepository;
import com.nxp.iemdm.initialaudit.service.jpa.ResourceClassRepository;
import com.nxp.iemdm.initialaudit.service.jpa.ResourceGroupRepository;
import com.nxp.iemdm.model.Equipment;
import com.nxp.iemdm.model.EquipmentCode;
import com.nxp.iemdm.model.Location;
import com.nxp.iemdm.model.ResourceClass;
import com.nxp.iemdm.model.ResourceGroup;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/audit")
public class AuditService {

  private final RestTemplate restTemplate;
  private final String syncServiceURI;

  private final LocationRepository locationRepository;
  private final ResourceClassRepository resourceClassRepository;
  private final ResourceGroupRepository resourceGroupRepository;
  private final EquipmentCodeRepository equipmentCodeRepository;
  private final EquipmentRepository equipmentRepository;
  private final LocalPlanningRepository localPlanningRepository;
  private final EplanUploadRepository eplanUploadRepository;
  private final DataSource dataSource;

  @Autowired
  public AuditService(
      RestTemplate restTemplate,
      @Value("${rest.syncserivce.uri}") String syncServiceURI,
      LocationRepository locationRepository,
      ResourceClassRepository resourceClassRepository,
      ResourceGroupRepository resourceGroupRepository,
      EquipmentCodeRepository equipmentCodeRepository,
      EquipmentRepository equipmentRepository,
      LocalPlanningRepository localPlanningRepository,
      EplanUploadRepository eplanUploadRepository,
      DataSource dataSource) {
    this.restTemplate = restTemplate;
    this.syncServiceURI = syncServiceURI;
    this.locationRepository = locationRepository;
    this.resourceClassRepository = resourceClassRepository;
    this.resourceGroupRepository = resourceGroupRepository;
    this.equipmentCodeRepository = equipmentCodeRepository;
    this.equipmentRepository = equipmentRepository;
    this.localPlanningRepository = localPlanningRepository;
    this.eplanUploadRepository = eplanUploadRepository;
    this.dataSource = dataSource;
  }

  @GetMapping("/initial")
  public void performInitialAudit() throws SQLException {
    List<Location> locations = getAllLocations();
    List<ResourceClass> resourceClasses = getAllResourceClasses();
    List<ResourceGroup> resourceGroups = getAllResourceGroups();
    List<EquipmentCode> equipmentCodes = getAllEquipmentCodes();
    List<Equipment> equipments = getAllEquipment();

    System.out.println("Received stuff");

    Connection connection = dataSource.getConnection();

    connection
        .createStatement()
        .execute("alter table global_user disable constraint fk_primary_location");
    connection
        .createStatement()
        .execute("alter table global_user_role disable constraint fk_user_role_location");
    connection
        .createStatement()
        .execute("alter table local_event disable constraint fk_event_location");
    connection
        .createStatement()
        .execute("alter table local_activity_log disable constraint fk_act_log_location");
    connection
        .createStatement()
        .execute(
            "alter table global_cap_statement_record disable constraint fk_cap_stat_rec_location");
    connection
        .createStatement()
        .execute("alter table local_planning_flag disable constraint fk_planning_flag_rg");
    connection
        .createStatement()
        .execute("alter table local_planning_flag disable constraint fk_planning_flag_rc");
    connection
        .createStatement()
        .execute("alter table local_planning_flag disable constraint fk_planning_flag_site");
    connection
        .createStatement()
        .execute(
            "alter table local_planning_flag_ctrl disable constraint fk_planning_flag_control_rc");
    connection
        .createStatement()
        .execute(
            "alter table local_planning_flag_ctrl disable constraint fk_planning_flag_control_site");

    System.out.println("Disabled constraints");

    eplanUploadRepository.deleteAll();
    localPlanningRepository.deleteAll();
    equipmentRepository.deleteAll();
    equipmentCodeRepository.deleteAll();
    resourceGroupRepository.deleteAll();
    resourceClassRepository.deleteAll();
    locationRepository.deleteAll();

    System.out.println("Deleted stuff");

    locations.forEach(this::saveLocation);

    System.out.println("Saved locations");

    resourceClasses.forEach(this::saveResourceClass);

    System.out.println("Saved resource classes");

    resourceGroups.forEach(this::saveResourceGroup);

    System.out.println("Saved resource groups");

    equipmentCodes.forEach(this::saveEquipmentCode);

    System.out.println("Saved equipment codes");

    equipments.forEach(this::saveEquipment);

    System.out.println("Saved equipment");

    connection
        .createStatement()
        .execute("alter table global_user enable constraint fk_primary_location");
    connection
        .createStatement()
        .execute("alter table global_user_role enable constraint fk_user_role_location");
    connection
        .createStatement()
        .execute("alter table local_event enable constraint fk_event_location");
    connection
        .createStatement()
        .execute("alter table local_activity_log enable constraint fk_act_log_location");
    connection
        .createStatement()
        .execute(
            "alter table global_cap_statement_record enable constraint fk_cap_stat_rec_location");
    connection
        .createStatement()
        .execute("alter table local_planning_flag enable constraint fk_planning_flag_rg");
    connection
        .createStatement()
        .execute("alter table local_planning_flag enable constraint fk_planning_flag_rc");
    connection
        .createStatement()
        .execute("alter table local_planning_flag enable constraint fk_planning_flag_site");
    connection
        .createStatement()
        .execute(
            "alter table local_planning_flag_ctrl enable constraint fk_planning_flag_control_rc");
    connection
        .createStatement()
        .execute(
            "alter table local_planning_flag_ctrl enable constraint fk_planning_flag_control_site");

    System.out.println("Enabled constraints");

    localPlanningRepository.initPlanning(0);

    System.out.println("Finished init");
  }

  private List<Equipment> getAllEquipment() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Equipment[]> responseEntity =
        this.restTemplate.getForEntity(
            syncServiceURI + "/equipment/all", Equipment[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  private List<EquipmentCode> getAllEquipmentCodes() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<EquipmentCode[]> responseEntity =
        this.restTemplate.getForEntity(
            syncServiceURI + "/equipmentcode/all", EquipmentCode[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  private List<ResourceGroup> getAllResourceGroups() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<ResourceGroup[]> responseEntity =
        this.restTemplate.getForEntity(
            syncServiceURI + "/resourcegroup/all", ResourceGroup[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  private List<ResourceClass> getAllResourceClasses() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<ResourceClass[]> responseEntity =
        this.restTemplate.getForEntity(
            syncServiceURI + "/resourceclass/all", ResourceClass[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  private List<Location> getAllLocations() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Location[]> responseEntity =
        this.restTemplate.getForEntity(syncServiceURI + "/location/all", Location[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  private void saveLocation(Location location) {
    Map<String, Object> params = new HashMap<>();

    this.restTemplate.postForEntity(syncServiceURI + "/location", location, Location.class, params);
  }

  private void saveResourceClass(ResourceClass resourceClass) {
    Map<String, Object> params = new HashMap<>();

    this.restTemplate.postForEntity(
        syncServiceURI + "/resourceclass", resourceClass, ResourceClass.class, params);
  }

  private void saveResourceGroup(ResourceGroup resourceGroup) {
    Map<String, Object> params = new HashMap<>();

    this.restTemplate.postForEntity(
        syncServiceURI + "/resourcegroup", resourceGroup, ResourceGroup.class, params);
  }

  private void saveEquipmentCode(EquipmentCode equipmentCode) {
    Map<String, Object> params = new HashMap<>();

    this.restTemplate.postForEntity(
        syncServiceURI + "/equipmentcode", equipmentCode, EquipmentCode.class, params);
  }

  private void saveEquipment(Equipment equipment) {
    Map<String, Object> params = new HashMap<>();

    this.restTemplate.postForEntity(
        syncServiceURI + "/equipment", equipment, Equipment.class, params);
  }
}
