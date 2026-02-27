package com.nxp.iemdm.enums.user;

/** This enum should be in sync with GLOBAL_ROLE table. */
public enum UserRoleType {

  // NOTE: Be careful with UserRoleEnum.<ENUM>.name(), you probable want to use: getName() because
  // that is the value stored in the dbs.

  ADMINISTRATOR_SYSTEM("Administrator_System", true, "Administrator_System"),
  ADMINISTRATOR_USER("Administrator_User", true, "Administrator_User"),
  MANAGER_PLANNING_SITE("Manager_Planning_Site", false, "Manager_Planning_Site"),
  MANAGER_EQUIPMENT_SITE("Manager_Equipment_Site", false, "Manager_Equipment_Site"),
  APPROVER_PLANNING_SITE("Approver_Planning_Site", false, "Approver_Planning_Site"),
  APPROVER_EQUIPMENT_SITE("Approver_Equipment_Site", false, "Approver_Equipment_Site"),
  MANAGER_EQUIPMENTCODES_GLOBAL(
      "Manager_EquipmentCodes_Global", true, "Manager_EquipmentCodes_Global"),
  APPROVER_EQUIPMENTCODES_GLOBAL(
      "Approver_EquipmentCodes_Global", true, "Approver_EquipmentCodes_Global"),
  MANAGER_PLANNINGGROUPS_GLOBAL(
      "Manager_PlanningGroups_Global", true, "Manager_PlanningGroups_Global"),
  APPROVER_PLANNINGGROUPS_GLOBAL(
      "Approver_PlanningGroups_Global", true, "Approver_PlanningGroups_Global"),
  DEVELOPER("Developer", true, "Developer"),
  FINANCIAL_YIELD_STEWARD("Financial_Yield_steward", true, "Financial Yield steward"),
  SITE_YIELD_STEWARD("Site_yield_steward", true, "Site yield steward");

  //  CYCLE_TIME_STEWARD("Cycle_time_steward", true, "Cycle time steward");

  UserRoleType(String name, boolean global, String description) {
    this.name = name;
    this.global = global;
    this.description = description;
  }

  private final String name;
  private final boolean global;
  private final String description;

  public String getName() {
    return name;
  }

  public boolean isGlobal() {
    return global;
  }

  public String getDescription() {
    return description;
  }
}
