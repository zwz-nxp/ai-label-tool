package com.nxp.iemdm.spring.security;

import com.nxp.iemdm.model.user.Role;
import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class IEMDMPrincipal implements UserDetails, AuthenticatedPrincipal {

  @Serial private static final long serialVersionUID = 8283002768625625926L;

  private final User user;

  @Getter private final Map<Integer, Set<Role>> userRoles;

  public IEMDMPrincipal(User user, Map<Integer, Set<Role>> userRoles) {
    this.user = user;
    this.userRoles = Collections.unmodifiableMap(userRoles);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return user.getAuthorities();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return user.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return user.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  @Override
  public String getName() {
    return this.user.getUsername();
  }
}
