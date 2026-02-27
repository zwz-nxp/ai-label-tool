package com.nxp.iemdm.mdminterface.service.landingai;

import com.databricks.sdk.WorkspaceClient;
import com.databricks.sdk.core.DatabricksConfig;
import com.databricks.sdk.service.files.DirectoryEntry;
import com.databricks.sdk.service.files.UploadRequest;
import com.databricks.sdk.service.jobs.Run;
import com.databricks.sdk.service.jobs.RunLifeCycleState;
import com.databricks.sdk.service.jobs.RunNow;
import com.databricks.sdk.service.jobs.RunNowResponse;
import com.databricks.sdk.service.jobs.RunResultState;
import com.databricks.sdk.support.Wait;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.ResultsConfig;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.TrainingConfig;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.VolumeConfig;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.WorkspaceConfig;
import com.nxp.iemdm.mdminterface.dto.request.*;
import com.nxp.iemdm.mdminterface.dto.response.*;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Real implementation of DatabricksService that connects to Databricks workspace. Uploads training
 * data zip files to Unity Catalog volumes.
 */
@Service
@ConditionalOnProperty(name = "databricks.mode", havingValue = "real")
@Slf4j
public class DatabricksServiceImpl implements DatabricksService {

  // Threshold for using direct HTTP upload (100MB) - SDK doesn't set Content-Length for streams
  private static final long LARGE_FILE_THRESHOLD = 100L * 1024 * 1024;

  @Autowired private WorkspaceConfig workspaceConfig;

  @Autowired private VolumeConfig volumeConfig;

  @Autowired private ResultsConfig resultsConfig;

  @Autowired private TrainingConfig trainingConfig;

  private WorkspaceClient client;

  @PostConstruct
  public void init() {
    log.info("[DATABRICKS] Initializing Databricks client");
    log.info("[DATABRICKS] Workspace URL: {}", workspaceConfig.getUrl());
    log.info("[DATABRICKS] Volume path: {}", volumeConfig.getVolumePath());
    log.info("[DATABRICKS] Results local download path: {}", resultsConfig.getLocalDownloadPath());

    DatabricksConfig config =
        new DatabricksConfig()
            .setHost(workspaceConfig.getUrl())
            .setToken(workspaceConfig.getToken());

    this.client = new WorkspaceClient(config);
    log.info("[DATABRICKS] Client initialized successfully");
  }

