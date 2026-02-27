package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ConfidentialReport;
import com.nxp.iemdm.service.ConfidentialReportService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** REST implementation of ConfidentialReportService that calls the operational service layer. */
@Slf4j
@Service
public class ConfidentialReportServiceREST implements ConfidentialReportService {

  private final RestTemplate restTemplate;
  private final String confidentialReportServiceUri;

  @Autowired
  public ConfidentialReportServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.confidentialreportservice.uri:http://localhost:8080}")
          String confidentialReportServiceUri) {
    this.restTemplate = restTemplate;
    this.confidentialReportServiceUri = confidentialReportServiceUri;
  }

  @Override
  public Optional<ConfidentialReport> getConfidentialReportByModelId(Long modelId) {
    log.info("REST Service: Getting confidential report for model id: {}", modelId);

    String url =
        confidentialReportServiceUri
            + "/operational/landingai/confidential-reports/model/"
            + modelId;

    try {
      ResponseEntity<ConfidentialReport> responseEntity =
          restTemplate.getForEntity(url, ConfidentialReport.class);

      return Optional.ofNullable(responseEntity.getBody());
    } catch (Exception e) {
      log.warn("Confidential report not found for model id: {}", modelId);
      return Optional.empty();
    }
  }

  @Override
  public Optional<ConfidentialReport> getConfidentialReportById(Long id) {
    log.info("REST Service: Getting confidential report by id: {}", id);

    String url = confidentialReportServiceUri + "/operational/landingai/confidential-reports/" + id;

    try {
      ResponseEntity<ConfidentialReport> responseEntity =
          restTemplate.getForEntity(url, ConfidentialReport.class);

      return Optional.ofNullable(responseEntity.getBody());
    } catch (Exception e) {
      log.warn("Confidential report not found with id: {}", id);
      return Optional.empty();
    }
  }
}
