package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import com.nxp.iemdm.shared.dto.landingai.YoloDatasetResultDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread class for asynchronous YOLO dataset generation. This thread runs independently without
 * blocking the main training flow. It loads the TrainingRecord fresh from the database to ensure
 * all data is available after the transaction has been committed.
 */
@Slf4j
public class YoloDatasetGenerationThread implements Runnable {

  private final Long trainingRecordId;
  private final TrainingService trainingService;

  /**
   * Constructor for YoloDatasetGenerationThread.
   *
   * @param trainingRecordId the training record ID to load from database
   * @param trainingService the training service instance to call generateYoloDataset
   */
  public YoloDatasetGenerationThread(Long trainingRecordId, TrainingService trainingService) {
    this.trainingRecordId = trainingRecordId;
    this.trainingService = trainingService;
  }

  @Override
  public void run() {
    try {
      log.info(
          "Starting async YOLO dataset generation for training record ID: {}", trainingRecordId);

      // Load the training record fresh from database
      TrainingRecord trainingRecord =
          trainingService.loadTrainingRecordForYoloGeneration(trainingRecordId);

      if (trainingRecord == null) {
        log.error("Training record not found with ID: {}", trainingRecordId);
        return;
      }

      log.info(
          "Loaded training record ID: {}, trackId: {}, snapshotId: {}",
          trainingRecord.getId(),
          trainingRecord.getTrackId(),
          trainingRecord.getSnapshotId());

      YoloDatasetResultDTO result = trainingService.generateYoloDataset(trainingRecord);

      if (result.isSuccess()) {
        log.info(
            "Successfully generated YOLO dataset for training record ID: {}, trackId: {}, files: {}",
            trainingRecord.getId(),
            trainingRecord.getTrackId(),
            result.getZipFilePaths() != null ? result.getZipFilePaths().size() : 0);
      } else {
        log.error(
            "Failed to generate YOLO dataset for training record ID: {}, trackId: {}, error: {}",
            trainingRecord.getId(),
            trainingRecord.getTrackId(),
            result.getErrorMessage());
      }
    } catch (Exception e) {
      log.error(
          "Exception during async YOLO dataset generation for training record ID: {}",
          trainingRecordId,
          e);
    }
  }
}
