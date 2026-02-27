package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.configuration.NxpProductionYear;
import com.nxp.iemdm.service.NxpProductionYearService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NxpProductionYearServiceREST implements NxpProductionYearService {

  private final RestTemplate restTemplate;
  private final String nxpProductionYearServiceUri;

  public NxpProductionYearServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.nxpproductionyearservice.uri}") String nxpProductionYearServiceUri) {
    this.restTemplate = restTemplate;
    this.nxpProductionYearServiceUri = nxpProductionYearServiceUri;
  }

  @MethodLog
  @Override
  public NxpProductionYear getNxpProductionYearForYear(Integer year) {
    Map<String, Object> params = new HashMap<>();

    params.put("year", year);

    ResponseEntity<NxpProductionYear> responseEntity =
        restTemplate.getForEntity(
            nxpProductionYearServiceUri + "/productionyear/{year}",
            NxpProductionYear.class,
            params);

    return responseEntity.getBody();
  }

  @MethodLog
  @Override
  public NxpProductionYear saveNxpProductionyear(NxpProductionYear nxpProductionYear) {
    ResponseEntity<NxpProductionYear> responseEntity =
        restTemplate.postForEntity(
            nxpProductionYearServiceUri + "/productionyear",
            nxpProductionYear,
            NxpProductionYear.class,
            Map.of());

    return responseEntity.getBody();
  }

  @Override
  @MethodLog
  public List<NxpProductionYear> getAllNxpProductionYears() {

    ResponseEntity<NxpProductionYear[]> responseEntity =
        restTemplate.getForEntity(
            nxpProductionYearServiceUri + "/productionyear/all",
            NxpProductionYear[].class,
            Map.of());

    List<NxpProductionYear> productionYears = Arrays.asList(responseEntity.getBody());
    productionYears.sort((b, a) -> a.getStartDate().compareTo(b.getStartDate()));
    return productionYears;
  }
}
