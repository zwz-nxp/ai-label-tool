package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.model.user.RoleAllowed;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.service.RoleService;
import com.nxp.iemdm.service.UserRoleService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization")
public class UserRoleController {

  private final RoleService roleService;
  private final UserRoleService userRoleService;

  @Autowired
  public UserRoleController(RoleService roleService, UserRoleService userRoleService) {
    this.roleService = roleService;
    this.userRoleService = userRoleService;
  }

  @MethodLog
  @GetMapping(path = "/roles/all", produces = MediaType.APPLICATION_JSON)
  public List<Role> getAllRoles() {
    return roleService.getAllRoles().stream().filter(this::isNotDeveloperRole).toList();
  }

  @MethodLog
  @GetMapping(path = "/{wbi}/roles", produces = MediaType.APPLICATION_JSON)
  public List<UserRole> getAllRolesForUser(@PathVariable("wbi") String wbi) {
    return userRoleService.getAllRolesForUser(wbi);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_User') ")
  @PostMapping(path = "/{wbi}/{locationid}/{roleid}", produces = MediaType.APPLICATION_JSON)
  public UserRole addUserToRoleForLocation(
      @PathVariable("wbi") @NotBlank String wbi,
      @PathVariable("roleid") @NotNull String roleId,
      @PathVariable("locationid") @NotNull Integer locationId,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    return userRoleService.addUserToRoleForLocation(wbi, roleId, locationId, user.getUsername());
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_User') ")
  @DeleteMapping(path = "/{wbi}/{locationid}/{roleid}")
  public void removeUserFromRoleForLocation(
      @PathVariable("wbi") @NotBlank String wbi,
      @PathVariable("roleid") @NotNull String roleId,
      @PathVariable("locationid") @NotNull Integer locationId,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    userRoleService.removeUserFromRoleForLocation(wbi, roleId, locationId, user.getUsername());
  }

  @MethodLog
  @GetMapping(path = "/{wbi}/getAllRolesAllowed", produces = MediaType.APPLICATION_JSON)
  public List<RoleAllowed> getAllRolesAllowed(@PathVariable("wbi") String wbi) {
    return userRoleService.getAllRolesAllowed(wbi);
  }

  private boolean isNotDeveloperRole(Role role) {
    return !"Developer".equals(role.getId());
  }
}
