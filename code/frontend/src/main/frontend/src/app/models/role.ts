export class Role {
  id = "";
  roleName = "";
  global = false;
  description = "";
}

export class RoleUtils {
  public static cloneRole(source: Role): Role {
    if (source) {
      const target = new Role();
      target.id = source.id;
      target.roleName = source.roleName;
      target.global = source.global;
      target.description = source.description;
      return target;
    } else {
      return new Role();
    }
  }
}

export enum RoleEnum {
  ADMINISTRATOR_SYSTEM = "Administrator_System",
  ADMINISTRATOR_USER = "Administrator_User",
  MANAGER_PLANNING_SITE = "Manager_Planning_Site",
  MANAGER_EQUIPMENT_SITE = "Manager_Equipment_Site",
  APPROVER_PLANNING_SITE = "Approver_Planning_Site",
  APPROVER_EQUIPMENT_SITE = "Approver_Equipment_Site",
  MANAGER_EQUIPMENTCODES_GLOBAL = "Manager_EquipmentCodes_Global",
  APPROVER_EQUIPMENTCODES_GLOBAL = "Approver_EquipmentCodes_Global",
  MANAGER_PLANNINGGROUPS_GLOBAL = "Manager_PlanningGroups_Global",
  APPROVER_PLANNINGGROUPS_GLOBAL = "Approver_PlanningGroups_Global",
  DEVELOPER = "Developer",
  SITE_YIELD_DATA_STEWARD = "Site_yield_steward",
  FINANCIAL_YIELD_STEWARD = "Financial_Yield_steward",
  SITE_PLANNING_FLAG_STEWARD = "Site_Planning_Flag_Steward",
  YIELD_ADMIN = "Yield_Admin",
  ADC_ENGINEER = "ADC_Engineer",
}
