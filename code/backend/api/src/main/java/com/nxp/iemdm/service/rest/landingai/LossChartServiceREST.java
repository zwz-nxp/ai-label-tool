package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.LossChart;
import com.nxp.iemdm.service.LossChartService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** REST implementation of LossChartService that calls the operational service layer. */
@Slf4j
@Service
public class LossChartServiceREST implements LossChartService {

  private final RestTemplate restTemplate;
  private final String lossChartServiceUri;

  @Autowired
  public LossChartServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.losschartservice.uri:http://localhost:8080}") String lossChartServiceUri) {
    this.restTemplate = restTemplate;
    this.lossChartServiceUri = lossChartServiceUri;
  }

  @Override
  public List<LossChart> getLossChartDataByModelId(Long modelId) {
    log.info("REST Service: Getting loss chart data for model id: {}", modelId);

    String url = lossChartServiceUri + "/operational/landingai/charts/loss/" + modelId;

    ResponseEntity<LossChart[]> responseEntity = restTemplate.getForEntity(url, LossChart[].class);

    return Arrays.asList(responseEntity.getBody());
  }
}
