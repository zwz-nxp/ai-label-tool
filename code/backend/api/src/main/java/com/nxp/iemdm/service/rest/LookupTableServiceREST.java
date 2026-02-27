package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.configuration.LocalLookupData;
import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.consumption.LookupTable;
import com.nxp.iemdm.service.LookupTableService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@PropertySource("classpath:git.properties")
@Service
public class LookupTableServiceREST implements LookupTableService {

  private final RestTemplate restTemplate;
  private final String syncServiceUri;

  @Autowired
  public LookupTableServiceREST(
      RestTemplate restTemplate, @Value("${rest.syncservice.uri}") String syncServiceUri) {
    this.restTemplate = restTemplate;
    this.syncServiceUri = syncServiceUri;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Map<String, List<LookupTable>> getLookupTables() {
    ResponseEntity<Map> responseEntity =
        this.restTemplate.getForEntity(this.syncServiceUri + "/lookuptables/all", Map.class);
    return responseEntity.getBody();
  }

  @Override
  public GlobalLookupData getGlobalLookupData() {
    ResponseEntity<GlobalLookupData> responseEntity =
        this.restTemplate.getForEntity(
            this.syncServiceUri + "/lookupdata/global", GlobalLookupData.class);
    return responseEntity.getBody();
  }

  @Override
  public LocalLookupData getLocalLookupData(Integer locationId) {
    Map<String, Object> params = new HashMap<>();
    params.put("locationId", locationId);

    ResponseEntity<LocalLookupData> responseEntity =
        this.restTemplate.getForEntity(
            this.syncServiceUri + "/lookupdata/local/{locationId}", LocalLookupData.class, params);
    return responseEntity.getBody();
  }
}
