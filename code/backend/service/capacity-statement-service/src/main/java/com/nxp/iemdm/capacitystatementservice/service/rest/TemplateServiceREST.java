package com.nxp.iemdm.capacitystatementservice.service.rest;

import com.nxp.iemdm.capacitystatementservice.service.TemplateService;
import com.nxp.iemdm.model.configuration.Template;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TemplateServiceREST implements TemplateService {
  private final RestTemplate restTemplate;
  private final String templateServiceURI;

  @Autowired
  public TemplateServiceREST(
      RestTemplate restTemplate, @Value("${rest.templateservice.uri}") String templateServiceURI) {
    this.restTemplate = restTemplate;
    this.templateServiceURI = templateServiceURI;
  }

  @Override
  public Template getTemplate(String name) {
    Map<String, Object> params = new HashMap<>();
    params.put("name", name);

    ResponseEntity<Template> responseEntity =
        this.restTemplate.getForEntity(
            templateServiceURI + "/template/{name}", Template.class, params);

    return responseEntity.getBody();
  }
}