  @Override
  public TrainingDataResponse submitTraining(TrainingDataRequest request) {
    log.info(
        "[DATABRICKS] Submitting training - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      if (request.getZipFilenames() == null || request.getZipFilenames().isEmpty()) {
        log.error("[DATABRICKS] No zip files provided");
        return new TrainingDataResponse("No zip files provided", request.getTrackId());
      }

      // Upload all zip files sequentially
      int totalFiles = request.getZipFilenames().size();
      int uploadedCount = 0;

      for (String zipFilename : request.getZipFilenames()) {
        uploadedCount++;
        log.info("[DATABRICKS] Uploading file {}/{}: {}", uploadedCount, totalFiles, zipFilename);

        // Build local zip file path
        String localZipPath = request.getZipPath() + zipFilename;
        Path zipFile = Paths.get(localZipPath);

        if (!Files.exists(zipFile)) {
          log.error("[DATABRICKS] Zip file not found: {}", localZipPath);
          return new TrainingDataResponse(
              "Zip file not found: " + localZipPath, request.getTrackId());
        }

        // Get file size for logging
        long fileSize = Files.size(zipFile);
        log.info(
            "[DATABRICKS] Zip file size: {} bytes ({} MB)", fileSize, fileSize / (1024 * 1024));

        // Build destination path in Databricks volume
        String destinationPath = volumeConfig.getVolumePath() + "/" + zipFilename;
        log.info("[DATABRICKS] Uploading zip to volume: {}", destinationPath);

        // Choose upload method based on file size
        // SDK doesn't set Content-Length header for streams, causing issues with large files
        if (fileSize > LARGE_FILE_THRESHOLD) {
          log.info(
              "[DATABRICKS] Using direct HTTP upload for large file (>{} MB)",
              LARGE_FILE_THRESHOLD / (1024 * 1024));
          uploadFileWithContentLength(zipFile, destinationPath, fileSize);
        } else {
          log.info("[DATABRICKS] Using SDK upload for small file");
          try (FileInputStream fis = new FileInputStream(zipFile.toFile())) {
            UploadRequest uploadRequest =
                new UploadRequest().setFilePath(destinationPath).setContents(fis);
            client.files().upload(uploadRequest);
          }
        }

        log.info("[DATABRICKS] Zip file uploaded successfully to: {}", destinationPath);
      }

      log.info(
          "[DATABRICKS] All {} files uploaded successfully for trackId: {}",
          totalFiles,
          request.getTrackId());

      // Submit Databricks job using runNow on existing job
      log.info(
          "[DATABRICKS] Triggering job {} for trackId: {}",
          trainingConfig.getJobId(),
          request.getTrackId());

      Map<String, String> jobParams = new HashMap<>();
      jobParams.put("TR_ID", request.getTrackId());

      RunNow runNow =
          new RunNow()
              .setJobId(Long.parseLong(trainingConfig.getJobId()))
              .setJobParameters(jobParams);

      Wait<Run, RunNowResponse> runWait = client.jobs().runNow(runNow);
      Long runId = runWait.getResponse().getRunId();

      log.info("[DATABRICKS] Job triggered successfully. runId: {}", runId);

      TrainingDataResponse response = new TrainingDataResponse("", request.getTrackId());
      response.setRunId(runId);
      return response;

    } catch (IOException e) {
      log.error("[DATABRICKS] IO error uploading zip file", e);
      return new TrainingDataResponse("IO error: " + e.getMessage(), request.getTrackId());
    } catch (Exception e) {
      log.error("[DATABRICKS] Error uploading zip file to Databricks", e);
      return new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
    }
  }

  /**
   * Upload file using direct HTTP PUT with explicit Content-Length header. This is needed for large
   * files because the SDK doesn't set Content-Length for streams. The Databricks Files API has a
   * 5GB limit per file.
   */
  private void uploadFileWithContentLength(Path localFile, String volumePath, long fileSize)
      throws IOException {
    // Build the API URL: https://<workspace>/api/2.0/fs/files/<volume_path>
    String apiUrl = workspaceConfig.getUrl() + "/api/2.0/fs/files" + volumePath;
    log.info("[DATABRICKS] Upload URL: {}", apiUrl);

    URL url = new URL(apiUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    try {
      conn.setRequestMethod("PUT");
      conn.setDoOutput(true);
      conn.setRequestProperty("Authorization", "Bearer " + workspaceConfig.getToken());
      conn.setRequestProperty("Content-Type", "application/octet-stream");
      conn.setFixedLengthStreamingMode(fileSize); // This sets Content-Length header
      conn.setConnectTimeout(30000); // 30 seconds connect timeout
      conn.setReadTimeout(0); // No read timeout for large uploads

      // Stream the file content
      try (FileInputStream fis = new FileInputStream(localFile.toFile());
          OutputStream os = conn.getOutputStream()) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalWritten = 0;
        long lastLogTime = System.currentTimeMillis();

        while ((bytesRead = fis.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
          totalWritten += bytesRead;

          // Log progress every 30 seconds
          long now = System.currentTimeMillis();
          if (now - lastLogTime > 30000) {
            double percent = (totalWritten * 100.0) / fileSize;
            log.info(
                "[DATABRICKS] Upload progress: {}/{} bytes ({}%)",
                totalWritten, fileSize, String.format("%.1f", percent));
            lastLogTime = now;
          }
        }
      }

      int responseCode = conn.getResponseCode();
      if (responseCode != 200 && responseCode != 204) {
        String errorMsg = new String(conn.getErrorStream().readAllBytes());
        throw new IOException("Upload failed with HTTP " + responseCode + ": " + errorMsg);
      }

      log.info("[DATABRICKS] Upload completed with HTTP {}", responseCode);

    } finally {
      conn.disconnect();
    }
  }

  // DEPRECATED: Old method replaced by getTrainingResultsAsFilePaths().
  // @Override
  // public TrainingResultsResponse getTrainingResults(TrainingResultsRequest request) {
  //   log.info("[DATABRICKS] Retrieving training results for trackId: {}", request.getTrackId());
  //   throw new UnsupportedOperationException(
  //       "Real Databricks results retrieval not yet implemented. Use getTrainingResultsAsFilePaths
  // for file-based results.");
  // }

  @Override
  public TrainingResultsFilePathsResponse getTrainingResultsAsFilePaths(
      TrainingResultsRequest request) {
    log.info(
        "[DATABRICKS] Retrieving training results as file paths for trackId: {}, runId: {}",
        request.getTrackId(),
        request.getRunId());

    String trackId = request.getTrackId();
    Long runId = request.getRunId();

    try {
      // runId is required in real mode to check job status before downloading
      if (runId == null) {
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .status("ERROR")
            .error(
                "runId is required. Submit training first to get a runId, then pass it here to check job status.")
            .build();
      }

      // Step 1: Check job status
      log.info("[DATABRICKS] Checking job status for runId: {}", runId);
      Run run = client.jobs().getRun(runId);
      RunLifeCycleState lifeCycleState = run.getState().getLifeCycleState();
      RunResultState resultState = run.getState().getResultState();

      log.info(
          "[DATABRICKS] Job runId: {} - lifeCycleState: {}, resultState: {}",
          runId,
          lifeCycleState,
          resultState);

      // If job hit internal error, return failed status immediately
      if (lifeCycleState == RunLifeCycleState.INTERNAL_ERROR) {
        String stateMsg =
            run.getState().getStateMessage() != null ? run.getState().getStateMessage() : "";
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status("FAILED")
            .error("Job internal error: " + stateMsg)
            .build();
      }

      // If job is not yet terminated, return in-progress status
      if (lifeCycleState != RunLifeCycleState.TERMINATED) {
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status(lifeCycleState.toString())
            .error("Job is still " + lifeCycleState + ". Please poll again later.")
            .build();
      }

      // If job terminated but failed
      if (resultState != RunResultState.SUCCESS) {
        String stateMessage =
            run.getState().getStateMessage() != null ? run.getState().getStateMessage() : "";
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status("FAILED")
            .error("Job failed with result: " + resultState + ". " + stateMessage)
            .build();
      }

      log.info("[DATABRICKS] Job completed successfully. Proceeding to download results.");

      // Step 2: Download results from volume (only reached if job is complete)
      String sourceDir = volumeConfig.getVolumePath() + "/" + trackId;
      log.info("[DATABRICKS] Source directory in volume: {}", sourceDir);

      // Build local destination directory
      Path localDir = Paths.get(resultsConfig.getLocalDownloadPath(), trackId);
      Files.createDirectories(localDir);
      log.info("[DATABRICKS] Local destination directory: {}", localDir);

      // Expected files to download
      Map<String, String> expectedFiles = new HashMap<>();
      expectedFiles.put("results.csv", "results");
      expectedFiles.put("args.yaml", "args");
      expectedFiles.put("predictions_metrics.json", "predictionMetrics");
      expectedFiles.put("train_predictions.json", "trainPrediction");
      expectedFiles.put("val_predictions.json", "valPrediction");
      expectedFiles.put("test_predictions.json", "testPrediction");

      // Map to store downloaded file paths
      Map<String, String> downloadedPaths = new HashMap<>();

      // List files in the source directory
      log.info("[DATABRICKS] Listing files in volume directory: {}", sourceDir);
      Iterable<DirectoryEntry> files = client.files().listDirectoryContents(sourceDir);

      for (DirectoryEntry file : files) {
        String fileName = file.getName();
        log.info("[DATABRICKS] Found file: {} (isDirectory: {})", fileName, file.getIsDirectory());

        // Skip directories
        if (Boolean.TRUE.equals(file.getIsDirectory())) {
          continue;
        }

        // Check if this is one of the expected files
        String fieldName = expectedFiles.get(fileName);
        if (fieldName != null) {
          // Download the file
          String sourcePath = sourceDir + "/" + fileName;
          Path localPath = localDir.resolve(fileName);

          log.info("[DATABRICKS] Downloading {} to {}", sourcePath, localPath);
          downloadFileFromVolume(sourcePath, localPath);

          downloadedPaths.put(fieldName, localPath.toString());
          log.info("[DATABRICKS] Downloaded {} successfully", fileName);
        }
      }

      // Build response
      TrainingResultsFilePathsResponse response =
          TrainingResultsFilePathsResponse.builder()
              .trackId(trackId)
              .runId(runId)
              .results(downloadedPaths.get("results"))
              .args(downloadedPaths.get("args"))
              .predictionMetrics(downloadedPaths.get("predictionMetrics"))
              .trainPrediction(downloadedPaths.get("trainPrediction"))
              .valPrediction(downloadedPaths.get("valPrediction"))
              .testPrediction(downloadedPaths.get("testPrediction"))
              .status("SUCCESS")
              .build();

      log.info(
          "[DATABRICKS] Training results downloaded successfully. Files: {}",
          downloadedPaths.size());
      return response;

    } catch (Exception e) {
      log.error("[DATABRICKS] Error downloading training results for trackId: {}", trackId, e);
      return TrainingResultsFilePathsResponse.builder()
          .trackId(trackId)
          .status("ERROR")
          .error("Failed to download training results: " + e.getMessage())
          .build();
    }
  }

  /** Download a file from Databricks volume to local path using SDK. */
  private void downloadFileFromVolume(String volumePath, Path localPath) throws IOException {
    try {
      var downloadResponse = client.files().download(volumePath);
      byte[] content = downloadResponse.getContents().readAllBytes();
      Files.write(localPath, content);
    } catch (Exception e) {
      throw new IOException(
          "Failed to download file from " + volumePath + ": " + e.getMessage(), e);
    }
  }

  @Override
  public TrainingDataResponse testModel(ModelTestRequest request) {
    log.info(
        "[DATABRICKS] Testing model - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      if (request.getZipFilenames() == null || request.getZipFilenames().isEmpty()) {
        log.error("[DATABRICKS] No zip files provided for model test");
        return new TrainingDataResponse("No zip files provided", request.getTrackId());
      }

      // Upload all zip files sequentially (same as submitTraining)
      int totalFiles = request.getZipFilenames().size();
      int uploadedCount = 0;

      for (String zipFilename : request.getZipFilenames()) {
        uploadedCount++;
        log.info(
            "[DATABRICKS] Uploading test file {}/{}: {}", uploadedCount, totalFiles, zipFilename);

        String localZipPath = request.getZipPath() + zipFilename;
        Path zipFile = Paths.get(localZipPath);

        if (!Files.exists(zipFile)) {
          log.error("[DATABRICKS] Zip file not found: {}", localZipPath);
          return new TrainingDataResponse(
              "Zip file not found: " + localZipPath, request.getTrackId());
        }

        long fileSize = Files.size(zipFile);
        log.info(
            "[DATABRICKS] Zip file size: {} bytes ({} MB)", fileSize, fileSize / (1024 * 1024));

        String destinationPath = volumeConfig.getVolumePath() + "/" + zipFilename;
        log.info("[DATABRICKS] Uploading zip to volume: {}", destinationPath);

        if (fileSize > LARGE_FILE_THRESHOLD) {
          uploadFileWithContentLength(zipFile, destinationPath, fileSize);
        } else {
          try (FileInputStream fis = new FileInputStream(zipFile.toFile())) {
            UploadRequest uploadRequest =
                new UploadRequest().setFilePath(destinationPath).setContents(fis);
            client.files().upload(uploadRequest);
          }
        }

        log.info("[DATABRICKS] Test zip uploaded successfully to: {}", destinationPath);
      }

      log.info(
          "[DATABRICKS] All {} test files uploaded for trackId: {}",
          totalFiles,
          request.getTrackId());

      // Trigger Databricks job using the shared job ID
      log.info(
          "[DATABRICKS] Triggering test job {} for trackId: {}",
          trainingConfig.getJobId(),
          request.getTrackId());

      Map<String, String> jobParams = new HashMap<>();
      jobParams.put("TR_ID", request.getTrackId());

      RunNow runNow =
          new RunNow()
              .setJobId(Long.parseLong(trainingConfig.getJobId()))
              .setJobParameters(jobParams);

      Wait<Run, RunNowResponse> runWait = client.jobs().runNow(runNow);
      Long runId = runWait.getResponse().getRunId();

      log.info("[DATABRICKS] Test job triggered successfully. runId: {}", runId);

      TrainingDataResponse response = new TrainingDataResponse("", request.getTrackId());
      response.setRunId(runId);
      return response;

    } catch (IOException e) {
      log.error("[DATABRICKS] IO error uploading test zip file", e);
      return new TrainingDataResponse("IO error: " + e.getMessage(), request.getTrackId());
    } catch (Exception e) {
      log.error("[DATABRICKS] Error during model test submission", e);
      return new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
    }
  }

  @Override
  public TrainingResultsFilePathsResponse getTestModelResultsAsFilePaths(
      TrainingResultsRequest request) {
    log.info(
        "[DATABRICKS] Retrieving test model results for trackId: {}, runId: {}",
        request.getTrackId(),
        request.getRunId());

    String trackId = request.getTrackId();
    Long runId = request.getRunId();

    try {
      // runId is required in real mode to check job status before downloading
      if (runId == null) {
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .status("ERROR")
            .error(
                "runId is required. Submit test model first to get a runId, then pass it here to check job status.")
            .build();
      }

      // Step 1: Check job status
      log.info("[DATABRICKS] Checking test job status for runId: {}", runId);
      Run run = client.jobs().getRun(runId);
      RunLifeCycleState lifeCycleState = run.getState().getLifeCycleState();
      RunResultState resultState = run.getState().getResultState();

      log.info(
          "[DATABRICKS] Test job runId: {} - lifeCycleState: {}, resultState: {}",
          runId,
          lifeCycleState,
          resultState);

      if (lifeCycleState == RunLifeCycleState.INTERNAL_ERROR) {
        String stateMsg =
            run.getState().getStateMessage() != null ? run.getState().getStateMessage() : "";
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status("FAILED")
            .error("Test job internal error: " + stateMsg)
            .build();
      }

      if (lifeCycleState != RunLifeCycleState.TERMINATED) {
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status(lifeCycleState.toString())
            .error("Test job is still " + lifeCycleState + ". Please poll again later.")
            .build();
      }

      if (resultState != RunResultState.SUCCESS) {
        String stateMessage =
            run.getState().getStateMessage() != null ? run.getState().getStateMessage() : "";
        return TrainingResultsFilePathsResponse.builder()
            .trackId(trackId)
            .runId(runId)
            .status("FAILED")
            .error("Test job failed with result: " + resultState + ". " + stateMessage)
            .build();
      }

      log.info("[DATABRICKS] Test job completed successfully. Proceeding to download results.");

      // Step 2: Download results from volume
      String sourceDir = volumeConfig.getVolumePath() + "/" + trackId;
      log.info("[DATABRICKS] Test results source directory: {}", sourceDir);

      Path localDir = Paths.get(resultsConfig.getLocalDownloadPath(), "test_" + trackId);
      Files.createDirectories(localDir);
      log.info("[DATABRICKS] Local destination directory: {}", localDir);

      Map<String, String> expectedFiles = new HashMap<>();
      expectedFiles.put("results.csv", "results");
      expectedFiles.put("args.yaml", "args");
      expectedFiles.put("predictions_metrics.json", "predictionMetrics");
      expectedFiles.put("train_predictions.json", "trainPrediction");
      expectedFiles.put("val_predictions.json", "valPrediction");
      expectedFiles.put("test_predictions.json", "testPrediction");

      Map<String, String> downloadedPaths = new HashMap<>();

      log.info("[DATABRICKS] Listing files in volume directory: {}", sourceDir);
      Iterable<DirectoryEntry> files = client.files().listDirectoryContents(sourceDir);

      for (DirectoryEntry file : files) {
        String fileName = file.getName();
        log.info("[DATABRICKS] Found file: {} (isDirectory: {})", fileName, file.getIsDirectory());

        if (Boolean.TRUE.equals(file.getIsDirectory())) {
          continue;
        }

        String fieldName = expectedFiles.get(fileName);
        if (fieldName != null) {
          String sourcePath = sourceDir + "/" + fileName;
          Path localPath = localDir.resolve(fileName);

          log.info("[DATABRICKS] Downloading {} to {}", sourcePath, localPath);
          downloadFileFromVolume(sourcePath, localPath);

          downloadedPaths.put(fieldName, localPath.toString());
          log.info("[DATABRICKS] Downloaded {} successfully", fileName);
        }
      }

      TrainingResultsFilePathsResponse response =
          TrainingResultsFilePathsResponse.builder()
              .trackId(trackId)
              .runId(runId)
              .results(downloadedPaths.get("results"))
              .args(downloadedPaths.get("args"))
              .predictionMetrics(downloadedPaths.get("predictionMetrics"))
              .trainPrediction(downloadedPaths.get("trainPrediction"))
              .valPrediction(downloadedPaths.get("valPrediction"))
              .testPrediction(downloadedPaths.get("testPrediction"))
              .status("SUCCESS")
              .build();

      log.info(
          "[DATABRICKS] Test model results downloaded successfully. Files: {}",
          downloadedPaths.size());
      return response;

    } catch (Exception e) {
      log.error("[DATABRICKS] Error downloading test model results for trackId: {}", trackId, e);
      return TrainingResultsFilePathsResponse.builder()
          .trackId(trackId)
          .status("ERROR")
          .error("Failed to download test model results: " + e.getMessage())
          .build();
    }
  }

  @Override
  public DownloadModelResponse downloadModel(DownloadModelRequest request) {
    log.info(
        "[DATABRICKS] Downloading model: {} version: {}",
        request.getModelName(),
        request.getVersion());
    // TODO: Implement when model registry access is available
    throw new UnsupportedOperationException(
        "Real Databricks model download not yet implemented. Waiting for model registry details.");
  }

  @Override
  public DeleteModelResponse deleteModel(DeleteModelRequest request) {
    log.info(
        "[DATABRICKS] Deleting model: {} version: {}",
        request.getModelFullName(),
        request.getVersion());
    // TODO: Implement when model registry access is available
    throw new UnsupportedOperationException(
        "Real Databricks model deletion not yet implemented. Waiting for model registry details.");
  }

  @Override
  public String readFileContent(String filePath) {
    log.info("[DATABRICKS] Reading file content from: {}", filePath);

    try {
      // 將相對路徑轉換為絕對路徑
      Path path = Paths.get(filePath);

      // 如果是相對路徑，從當前工作目錄解析
      if (!path.isAbsolute()) {
        path = Paths.get(System.getProperty("user.dir"), filePath);
      }

      log.debug("[DATABRICKS] Resolved absolute path: {}", path.toAbsolutePath());

      // 檢查檔案是否存在
      if (!Files.exists(path)) {
        log.error("[DATABRICKS] File not found: {}", path.toAbsolutePath());
        throw new IOException("File not found: " + filePath);
      }

      // 讀取檔案內容
      String content = Files.readString(path);
      log.info("[DATABRICKS] Successfully read file, size: {} bytes", content.length());

      return content;

    } catch (IOException e) {
      log.error("[DATABRICKS] Error reading file {}: {}", filePath, e.getMessage(), e);
      throw new RuntimeException("Failed to read file: " + filePath, e);
    }
  }
}
