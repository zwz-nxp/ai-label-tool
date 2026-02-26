package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.model.user.UserRole;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleRepository extends CrudRepository<UserRole, UserRole.UserRoleKey> {

  @Transactional(readOnly = true)
  List<UserRole> findAllByUser(Person user);

  @Transactional(readOnly = true)
  List<UserRole> findAllByRole_IdAndLocation(String roleName, Location location);

  @Transactional(readOnly = true)
  List<UserRole> findAllByRole_Id(String roleName);

  @Transactional(readOnly = true)
  List<UserRole> findAllByLocation(Location location);

  @Transactional(readOnly = true)
  @Query("select ur.user from UserRole ur where ur.role.roleName = ?1 and ur.location in ?2")
  Set<Person> findAllUsersWithRoleForLocation(String roleName, List<Location> locations);

  @Transactional
  void deleteByUserAndRoleAndLocation(Person user, Role role, Location location);

  @Transactional
  List<UserRole> findByUserAndRole_IdAndLocation_Status(
      Person user, String id, LocationStatus status);
}
