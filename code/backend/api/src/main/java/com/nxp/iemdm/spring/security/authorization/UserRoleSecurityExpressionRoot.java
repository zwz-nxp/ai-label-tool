package com.nxp.iemdm.spring.security.authorization;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;

import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserRoleSecurityExpressionRoot extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {
  private Object filterObject;
  private Object returnObject;
  private Object target;
  private final String securityEnvironment;

  /**
   * Creates a new instance
   *
   * @param authentication the {@link Authentication} to use. Cannot be null.
   * @param securityEnvironment
   */
  public UserRoleSecurityExpressionRoot(Authentication authentication, String securityEnvironment) {
    super(authentication);
    this.securityEnvironment = securityEnvironment;
  }

  public boolean hasRoleForLocation(@NotNull String roleId, @NotNull Integer locationId) {
    if (securityEnvironment.equals(LOCAL_DEVELOPMENT_ENVIRONMENT)) {
      return true;
    }
    IEMDMPrincipal principal =
        (IEMDMPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Map<Integer, Set<Role>> userRoles = principal.getUserRoles();
    Set<Role> roles = userRoles.getOrDefault(0, new HashSet<>());
    roles.addAll(userRoles.getOrDefault(locationId, new HashSet<>()));
    return roles.stream().anyMatch(role -> role.getId().equals(roleId));
  }

  public boolean hasGlobalRole(@NotNull String roleId) {
    if (securityEnvironment.equals(LOCAL_DEVELOPMENT_ENVIRONMENT)) {
      return true;
    }
    IEMDMPrincipal principal =
        (IEMDMPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Map<Integer, Set<Role>> userRoles = principal.getUserRoles();

    return userRoles.getOrDefault(0, new HashSet<>()).stream()
        .anyMatch(role -> role.getId().equals(roleId));
  }

  public boolean hasRoleForAnyLocation(@NotNull String roleId) {
    if (securityEnvironment.equals(LOCAL_DEVELOPMENT_ENVIRONMENT)) {
      return true;
    }
    IEMDMPrincipal principal =
        (IEMDMPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    Map<Integer, Set<Role>> userRoles = principal.getUserRoles();

    return userRoles.values().stream()
        .flatMap(Collection::stream)
        .distinct()
        .anyMatch(role -> role.getId().equals(roleId));
  }

  @Override
  public void setFilterObject(Object filterObject) {
    this.filterObject = filterObject;
  }

  @Override
  public Object getFilterObject() {
    return filterObject;
  }

  @Override
  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public Object getReturnObject() {
    return returnObject;
  }

  public void setThis(Object target) {
    this.target = target;
  }

  @Override
  public Object getThis() {
    return target;
  }
}
