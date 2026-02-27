package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.location.SapCode;
import com.nxp.iemdm.service.SapCodeService;
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
public class SapCodeServiceREST implements SapCodeService {
  private final RestTemplate restTemplate;
  private final String sapcodeServiceUri;

  @Autowired
  public SapCodeServiceREST(
      RestTemplate restTemplate, @Value("${rest.sapcodeservice.uri}") String sapcodeServiceUri) {
    this.restTemplate = restTemplate;
    this.sapcodeServiceUri = sapcodeServiceUri;
  }

  @Override
  public List<SapCode> getAllSapCodes() {
    ResponseEntity<SapCode[]> responseEntity =
        this.restTemplate.getForEntity(
            sapcodeServiceUri + "/sapcode/all", SapCode[].class, new HashMap<>());

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public SapCode getSapCode(String sapCodeName) {
    Map<String, Object> params = new HashMap<>();
    params.put("sapCodeName", sapCodeName);
    ResponseEntity<SapCode> responseEntity =
        this.restTemplate.getForEntity(
            sapcodeServiceUri + "/sapcode/{sapCodeName}", SapCode.class, params);
    return responseEntity.getBody();
  }

  @Override
  public SapCode saveSapCode(SapCode sapCode) {
    ResponseEntity<SapCode> responseEntity =
        this.restTemplate.postForEntity(
            sapcodeServiceUri + "/sapcode/save", sapCode, SapCode.class, new HashMap<>());
    return responseEntity.getBody();
  }

  @Override
  public void deleteSapCode(String sapCodeName) {
    Map<String, Object> params = new HashMap<>();
    params.put("sapCodeName", sapCodeName);
    this.restTemplate.delete(sapcodeServiceUri + "/sapcode/{sapCodeName}", params);
  }
}
