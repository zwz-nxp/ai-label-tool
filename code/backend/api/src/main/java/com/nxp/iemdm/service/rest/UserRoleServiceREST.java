package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.user.RoleAllowed;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.service.UserRoleService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserRoleServiceREST implements UserRoleService {
  private final RestTemplate restTemplate;
  private final String userRoleServiceUri;

  @Autowired
  public UserRoleServiceREST(
      RestTemplate restTemplate, @Value("${rest.userroleservice.uri}") String userRoleServiceUri) {
    this.restTemplate = restTemplate;
    this.userRoleServiceUri = userRoleServiceUri;
  }

  @MethodLog
  @Override
  public List<UserRole> getAllRolesForUser(String wbi) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);

    ResponseEntity<UserRole[]> responseEntity =
        this.restTemplate.getForEntity(
            userRoleServiceUri + "/userrole/{wbi}/all", UserRole[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public UserRole addUserToRoleForLocation(
      String wbi, String roleId, Integer locationId, String updatedBy) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);
    params.put("locationid", locationId);
    params.put("roleid", roleId);
    params.put("updatedBy", updatedBy);

    ResponseEntity<UserRole> responseEntity =
        this.restTemplate.postForEntity(
            userRoleServiceUri + "/userrole/{wbi}/{locationid}/{roleid}/{updatedBy}",
            null,
            UserRole.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public void removeUserFromRoleForLocation(
      String wbi, String roleId, Integer locationId, String updatedBy) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);
    params.put("roleid", roleId);
    params.put("locationid", locationId);
    params.put("updatedBy", updatedBy);

    this.restTemplate.delete(userRoleServiceUri + "/userrole/{wbi}/{locationid}/{roleid}", params);
  }

  @Override
  public List<RoleAllowed> getAllRolesAllowed(String wbi) {
    Map<String, Object> params = new HashMap<>();
    params.put("wbi", wbi);

    String uri = userRoleServiceUri + "/userrole/{wbi}/getAllRolesAllowed";
    ResponseEntity<RoleAllowed[]> responseEntity =
        this.restTemplate.getForEntity(uri, RoleAllowed[].class, params);
    return Arrays.asList(responseEntity.getBody());
  }
}
