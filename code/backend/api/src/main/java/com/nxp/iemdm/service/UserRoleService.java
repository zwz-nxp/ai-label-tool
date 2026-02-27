package com.nxp.iemdm.service;

import com.nxp.iemdm.model.user.RoleAllowed;
import com.nxp.iemdm.model.user.UserRole;
import java.util.List;

public interface UserRoleService {
  List<UserRole> getAllRolesForUser(String wbi);

  UserRole addUserToRoleForLocation(
      String wbi, String roleId, Integer locationId, String updatedBy);

  void removeUserFromRoleForLocation(
      String wbi, String roleId, Integer locationId, String updatedBy);

  List<RoleAllowed> getAllRolesAllowed(String wbi);
}
