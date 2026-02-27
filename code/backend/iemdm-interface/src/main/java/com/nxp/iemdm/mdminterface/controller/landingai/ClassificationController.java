package com.nxp.iemdm.mdminterface.controller.landingai;

import com.nxp.iemdm.mdminterface.dto.request.ClassificationTrainingRequest;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationResultsResponse;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationTrainingResponse;
import com.nxp.iemdm.mdminterface.service.landingai.ClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Classification operations. Separate from DatabricksController (detection) for
 * task-type isolation.
 */
@RestController
@RequestMapping("/infc/databricks/classification")
@CrossOrigin(origins = "*")
@Slf4j
public class ClassificationController {

  @Autowired private ClassificationService classificationService;

  /** Submit classification training data. POST /infc/databricks/classification/training */
  @PostMapping(value = "/training", produces = "application/json", consumes = "application/json")
  public ResponseEntity<ClassificationTrainingResponse> submitTraining(
      @RequestBody ClassificationTrainingRequest request) {
    log.info(
        "Received classification training request - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      ClassificationTrainingResponse response = classificationService.submitTraining(request);
      if (response.getError() != null && !response.getError().isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error submitting classification training: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ClassificationTrainingResponse("Error: " + e.getMessage(), request.getTrackId()));
    }
  }

  /**
   * Retrieve classification training results. GET
   * /infc/databricks/classification/training/results?trackId=xxx
   */
  @GetMapping(value = "/training/results", produces = "application/json")
  public ResponseEntity<ClassificationResultsResponse> getTrainingResults(
      @RequestParam String trackId) {
    log.info("Retrieving classification results for trackId: {}", trackId);

    try {
      ClassificationResultsResponse response = classificationService.getTrainingResults(trackId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving classification results: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Test a trained classification model with new images. POST
   * /infc/databricks/classification/model/test
   */
  @PostMapping(value = "/model/test", produces = "application/json", consumes = "application/json")
  public ResponseEntity<ClassificationResultsResponse> testModel(
      @RequestBody ClassificationTrainingRequest request) {
    log.info(
        "Testing classification model - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      ClassificationResultsResponse response = classificationService.testModel(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error testing classification model: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
