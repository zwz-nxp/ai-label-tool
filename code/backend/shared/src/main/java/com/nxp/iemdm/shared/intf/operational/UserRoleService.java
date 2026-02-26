package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.model.user.RoleAllowed;
import com.nxp.iemdm.model.user.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRoleService {
  // -- interface original UserRoleService
  Iterable<UserRole> getAllForUser(String wbi);

  List<RoleAllowed> getAllRolesAllowed(String wbi);

  boolean isUserAllowedForRole(String wbi, String roleId, Integer locationId);

  // --- interface UserRoleRepository
  List<UserRole> findAll();

  List<UserRole> findAllByUser(Person user);

  List<UserRole> findAllByRoleId(String roleName);

  List<UserRole> findAllByLocation(Location location);

  List<UserRole> findAllByUserAndRoleRoleId(Person user, String roleName);

  Set<Person> findAllUsersWithRoleForLocation(String roleName, List<Location> locations);

  // --- interface RoleRepository
  Optional<Role> findByRoleId(String roleId);
}
