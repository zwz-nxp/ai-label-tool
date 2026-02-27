package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ValidationChart;
import com.nxp.iemdm.service.ValidationChartService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** REST implementation of ValidationChartService that calls the operational service layer. */
@Slf4j
@Service
public class ValidationChartServiceREST implements ValidationChartService {

  private final RestTemplate restTemplate;
  private final String validationChartServiceUri;

  @Autowired
  public ValidationChartServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.validationchartservice.uri:http://localhost:8080}")
          String validationChartServiceUri) {
    this.restTemplate = restTemplate;
    this.validationChartServiceUri = validationChartServiceUri;
  }

  @Override
  public List<ValidationChart> getValidationChartDataByModelId(Long modelId) {
    log.info("REST Service: Getting validation chart data for model id: {}", modelId);

    String url = validationChartServiceUri + "/operational/landingai/charts/validation/" + modelId;

    ResponseEntity<ValidationChart[]> responseEntity =
        restTemplate.getForEntity(url, ValidationChart[].class);

    return Arrays.asList(responseEntity.getBody());
  }
}
