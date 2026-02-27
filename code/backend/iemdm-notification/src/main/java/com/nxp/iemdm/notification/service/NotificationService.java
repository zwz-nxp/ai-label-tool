package com.nxp.iemdm.notification.service;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private static final String ROLE_SITE_PLANNER = "Manager_Planning_Site";
  private static final String ROLE_SITE_ENGINEER = "Manager_Equipment_Site";

  public boolean isPersonPlannerOrEngineerForLocation(Person person, Location location) {
    Set<Role> roles = new HashSet<>();
    roles.addAll(person.getRoles().getOrDefault(0, new HashSet<>()));
    roles.addAll(person.getRoles().getOrDefault(location.getId(), new HashSet<>()));
    return roles.stream()
        .anyMatch(
            role ->
                role.getId().equals(ROLE_SITE_PLANNER) || role.getId().equals(ROLE_SITE_ENGINEER));
  }

  // --------------- private methods -------------------
}
