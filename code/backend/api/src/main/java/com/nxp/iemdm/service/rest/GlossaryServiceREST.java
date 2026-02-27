package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import com.nxp.iemdm.service.GlossaryService;
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
public class GlossaryServiceREST implements GlossaryService {

  private final RestTemplate restTemplate;
  private final String glossaryServiceURI;

  @Autowired
  public GlossaryServiceREST(
      RestTemplate restTemplate, @Value("${rest.glossaryservice.uri}") String glossaryServiceURI) {
    this.restTemplate = restTemplate;
    this.glossaryServiceURI = glossaryServiceURI;
  }

  @MethodLog
  @Override
  public List<GlossaryItem> getAllGlossaryItems() {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<GlossaryItem[]> responseEntity =
        this.restTemplate.getForEntity(
            glossaryServiceURI + "/glossary/all", GlossaryItem[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public GlossaryItem saveGlossaryItem(GlossaryItem glossaryItem) {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<GlossaryItem> responseEntity =
        this.restTemplate.postForEntity(
            glossaryServiceURI + "/glossary", glossaryItem, GlossaryItem.class, params);

    return responseEntity.getBody();
  }
}
