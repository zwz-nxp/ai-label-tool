package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.shared.dto.landingai.TrainingResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client for calling Databricks API to retrieve training results. Handles API communication,
 * error handling, and response parsing.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DatabricksApiClient {

  private final RestTemplate restTemplate;

  @Value("${databricks.api.base-url:http://localhost:8083}")
  private String databricksBaseUrl;

  /**
   * Call Databricks API to get training results.
   *
   * @param modelFullName Model full name
   * @param trackId Track ID
   * @return TrainingResultResponse or null if failed
   */
  public TrainingResultResponse getTrainingResults(String modelFullName, String trackId) {
    try {
      // Build URL with query parameters
      String url =
          UriComponentsBuilder.fromHttpUrl(databricksBaseUrl)
              .path("/infc/databricks/training/results")
              .queryParam("modelFullName", modelFullName)
              .queryParam("trackId", trackId)
              .toUriString();

      log.debug("Calling Databricks API: {}", url);

      // Call API using RestTemplate
      ResponseEntity<TrainingResultResponse> response =
          restTemplate.getForEntity(url, TrainingResultResponse.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        log.debug("Successfully retrieved training results for trackId: {}", trackId);
        return response.getBody();
      } else {
        log.error(
            "Databricks API returned non-OK status: {} for trackId: {}",
            response.getStatusCode(),
            trackId);
        return null;
      }

    } catch (HttpClientErrorException e) {
      log.error(
          "HTTP client error calling Databricks API for trackId {}: {} - {}",
          trackId,
          e.getStatusCode(),
          e.getMessage());
      return null;

    } catch (HttpServerErrorException e) {
      log.error(
          "HTTP server error calling Databricks API for trackId {}: {} - {}",
          trackId,
          e.getStatusCode(),
          e.getMessage());
      return null;

    } catch (ResourceAccessException e) {
      log.error(
          "Timeout or connection error calling Databricks API for trackId {}: {}",
          trackId,
          e.getMessage());
      return null;

    } catch (Exception e) {
      log.error(
          "Unexpected error calling Databricks API for trackId {}: {}", trackId, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Validate that required fields are present in the API response.
   *
   * @param response API response to validate
   * @return true if all required fields are present, false otherwise
   */
  public boolean validateResponse(TrainingResultResponse response) {
    if (response == null) {
      log.error("API response is null");
      return false;
    }

    boolean isValid = true;

    // 驗證 training metrics
    if (response.getTrainingF1Rate() == null) {
      log.warn("API response missing training_f1_rate");
      isValid = false;
    }
    if (response.getTrainingPrecisionRate() == null) {
      log.warn("API response missing training_precision_rate");
      isValid = false;
    }
    if (response.getTrainingRecallRate() == null) {
      log.warn("API response missing training_recall_rate");
      isValid = false;
    }
    if (response.getTrainingCorrectRate() == null) {
      log.warn("API response missing training_correct_rate");
      isValid = false;
    }

    // 驗證 dev metrics
    if (response.getDevF1Rate() == null) {
      log.warn("API response missing dev_f1_rate");
      isValid = false;
    }
    if (response.getDevPrecisionRate() == null) {
      log.warn("API response missing dev_precision_rate");
      isValid = false;
    }
    if (response.getDevRecallRate() == null) {
      log.warn("API response missing dev_recall_rate");
      isValid = false;
    }
    if (response.getDevCorrectRate() == null) {
      log.warn("API response missing dev_correct_rate");
      isValid = false;
    }

    // 驗證 test metrics
    if (response.getTestF1Rate() == null) {
      log.warn("API response missing test_f1_rate");
      isValid = false;
    }
    if (response.getTestPrecisionRate() == null) {
      log.warn("API response missing test_precision_rate");
      isValid = false;
    }
    if (response.getTestRecallRate() == null) {
      log.warn("API response missing test_recall_rate");
      isValid = false;
    }
    if (response.getTestCorrectRate() == null) {
      log.warn("API response missing test_correct_rate");
      isValid = false;
    }

    // 驗證其他必要欄位
    if (response.getModelVersion() == null) {
      log.warn("API response missing model_version");
      isValid = false;
    }
    if (response.getConfidenceThreshold() == null) {
      log.warn("API response missing confidence_threshold");
      isValid = false;
    }

    // prediction_images 可以是空陣列,不強制要求
    if (response.getPredictionImages() == null) {
      log.warn("API response missing prediction_images (will use empty list)");
      // 不設為 invalid,因為 training 可能沒有 prediction images
    }

    return isValid;
  }
}
