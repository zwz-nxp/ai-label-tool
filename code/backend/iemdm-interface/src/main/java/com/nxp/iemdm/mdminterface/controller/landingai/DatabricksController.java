package com.nxp.iemdm.mdminterface.controller.landingai;

import com.nxp.iemdm.mdminterface.dto.request.*;
import com.nxp.iemdm.mdminterface.dto.response.*;
import com.nxp.iemdm.mdminterface.service.landingai.DatabricksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Databricks integration operations. Provides endpoints for model training,
 * testing, and management.
 *
 * <p>TODO: Consider renaming to DetectionController and moving to /infc/databricks/detection when
 * backward compatibility is no longer needed. Currently kept as-is because teammates are using
 * these endpoints for mockup testing. See ClassificationController for the new task-type-specific
 * pattern.
 */
@RestController
@RequestMapping("/infc/databricks")
@CrossOrigin(origins = "*")
@Slf4j
public class DatabricksController {

  @Autowired private DatabricksService databricksService;

  /** Submit training data to Databricks POST /api/databricks/training */
  @PostMapping(value = "/training", produces = "application/json", consumes = "application/json")
  public ResponseEntity<TrainingDataResponse> submitTraining(
      @RequestBody TrainingDataRequest request) {
    log.info(
        "Received training submission request - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      TrainingDataResponse response = databricksService.submitTraining(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error submitting training: {}", e.getMessage(), e);
      TrainingDataResponse errorResponse =
          new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  // ==========================================================================
  // DEPRECATED: Old endpoint that returns parsed JSON data directly.
  // Replaced by /training/results/files which returns file paths instead.
  // Kept commented out for reference. Remove once confirmed no longer needed.
  // ==========================================================================
  // @GetMapping(value = "/training/results", produces = "application/json")
  // public ResponseEntity<TrainingResultsResponse> getTrainingResults(@RequestParam String trackId)
  // {
  //   log.info("Retrieving training results for trackId: {}", trackId);
  //   try {
  //     TrainingResultsRequest request = new TrainingResultsRequest(null, trackId);
  //     TrainingResultsResponse response = databricksService.getTrainingResults(request);
  //     return ResponseEntity.ok(response);
  //   } catch (Exception e) {
  //     log.error("Error retrieving training results: {}", e.getMessage(), e);
  //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  //   }
  // }

  /**
   * Retrieve training results as file paths. Downloads files from Databricks volume to local server
   * path and returns local file paths. GET /api/databricks/training/results/files
   */
  @GetMapping(value = "/training/results/files", produces = "application/json")
  public ResponseEntity<TrainingResultsFilePathsResponse> getTrainingResultsAsFilePaths(
      @RequestParam String trackId, @RequestParam(required = false) Long runId) {

    log.info(
        "Retrieving training results as file paths for trackId: {}, runId: {}", trackId, runId);

    try {
      TrainingResultsRequest request = new TrainingResultsRequest(null, trackId);
      request.setRunId(runId);
      TrainingResultsFilePathsResponse response =
          databricksService.getTrainingResultsAsFilePaths(request);

      if ("ERROR".equals(response.getStatus())) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving training results as file paths: {}", e.getMessage(), e);
      TrainingResultsFilePathsResponse errorResponse =
          TrainingResultsFilePathsResponse.builder()
              .trackId(trackId)
              .status("ERROR")
              .error("Failed to retrieve training results: " + e.getMessage())
              .build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** Test a trained model with new images POST /infc/databricks/model/test */
  @PostMapping(value = "/model/test", produces = "application/json", consumes = "application/json")
  public ResponseEntity<TrainingDataResponse> testModel(@RequestBody ModelTestRequest request) {
    log.info(
        "Testing model - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      TrainingDataResponse response = databricksService.testModel(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error testing model: {}", e.getMessage(), e);
      TrainingDataResponse errorResponse =
          new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** Retrieve test model results as file paths. GET /infc/databricks/model/test/results/files */
  @GetMapping(value = "/model/test/results/files", produces = "application/json")
  public ResponseEntity<TrainingResultsFilePathsResponse> getTestModelResultsAsFilePaths(
      @RequestParam String trackId, @RequestParam(required = false) Long runId) {

    log.info(
        "Retrieving test model results as file paths for trackId: {}, runId: {}", trackId, runId);

    try {
      TrainingResultsRequest request = new TrainingResultsRequest(null, trackId);
      request.setRunId(runId);
      TrainingResultsFilePathsResponse response =
          databricksService.getTestModelResultsAsFilePaths(request);

      if ("ERROR".equals(response.getStatus())) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving test model results as file paths: {}", e.getMessage(), e);
      TrainingResultsFilePathsResponse errorResponse =
          TrainingResultsFilePathsResponse.builder()
              .trackId(trackId)
              .status("ERROR")
              .error("Failed to retrieve test model results: " + e.getMessage())
              .build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * Read file content from local file system and return as JSON. GET
   * /infc/databricks/file/content?path=xxx
   */
  @GetMapping(value = "/file/content", produces = "application/json")
  public ResponseEntity<String> getFileContent(@RequestParam String path) {
    log.info("Reading file content from path: {}", path);

    try {
      String content = databricksService.readFileContent(path);
      return ResponseEntity.ok(content);
    } catch (Exception e) {
      log.error("Error reading file content from path {}: {}", path, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("{\"error\": \"Failed to read file: " + e.getMessage() + "\"}");
    }
  }

  /** Get download URL for a trained model GET /api/databricks/model/download */
  @GetMapping(value = "/model/download", produces = "application/json")
  public ResponseEntity<DownloadModelResponse> downloadModel(
      @RequestParam(name = "model_name") String modelName,
      @RequestParam Integer version,
      @RequestParam(name = "track_id") String trackId) {

    log.info("Downloading model: {} version: {} trackId: {}", modelName, version, trackId);

    try {
      DownloadModelRequest request = new DownloadModelRequest(modelName, version, trackId);
      DownloadModelResponse response = databricksService.downloadModel(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error downloading model: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /** Delete a model version DELETE /api/databricks/model */
  @DeleteMapping(value = "/model", produces = "application/json", consumes = "application/json")
  public ResponseEntity<DeleteModelResponse> deleteModel(@RequestBody DeleteModelRequest request) {
    log.info("Deleting model: {} version: {}", request.getModelFullName(), request.getVersion());

    try {
      DeleteModelResponse response = databricksService.deleteModel(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error deleting model: {}", e.getMessage(), e);
      DeleteModelResponse errorResponse =
          new DeleteModelResponse(
              "ERROR",
              "Failed to delete model: " + e.getMessage(),
              new DeleteModelResponse.ModelInfo(
                  request.getModelFullName(), request.getVersion(), request.getTrackId()));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
}
