package com.nxp.iemdm.operational.service.databricks;

import com.nxp.iemdm.shared.dto.databricks.TestModelRequest;
import com.nxp.iemdm.shared.dto.databricks.TestModelResponse;
import com.nxp.iemdm.shared.intf.operational.databricks.DatabricksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Operational service implementation for Databricks model testing.
 * 
 * <p>This service handles communication with the external Databricks API for model inference and
 * prediction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabricksOperationalService implements DatabricksService {

  private final RestTemplate restTemplate;

  @Value("${databricks.api.base-url:http://localhost:9000}")
  private String databricksBaseUrl;

  @Value("${databricks.api.model-test-endpoint:/model/test}")
  private String modelTestEndpoint;

  @Override
  public TestModelResponse testModel(TestModelRequest request) {
    String url = databricksBaseUrl + modelTestEndpoint;

    log.info(
        "Calling Databricks API to test model: {} version {} with {} images at URL: {}",
        request.getModelFullName(),
        request.getVersion(),
        request.getImageUrls().size(),
        url);

    try {
      // Build HTTP headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // Build HTTP entity with request body
      HttpEntity<TestModelRequest> entity = new HttpEntity<>(request, headers);

      // Call external Databricks API
      ResponseEntity<TestModelResponse> response =
          restTemplate.exchange(url, HttpMethod.POST, entity, TestModelResponse.class);

      TestModelResponse responseBody = response.getBody();

      if (responseBody != null) {
        log.info(
            "Successfully received prediction results from Databricks. Image count: {}",
            responseBody.getImageList() != null ? responseBody.getImageList().size() : 0);
      } else {
        log.warn("Received null response body from Databricks API");
      }

      return responseBody;

    } catch (HttpClientErrorException e) {
      log.error(
          "Client error calling Databricks API: {} - {}",
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      throw new RuntimeException(
          "Failed to test model: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);

    } catch (HttpServerErrorException e) {
      log.error(
          "Server error calling Databricks API: {} - {}",
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      throw new RuntimeException(
          "Databricks service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
          e);

    } catch (Exception e) {
      log.error("Unexpected error calling Databricks API", e);
      throw new RuntimeException("Failed to test model: " + e.getMessage(), e);
    }
  }
}
