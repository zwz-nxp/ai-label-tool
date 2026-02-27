package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.service.RoleService;
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
public class RoleServiceREST implements RoleService {
  private final RestTemplate restTemplate;
  private final String roleServiceUri;

  @Autowired
  public RoleServiceREST(
      RestTemplate restTemplate, @Value("${rest.roleservice.uri}") String roleServiceUri) {
    this.restTemplate = restTemplate;
    this.roleServiceUri = roleServiceUri;
  }

  @MethodLog
  @Override
  public List<Role> getAllRoles() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Role[]> responseEntity =
        this.restTemplate.getForEntity(roleServiceUri + "/role/all", Role[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }
}
