package com.nxp.iemdm.operational.service.rest.landingai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.model.landingai.DatabricksRequest;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageFile;
import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.model.landingai.Snapshot;
import com.nxp.iemdm.model.landingai.SnapshotImage;
import com.nxp.iemdm.model.landingai.SnapshotImageLabel;
import com.nxp.iemdm.model.landingai.SnapshotProjectClass;
import com.nxp.iemdm.model.landingai.TrainingRecord;
import com.nxp.iemdm.shared.dto.landingai.AugmentationConfigDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelConfigDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRecordDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRequest;
import com.nxp.iemdm.shared.dto.landingai.TrainingStatusDTO;
import com.nxp.iemdm.shared.dto.landingai.TransformConfigDTO;
import com.nxp.iemdm.shared.dto.landingai.YoloDatasetResultDTO;
import com.nxp.iemdm.shared.exception.landingai.InsufficientDataException;
import com.nxp.iemdm.shared.exception.landingai.TrainingException;
import com.nxp.iemdm.shared.intf.operational.ConfigurationValueService;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageFileRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.TrainingRecordRepository;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/landingai/training")
@Service
@Slf4j
@Validated
public class TrainingService {

  private final TrainingRecordRepository trainingRecordRepository;
  private final ProjectRepository projectRepository;
  private final ImageRepository imageRepository;
  private final SnapshotRepository snapshotRepository;
  private final SnapshotImageRepository snapshotImageRepository;
  private final SnapshotImageLabelRepository snapshotImageLabelRepository;
  private final SnapshotProjectClassRepository snapshotProjectClassRepository;
  private final ObjectMapper objectMapper;
  private final ImageLabelRepository imageLabelRepository;
  private final ImageFileRepository imageFileRepository;
  private final EntityManager entityManager;
  private final RestTemplate restTemplate;
  private final ConfigurationValueService configurationValueService;

  @Value("${rest.iemdm-interface.uri:http://localhost:8083}")
  private String iemdmInterfaceUri;

  /** Thread pool for async YOLO dataset generation */
  private final ExecutorService yoloDatasetExecutor = Executors.newFixedThreadPool(3);

  @Autowired
  public TrainingService(
      TrainingRecordRepository trainingRecordRepository,
      ProjectRepository projectRepository,
      ImageRepository imageRepository,
      SnapshotRepository snapshotRepository,
      SnapshotImageRepository snapshotImageRepository,
      SnapshotImageLabelRepository snapshotImageLabelRepository,
      SnapshotProjectClassRepository snapshotProjectClassRepository,
      ObjectMapper objectMapper,
      ImageLabelRepository imageLabelRepository,
      ImageFileRepository imageFileRepository,
      EntityManager entityManager,
      RestTemplate restTemplate,
      ConfigurationValueService configurationValueService) {
    this.trainingRecordRepository = trainingRecordRepository;
    this.projectRepository = projectRepository;
    this.imageRepository = imageRepository;
    this.snapshotRepository = snapshotRepository;
    this.snapshotImageRepository = snapshotImageRepository;
    this.snapshotImageLabelRepository = snapshotImageLabelRepository;
    this.snapshotProjectClassRepository = snapshotProjectClassRepository;
    this.objectMapper = objectMapper;
    this.imageLabelRepository = imageLabelRepository;
    this.imageFileRepository = imageFileRepository;
    this.entityManager = entityManager;
    this.restTemplate = restTemplate;
    this.configurationValueService = configurationValueService;
  }

  /** Shutdown the executor service when the bean is destroyed */
  @PreDestroy
  public void shutdown() {
    log.info("Shutting down YOLO dataset executor service");
    yoloDatasetExecutor.shutdown();
  }

  /**
   * Load a TrainingRecord by ID for YOLO dataset generation. This method is called from the async
   * thread after transaction commit.
   *
   * @param trainingRecordId the training record ID
   * @return the TrainingRecord entity with project loaded, or null if not found
   */
  public TrainingRecord loadTrainingRecordForYoloGeneration(Long trainingRecordId) {
    // Use findByIdWithProject to eagerly fetch the project in a single query
    // This avoids LazyInitializationException in the async thread
    return trainingRecordRepository.findByIdWithProject(trainingRecordId).orElse(null);
  }

  @Value("${adc.yolo.zip.file.path:C:/NXP/landingai/yolo/}")
  private String yoloZipFilePath;

  @Value("${adc.yolo.zip.file.max.size:104857600}")
  private long yoloZipFileMaxSize; // Default 100MB

  @Value("${adc.yolo.zip.file.thread.pool.size:4}")
  private int zipThreadPoolSize; // Default 4 threads for parallel processing

  @Value("${adc.yolo.zip.file.batch.size:100}")
  private int zipBatchSize; // Default 100 images per batch

  private static final int MINIMUM_LABELED_IMAGES = 10;
  private static final String PROJECT_TYPE_OBJECT_DETECTION = "Object Detection";
  private static final String PROJECT_TYPE_SEGMENTATION = "Segmentation";
  private static final String PROJECT_TYPE_CLASSIFICATION = "Classification";

  /**
   * Start training with the provided parameters (legacy single-model support).
   *
   * @param request the training request
   * @param userId the user initiating training
   * @return the created training record DTO
   */
  @PostMapping("/start")
  @Transactional
  public TrainingRecordDTO startTraining(
      @RequestBody TrainingRequest request, @RequestParam("userId") String userId) {
    log.info("Starting training for project ID: {}", request.getProjectId());

    // Validate project exists
    Project project = validateAndGetProject(request.getProjectId());

    // Validate minimum labeled images
    validateMinimumLabeledImages(request.getProjectId(), request.getSnapshotId());

    // Create snapshot if snapshotId is null or 0
    Long snapshotId = request.getSnapshotId();
    if (snapshotId == null || snapshotId == 0L) {
      snapshotId = createAutoSnapshot(project, userId);
      log.info("Auto-created snapshot with ID: {} for training", snapshotId);
    }

    // Count images by split from snapshot table
    Long trainingCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "training");
    Long devCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "dev");
    Long testCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "test");

    // Create training record
    TrainingRecord trainingRecord = new TrainingRecord();
    trainingRecord.setProject(project);
    trainingRecord.setStatus("pending");
    trainingRecord.setModelAlias(request.getModelAlias());
    trainingRecord.setSnapshotId(snapshotId);
    trainingRecord.setEpochs(
        request.getEpochs() != null ? request.getEpochs() : 100); // Default 100 epochs
    trainingRecord.setModelSize(request.getModelSize() != null ? request.getModelSize() : "medium");
    trainingRecord.setTrainingCount(trainingCount.intValue());
    trainingRecord.setDevCount(devCount.intValue());
    trainingRecord.setTestCount(testCount.intValue());
    trainingRecord.setStartedAt(Instant.now());
    trainingRecord.setCreatedBy(userId);

    // Convert transform and augmentation params to JSON strings
    if (request.getTransformParams() != null && !request.getTransformParams().isEmpty()) {
      try {
        trainingRecord.setTransformParam(
            objectMapper.writeValueAsString(request.getTransformParams()));
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize transform params", e);
        throw new TrainingException("Failed to process transform parameters", e);
      }
    }

    if (request.getModelParams() != null && !request.getModelParams().isEmpty()) {
      try {
        trainingRecord.setModelParam(objectMapper.writeValueAsString(request.getModelParams()));
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize augmentation params", e);
        throw new TrainingException("Failed to process augmentation parameters", e);
      }
    }

    // Save training record
    TrainingRecord savedRecord = trainingRecordRepository.save(trainingRecord);
    trainingRecordRepository.flush();
    entityManager.refresh(savedRecord);
    log.info(
        "Training record created with ID: {} for model alias: {}, trackId: {}",
        savedRecord.getId(),
        savedRecord.getModelAlias(),
        savedRecord.getTrackId());

    // Start async YOLO dataset generation after transaction commits
    final Long trainingRecordId = savedRecord.getId();
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            YoloDatasetGenerationThread yoloThread =
                new YoloDatasetGenerationThread(trainingRecordId, TrainingService.this);
            yoloDatasetExecutor.submit(yoloThread);
            log.info(
                "Submitted async YOLO dataset generation task for training record ID: {} after transaction commit",
                trainingRecordId);
          }
        });

    return convertToDTO(savedRecord);
  }

  /**
   * Start training with multiple model configurations. Creates independent training records for
   * each model configuration.
   *
   * @param request the training request containing multiple model configs
   * @param userId the user initiating training
   * @return list of created training records, one per model configuration
   */
  @PostMapping("/start-multi")
  @Transactional
  public List<TrainingRecordDTO> startMultiConfigTraining(
      @Valid @RequestBody TrainingRequest request, @RequestParam("userId") String userId) {
    log.info(
        "Starting multi-config training for project ID: {} with {} configurations",
        request.getProjectId(),
        request.getModelConfigs() != null ? request.getModelConfigs().size() : 0);

    // Validate request has model configs
    if (request.getModelConfigs() == null || request.getModelConfigs().isEmpty()) {
      throw new TrainingException("At least one model configuration is required");
    }

    // Validate project exists
    Project project = validateAndGetProject(request.getProjectId());

    // Validate minimum labeled images
    validateMinimumLabeledImages(request.getProjectId(), request.getSnapshotId());

    // Create snapshot if snapshotId is null or 0
    Long snapshotId = request.getSnapshotId();
    if (snapshotId == null || snapshotId == 0L) {
      snapshotId = createAutoSnapshot(project, userId);
      log.info("Auto-created snapshot with ID: {} for multi-config training", snapshotId);
    }

    // Count images by split from snapshot table
    Long trainingCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "training");
    Long devCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "dev");
    Long testCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "test");

    List<TrainingRecordDTO> createdRecords = new ArrayList<>();
    List<Long> trainingRecordIds = new ArrayList<>();

    // Create a training record for each model configuration
    for (ModelConfigDTO modelConfig : request.getModelConfigs()) {
      // Validate model config parameters
      validateModelConfig(modelConfig);

      TrainingRecord trainingRecord =
          createTrainingRecordFromConfig(
              project,
              snapshotId,
              modelConfig,
              userId,
              trainingCount.intValue(),
              devCount.intValue(),
              testCount.intValue());

      TrainingRecord savedRecord = trainingRecordRepository.save(trainingRecord);
      trainingRecordRepository.flush();
      entityManager.refresh(savedRecord);
      log.info(
          "start-multi Training record created with ID: {} for model alias: {}, trackId: {}",
          savedRecord.getId(),
          modelConfig.getModelAlias(),
          savedRecord.getTrackId());

      trainingRecordIds.add(savedRecord.getId());
      createdRecords.add(convertToDTO(savedRecord));
    }

    // Start async YOLO dataset generation after transaction commits for all records
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            for (Long recordId : trainingRecordIds) {
              YoloDatasetGenerationThread yoloThread =
                  new YoloDatasetGenerationThread(recordId, TrainingService.this);
              yoloDatasetExecutor.submit(yoloThread);
              log.info(
                  "Submitted async YOLO dataset generation task for training record ID: {} after transaction commit",
                  recordId);
            }
          }
        });

    log.info(
        "Created {} training records for project ID: {}",
        createdRecords.size(),
        request.getProjectId());

    return createdRecords;
  }

  /**
   * Create a single training record from a model configuration.
   *
   * @param projectId the project ID
   * @param snapshotId the snapshot ID (optional)
   * @param modelConfig the model configuration
   * @param userId the user initiating training
   * @return the created training record DTO
   */
  @PostMapping("/create-record")
  @Transactional
  public TrainingRecordDTO createTrainingRecord(
      @RequestParam("projectId") @NotNull Long projectId,
      @RequestParam(value = "snapshotId", required = false) Long snapshotId,
      @Valid @RequestBody ModelConfigDTO modelConfig,
      @RequestParam("userId") String userId) {
    log.info(
        "Creating training record for project ID: {} with model alias: {}",
        projectId,
        modelConfig.getModelAlias());

    // Validate model config parameters
    validateModelConfig(modelConfig);

    // Validate project exists
    Project project = validateAndGetProject(projectId);

    // Validate minimum labeled images
    validateMinimumLabeledImages(projectId, snapshotId);

    // Create snapshot if snapshotId is null or 0
    if (snapshotId == null || snapshotId == 0L) {
      snapshotId = createAutoSnapshot(project, userId);
      log.info("Auto-created snapshot with ID: {} for training record", snapshotId);
    }

    // Count images by split from snapshot table
    Long trainingCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "training");
    Long devCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "dev");
    Long testCount = snapshotImageRepository.countBySnapshotIdAndSplit(snapshotId, "test");

    TrainingRecord trainingRecord =
        createTrainingRecordFromConfig(
            project,
            snapshotId,
            modelConfig,
            userId,
            trainingCount.intValue(),
            devCount.intValue(),
            testCount.intValue());

    TrainingRecord savedRecord = trainingRecordRepository.save(trainingRecord);
    trainingRecordRepository.flush();
    entityManager.refresh(savedRecord);
    log.info(
        "Training record created with ID: {}, trackId: {}",
        savedRecord.getId(),
        savedRecord.getTrackId());

    // Start async YOLO dataset generation after transaction commits
    final Long trainingRecordId = savedRecord.getId();
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            YoloDatasetGenerationThread yoloThread =
                new YoloDatasetGenerationThread(trainingRecordId, TrainingService.this);
            yoloDatasetExecutor.submit(yoloThread);
            log.info(
                "Submitted async YOLO dataset generation task for training record ID: {} after transaction commit",
                trainingRecordId);
          }
        });

    return convertToDTO(savedRecord);
  }

  /**
   * Get training status by ID.
   *
   * @param trainingId the training record ID
   * @return the training status DTO
   */
  @GetMapping("/{id}/status")
  @Transactional(readOnly = true)
  public TrainingStatusDTO getTrainingStatus(@PathVariable("id") Long trainingId) {
    log.info("Retrieving training status for ID: {}", trainingId);

    TrainingRecord trainingRecord =
        trainingRecordRepository
            .findById(trainingId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Training record not found with ID: " + trainingId));

    TrainingStatusDTO statusDTO = new TrainingStatusDTO();
    statusDTO.setId(trainingRecord.getId());
    statusDTO.setStatus(trainingRecord.getStatus());
    statusDTO.setStartedAt(trainingRecord.getStartedAt());

    // Calculate progress based on status
    Integer progress = calculateProgress(trainingRecord.getStatus());
    statusDTO.setProgress(progress);

    // Set current phase based on status
    String currentPhase = getCurrentPhase(trainingRecord.getStatus());
    statusDTO.setCurrentPhase(currentPhase);

    return statusDTO;
  }

  /**
   * Cancel a training job.
   *
   * @param trainingId the training record ID
   */
  @DeleteMapping("/{id}/cancel")
  @Transactional
  public void cancelTraining(@PathVariable("id") Long trainingId) {
    log.info("Cancelling training with ID: {}", trainingId);

    TrainingRecord trainingRecord =
        trainingRecordRepository
            .findById(trainingId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Training record not found with ID: " + trainingId));

    if ("completed".equals(trainingRecord.getStatus())) {
      throw new TrainingException("Cannot cancel a completed training");
    }

    trainingRecord.setStatus("cancelled");
    trainingRecord.setCompletedAt(Instant.now());
    trainingRecordRepository.save(trainingRecord);

    log.info("Training cancelled with ID: {}", trainingId);
  }

  /**
   * Get training record by ID.
   *
   * @param trainingId the training record ID
   * @return the training record DTO
   */
  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public TrainingRecordDTO getTrainingRecord(@PathVariable("id") Long trainingId) {
    log.info("Retrieving training record with ID: {}", trainingId);

    TrainingRecord trainingRecord =
        trainingRecordRepository
            .findById(trainingId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Training record not found with ID: " + trainingId));

    return convertToDTO(trainingRecord);
  }

  /**
   * Validate and get project by ID.
   *
   * @param projectId the project ID
   * @return the project entity
   * @throws EntityNotFoundException if project not found
   */
  private Project validateAndGetProject(Long projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));
  }

  /**
   * Validate minimum labeled images requirement. If snapshotId is provided, query from snapshot
   * table; otherwise query from main image table.
   *
   * @param projectId the project ID
   * @param snapshotId the snapshot ID (optional)
   * @throws InsufficientDataException if not enough labeled images
   */
  private void validateMinimumLabeledImages(Long projectId, Long snapshotId) {
    Long labeledImageCount;
    if (snapshotId != null && snapshotId > 0L) {
      // Query from snapshot table
      labeledImageCount = snapshotImageRepository.countBySnapshotIdAndIsLabeled(snapshotId, true);
      log.info(
          "Validating labeled images from snapshot ID: {}, count: {}",
          snapshotId,
          labeledImageCount);
    } else {
      // Query from main image table
      labeledImageCount = imageRepository.countByProject_IdAndIsLabeled(projectId, true);
      log.info(
          "Validating labeled images from project ID: {}, count: {}", projectId, labeledImageCount);
    }
    if (labeledImageCount < MINIMUM_LABELED_IMAGES) {
      throw new InsufficientDataException(
          String.format(
              "Project must have at least %d labeled images to start training. Current count: %d",
              MINIMUM_LABELED_IMAGES, labeledImageCount));
    }
  }

  /**
   * Validate model configuration parameters.
   *
   * @param modelConfig the model configuration to validate
   * @throws TrainingException if validation fails
   */
  private void validateModelConfig(ModelConfigDTO modelConfig) {
    if (modelConfig.getModelAlias() == null || modelConfig.getModelAlias().isBlank()) {
      throw new TrainingException("Model alias is required");
    }

    if (modelConfig.getEpochs() == null) {
      throw new TrainingException("Epochs is required");
    }

    if (modelConfig.getEpochs() < 1 || modelConfig.getEpochs() > 100) {
      throw new TrainingException("Epochs must be between 1 and 100");
    }

    if (modelConfig.getModelSize() == null || modelConfig.getModelSize().isBlank()) {
      throw new TrainingException("Model size is required");
    }
  }

  /**
   * Create a TrainingRecord entity from a ModelConfigDTO.
   *
   * @param project the project entity
   * @param snapshotId the snapshot ID (optional)
   * @param modelConfig the model configuration
   * @param userId the user ID
   * @param trainingCount count of training images
   * @param devCount count of dev images
   * @param testCount count of test images
   * @return the created TrainingRecord entity (not yet persisted)
   */
  private TrainingRecord createTrainingRecordFromConfig(
      Project project,
      Long snapshotId,
      ModelConfigDTO modelConfig,
      String userId,
      int trainingCount,
      int devCount,
      int testCount) {

    TrainingRecord trainingRecord = new TrainingRecord();
    trainingRecord.setProject(project);
    // Use status from modelConfig if provided, otherwise default to "PENDING"
    String status =
        (modelConfig.getStatus() != null && !modelConfig.getStatus().isBlank())
            ? modelConfig.getStatus()
            : "PENDING";
    trainingRecord.setStatus(status);
    trainingRecord.setModelAlias(modelConfig.getModelAlias());
    // Set snapshotId if provided (0 if null)
    trainingRecord.setSnapshotId(snapshotId != null ? snapshotId : 0L);
    trainingRecord.setEpochs(modelConfig.getEpochs());
    trainingRecord.setModelSize(modelConfig.getModelSize());
    trainingRecord.setTrainingCount(trainingCount);
    trainingRecord.setDevCount(devCount);
    trainingRecord.setTestCount(testCount);
    trainingRecord.setStartedAt(Instant.now());
    trainingRecord.setCreatedBy(userId);

    // Serialize transform config to JSON
    if (modelConfig.getTransforms() != null) {
      String transformJson = serializeTransformConfig(modelConfig.getTransforms());
      trainingRecord.setTransformParam(transformJson);
    }

    // Use raw modelParam JSON string directly if provided (from Model Parameters editor),
    // otherwise fall back to serializing the structured AugmentationConfigDTO
    if (modelConfig.getModelParam() != null && !modelConfig.getModelParam().isBlank()) {
      trainingRecord.setModelParam(modelConfig.getModelParam());
    } else if (modelConfig.getAugmentations() != null) {
      String augmentationJson = serializeAugmentationConfig(modelConfig.getAugmentations());
      trainingRecord.setModelParam(augmentationJson);
    }

    return trainingRecord;
  }

  /**
   * Serialize TransformConfigDTO to JSON string.
   *
   * @param transformConfig the transform configuration
   * @return JSON string representation
   * @throws TrainingException if serialization fails
   */
  private String serializeTransformConfig(TransformConfigDTO transformConfig) {
    try {
      return objectMapper.writeValueAsString(transformConfig);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize transform config", e);
      throw new TrainingException("Failed to process transform parameters", e);
    }
  }

  /**
   * Serialize AugmentationConfigDTO to JSON string.
   *
   * @param augmentationConfig the augmentation configuration
   * @return JSON string representation
   * @throws TrainingException if serialization fails
   */
  private String serializeAugmentationConfig(AugmentationConfigDTO augmentationConfig) {
    try {
      return objectMapper.writeValueAsString(augmentationConfig);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize augmentation config", e);
      throw new TrainingException("Failed to process augmentation parameters", e);
    }
  }

  /**
   * Convert TrainingRecord entity to DTO.
   *
   * @param trainingRecord the training record entity
   * @return the training record DTO
   */
  private TrainingRecordDTO convertToDTO(TrainingRecord trainingRecord) {
    TrainingRecordDTO dto = new TrainingRecordDTO();
    dto.setId(trainingRecord.getId());
    dto.setProjectId(trainingRecord.getProject().getId());
    dto.setStatus(trainingRecord.getStatus());
    dto.setModelAlias(trainingRecord.getModelAlias());
    dto.setTrackId(trainingRecord.getTrackId());
    dto.setEpochs(trainingRecord.getEpochs());
    dto.setModelSize(trainingRecord.getModelSize());
    dto.setTrainingCount(trainingRecord.getTrainingCount());
    dto.setDevCount(trainingRecord.getDevCount());
    dto.setTestCount(trainingRecord.getTestCount());
    dto.setStartedAt(trainingRecord.getStartedAt());
    dto.setCompletedAt(trainingRecord.getCompletedAt());
    dto.setCreatedBy(trainingRecord.getCreatedBy());
    return dto;
  }

  /**
   * Calculate progress percentage based on status.
   *
   * @param status the training status
   * @return progress percentage (0-100)
   */
  private Integer calculateProgress(String status) {
    return switch (status) {
      case "pending" -> 0;
      case "preparing" -> 10;
      case "training" -> 50;
      case "evaluating" -> 90;
      case "completed" -> 100;
      case "failed", "cancelled" -> 0;
      default -> 0;
    };
  }

  /**
   * Get current phase description based on status.
   *
   * @param status the training status
   * @return current phase description
   */
  private String getCurrentPhase(String status) {
    return switch (status) {
      case "pending" -> "Queued";
      case "preparing" -> "Preparing data";
      case "training" -> "Training model";
      case "evaluating" -> "Evaluating results";
      case "completed" -> "Completed";
      case "failed" -> "Failed";
      case "cancelled" -> "Cancelled";
      default -> "Unknown";
    };
  }

  /**
   * Create an auto-generated snapshot for training when no snapshot is provided. Snapshot name
   * format: Snapshot-${projectId}-${month}-${day}-${year}_${hours}:${minutes}
   *
   * @param project the project entity
   * @param userId the user creating the snapshot
   * @return the created snapshot ID
   */
  private Long createAutoSnapshot(Project project, String userId) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
    String snapshotName =
        String.format(
            "Snapshot-%d-%02d-%02d-%d_%02d:%02d",
            project.getId(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getYear(),
            now.getHour(),
            now.getMinute());

    Snapshot snapshot = new Snapshot();
    snapshot.setProject(project);
    snapshot.setSnapshotName(snapshotName);
    snapshot.setDescription("Auto-generated snapshot for training");
    snapshot.setCreatedBy(userId);

    Snapshot savedSnapshot = snapshotRepository.save(snapshot);
    // Flush to ensure the database trigger (tgf_do_snapshot) executes immediately
    // The trigger copies data from main tables to snapshot tables (_ss)
    snapshotRepository.flush();
    log.info("Auto-created snapshot '{}' with ID: {}", snapshotName, savedSnapshot.getId());

    return savedSnapshot.getId();
  }

  /**
   * Generate YOLO dataset zip files for a training record based on its project type. Supports
   * Object Detection, Segmentation, and Classification project types. The zip files are generated
   * in the configured path with model alias as subdirectory. If the total size exceeds the max size
   * limit, multiple zip files will be created. Data is retrieved from snapshot tables (_ss) using
   * the training record's snapshot ID.
   *
   * @param trainRecord the training record containing project and snapshot information
   * @return YoloDatasetResultDTO containing information about generated files
   */
  public YoloDatasetResultDTO generateYoloDataset(TrainingRecord trainRecord) {
    Long projectId = trainRecord.getProject().getId();
    String modelAlias = trainRecord.getModelAlias();
    Long snapshotId = trainRecord.getSnapshotId();
    log.info(
        "Generating YOLO dataset for project ID: {} with model alias: {} using snapshot ID: {}",
        projectId,
        modelAlias,
        snapshotId);

    try {
      // Get project from training record
      Project project = trainRecord.getProject();
      String projectType = project.getType();

      if (projectType == null || projectType.isBlank()) {
        throw new TrainingException("Project type is not defined");
      }

      if (snapshotId == null) {
        throw new TrainingException("Snapshot ID is not defined in training record");
      }

      // Create base directory path: yoloZipFilePath + modelAlias
      String basePath = yoloZipFilePath + modelAlias;
      log.info("yolo base path is {}", basePath);
      Path baseDir = Paths.get(basePath);
      Files.createDirectories(baseDir);

      // Get all classes from snapshot table, ordered by sequence for consistent YOLO class index
      // mapping
      List<SnapshotProjectClass> snapshotClasses =
          snapshotProjectClassRepository.findBySnapshotIdOrderBySequenceAsc(snapshotId);
      log.info("Found {} snapshot classes for snapshot ID: {}", snapshotClasses.size(), snapshotId);
      Map<Long, Integer> classIdToIndex =
          snapshotClasses.stream()
              .collect(
                  Collectors.toMap(SnapshotProjectClass::getId, SnapshotProjectClass::getSequence));

      // Get all labeled images from snapshot table (isLabeled = true only)
      List<SnapshotImage> allSnapshotImages =
          snapshotImageRepository.findBySnapshotIdAndIsLabeledTrue(snapshotId);
      log.info(
          "Found {} labeled snapshot images for snapshot ID: {}",
          allSnapshotImages.size(),
          snapshotId);

      // Filter images by split
      List<SnapshotImage> trainingImages =
          allSnapshotImages.stream()
              .filter(img -> "training".equals(img.getSplit()))
              .collect(Collectors.toList());
      List<SnapshotImage> devImages =
          allSnapshotImages.stream()
              .filter(img -> "dev".equals(img.getSplit()))
              .collect(Collectors.toList());
      List<SnapshotImage> testImages =
          allSnapshotImages.stream()
              .filter(img -> "test".equals(img.getSplit()))
              .collect(Collectors.toList());

      // Reset fileName from ImageFile for all snapshot images
      resetFileNamesFromImageFile(trainingImages);
      resetFileNamesFromImageFile(devImages);
      resetFileNamesFromImageFile(testImages);

      // Get all labels from snapshot table
      List<SnapshotImageLabel> allSnapshotLabels =
          snapshotImageLabelRepository.findBySnapshotId(snapshotId);

      // Generate dataset based on project type
      List<String> zipFilePaths;
      switch (projectType) {
        case PROJECT_TYPE_OBJECT_DETECTION:
          zipFilePaths =
              generateObjectDetectionDatasetFromSnapshot(
                  baseDir,
                  snapshotClasses,
                  classIdToIndex,
                  trainingImages,
                  devImages,
                  testImages,
                  allSnapshotLabels,
                  trainRecord);
          break;
        case PROJECT_TYPE_SEGMENTATION:
          zipFilePaths =
              generateSegmentationDatasetFromSnapshot(
                  baseDir,
                  snapshotClasses,
                  classIdToIndex,
                  trainingImages,
                  devImages,
                  testImages,
                  allSnapshotLabels,
                  trainRecord);
          break;
        case PROJECT_TYPE_CLASSIFICATION:
          zipFilePaths =
              generateClassificationDatasetFromSnapshot(
                  baseDir,
                  snapshotClasses,
                  classIdToIndex,
                  trainingImages,
                  devImages,
                  testImages,
                  allSnapshotLabels,
                  trainRecord);
          break;
        default:
          throw new TrainingException("Unsupported project type: " + projectType);
      }

      // Calculate total size
      long totalSize = zipFilePaths.stream().mapToLong(path -> new File(path).length()).sum();

      // Create DatabricksRequest with trackId, zipFilenames and zipPath
      List<String> zipFilenames =
          zipFilePaths.stream().map(path -> new File(path).getName()).collect(Collectors.toList());
      DatabricksRequest databricksRequest = new DatabricksRequest();
      databricksRequest.setTrackId(trainRecord.getTrackId());
      databricksRequest.setZipFilenames(zipFilenames);
      databricksRequest.setZipPath(basePath + "/");
      log.info(
          "Successfully generated DatabricksRequest {}, for project ID: {}",
          databricksRequest,
          projectId);

      log.info(
          "Successfully generated {} YOLO dataset zip file(s) for project ID: {}",
          zipFilePaths.size(),
          projectId);

      // Call DatabricksController to submit training request
      submitTrainingToDatabricks(databricksRequest, trainRecord);

      return YoloDatasetResultDTO.builder()
          .modelAlias(modelAlias)
          .projectId(projectId)
          .projectType(projectType)
          .basePath(basePath)
          .zipFilePaths(zipFilePaths)
          .totalImages(trainingImages.size() + devImages.size() + testImages.size())
          .trainingImages(trainingImages.size())
          .validationImages(devImages.size())
          .testImages(testImages.size())
          .classCount(snapshotClasses.size())
          .totalSize(totalSize)
          .success(true)
          .databricksRequest(databricksRequest)
          .build();

    } catch (Exception e) {
      log.error("Failed to generate YOLO dataset for project ID: {}", projectId, e);
      return YoloDatasetResultDTO.builder()
          .modelAlias(modelAlias)
          .projectId(projectId)
          .success(false)
          .errorMessage(e.getMessage())
          .build();
    }
  }

  /**
   * Submit training request to DatabricksController via REST API.
   *
   * @param databricksRequest the request containing trackId, zipFilenames and zipPath
   * @param trainRecord the training record to update status
   */
  private void submitTrainingToDatabricks(
      DatabricksRequest databricksRequest, TrainingRecord trainRecord) {
    try {
      // Build the request body as JSON using ObjectMapper
      // Format: {"trackId":"xxx","zipFilenames":["file1.zip","file2.zip"],"zipPath":"path/"}
      String requestBody = objectMapper.writeValueAsString(databricksRequest);

      log.info("Training request requestBody is: {}", requestBody);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

      String url = iemdmInterfaceUri + "/infc/databricks/training";
      log.info(
          "Submitting training request to Databricks: URL={}, trackId={}",
          url,
          databricksRequest.getTrackId());

      ResponseEntity<String> response =
          restTemplate.postForEntity(url, requestEntity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info(
            "Successfully submitted training request to Databricks, response: {}",
            response.getBody());

        // Extract runId from response and set to modelTrackKey
        String responseBody = response.getBody();
        if (responseBody != null) {
          JsonNode jsonNode = objectMapper.readTree(responseBody);
          JsonNode runIdNode = jsonNode.get("runId");
          if (runIdNode != null && !runIdNode.isNull()) {
            trainRecord.setModelTrackKey(runIdNode.asText());
            log.info(
                "Set modelTrackKey to runId: {} for trackId: {}",
                runIdNode.asText(),
                trainRecord.getTrackId());
          }
        }

        // Update training record status to WAITFORRESULT
        trainRecord.setStatus("WAITFORRESULT");
        trainingRecordRepository.save(trainRecord);
        log.info(
            "Updated training record status to WAITFORRESULT for trackId: {}",
            trainRecord.getTrackId());
      } else {
        log.warn(
            "Databricks training submission returned non-success status: {}",
            response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Failed to submit training request to Databricks: {}", e.getMessage(), e);
      // Don't throw exception to avoid breaking the YOLO dataset generation flow
      // The training submission failure should be handled separately
    }
  }

  /**
   * Reset fileName for each SnapshotImage from the corresponding ImageFile. This ensures the
   * correct file name is used when generating the dataset.
   *
   * @param snapshotImages list of snapshot images to update
   */
  private void resetFileNamesFromImageFile(List<SnapshotImage> snapshotImages) {
    if (snapshotImages == null || snapshotImages.isEmpty()) {
      return;
    }

    // Collect all fileIds
    List<Long> fileIds =
        snapshotImages.stream()
            .map(SnapshotImage::getFileId)
            .filter(fileId -> fileId != null)
            .distinct()
            .collect(Collectors.toList());

    if (fileIds.isEmpty()) {
      return;
    }

    // Batch query ImageFile entities
    List<ImageFile> imageFiles = imageFileRepository.findAllById(fileIds);

    // Create a map of fileId to fileName
    Map<Long, String> fileIdToFileName =
        imageFiles.stream().collect(Collectors.toMap(ImageFile::getId, ImageFile::getFileName));

    // Update each SnapshotImage's fileName
    for (SnapshotImage snapshotImage : snapshotImages) {
      if (snapshotImage.getFileId() != null) {
        String imageFileName = fileIdToFileName.get(snapshotImage.getFileId());
        if (imageFileName != null) {
          snapshotImage.setFileName(imageFileName);
        }
      }
    }
  }

  /**
   * Generate Object Detection dataset in YOLO format. Structure (inside zip, root folder is
   * 'dataset/'): - dataset/images/train/, dataset/images/val/, dataset/images/test/ -
   * dataset/labels/train/, dataset/labels/val/, dataset/labels/test/ - dataset/data.yaml
   */
  private List<String> generateObjectDetectionDataset(
      Path baseDir,
      List<ProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<Image> trainingImages,
      List<Image> devImages,
      List<Image> testImages)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    List<ZipEntryData> allEntries = new ArrayList<>();

    // Generate data.yaml content (placed in dataset/ root)
    String dataYaml = generateDataYaml(baseDir.toString(), classes, "detect");
    allEntries.add(
        new ZipEntryData("dataset/data.yaml", dataYaml.getBytes(StandardCharsets.UTF_8)));

    // Process training images (path: dataset/images/train/)
    allEntries.addAll(processImagesForYolo(trainingImages, classIdToIndex, "train", false));

    // Process validation images (path: dataset/images/val/)
    allEntries.addAll(processImagesForYolo(devImages, classIdToIndex, "val", false));

    // Process test images (path: dataset/images/test/)
    allEntries.addAll(processImagesForYolo(testImages, classIdToIndex, "test", false));

    // Create zip files with size limit
    zipFilePaths.addAll(createZipFilesWithSizeLimit(baseDir, allEntries, "dataset"));

    return zipFilePaths;
  }

  /**
   * Generate Segmentation dataset in YOLO format. Structure is identical to Object Detection but
   * labels contain polygon coordinates. - dataset/images/train/, dataset/images/val/,
   * dataset/images/test/ - dataset/labels/train/, dataset/labels/val/, dataset/labels/test/ -
   * dataset/data.yaml
   */
  private List<String> generateSegmentationDataset(
      Path baseDir,
      List<ProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<Image> trainingImages,
      List<Image> devImages,
      List<Image> testImages)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    List<ZipEntryData> allEntries = new ArrayList<>();

    // Generate data.yaml content (same format as detection)
    String dataYaml = generateDataYaml(baseDir.toString(), classes, "segment");
    allEntries.add(
        new ZipEntryData("dataset/data.yaml", dataYaml.getBytes(StandardCharsets.UTF_8)));

    // Process training images (with segmentation format)
    allEntries.addAll(processImagesForYolo(trainingImages, classIdToIndex, "train", true));

    // Process validation images
    allEntries.addAll(processImagesForYolo(devImages, classIdToIndex, "val", true));

    // Process test images
    allEntries.addAll(processImagesForYolo(testImages, classIdToIndex, "test", true));

    // Create zip files with size limit
    zipFilePaths.addAll(createZipFilesWithSizeLimit(baseDir, allEntries, "dataset"));

    return zipFilePaths;
  }

  /**
   * Generate Classification dataset in YOLO format. Structure (NO data.yaml needed - YOLO
   * auto-infers class from folder names): - train/class_name/image.jpg - val/class_name/image.jpg -
   * test/class_name/image.jpg
   */
  private List<String> generateClassificationDataset(
      Path baseDir,
      List<ProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<Image> trainingImages,
      List<Image> devImages,
      List<Image> testImages)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    List<ZipEntryData> allEntries = new ArrayList<>();

    // NOTE: Classification does NOT need data.yaml file
    // YOLO automatically maps folder names to class IDs (0-indexed)

    // Process images for classification (organized by class folders)
    allEntries.addAll(processImagesForClassification(trainingImages, classes, "train"));
    allEntries.addAll(processImagesForClassification(devImages, classes, "val"));
    allEntries.addAll(processImagesForClassification(testImages, classes, "test"));

    // Create zip files with size limit
    zipFilePaths.addAll(createZipFilesWithSizeLimit(baseDir, allEntries, "dataset"));

    return zipFilePaths;
  }

  // ==================== Snapshot-based dataset generation methods ====================

  /**
   * Generate Object Detection dataset in YOLO format from snapshot data. Structure (inside zip,
   * root folder is 'dataset/'): - dataset/images/train/, dataset/images/val/, dataset/images/test/
   * - dataset/labels/train/, dataset/labels/val/, dataset/labels/test/ - dataset/data.yaml -
   * dataset/model_metadata.json (training parameters)
   *
   * <p>Optimized with streaming pipeline: BufferedOutputStream, BEST_SPEED compression, parallel
   * batch processing, and batch database queries.
   */
  private List<String> generateObjectDetectionDatasetFromSnapshot(
      Path baseDir,
      List<SnapshotProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<SnapshotImage> trainingImages,
      List<SnapshotImage> devImages,
      List<SnapshotImage> testImages,
      List<SnapshotImageLabel> allLabels,
      TrainingRecord trainRecord)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    String zipFileName = trainRecord.getTrackId() + ".zip";
    Path zipPath = baseDir.resolve(zipFileName);

    // Pre-build labels map for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));

    // Use streaming pipeline with BufferedOutputStream and fast compression
    // Collect all written entry paths for generating model_file_structure.json
    List<String> writtenEntryPaths = Collections.synchronizedList(new ArrayList<>());

    try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
        BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
        ZipOutputStream zos = new ZipOutputStream(bos)) {

      // Use fast compression for better performance
      zos.setLevel(Deflater.BEST_SPEED);

      // Generate data.yaml content (placed in dataset/ root)
      String dataYaml = generateDataYamlFromSnapshot(baseDir.toString(), classes, "detect");
      zos.putNextEntry(new ZipEntry("dataset/data.yaml"));
      zos.write(dataYaml.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("dataset/data.yaml");

      // Generate model_metadata.json content (placed in dataset/ root alongside data.yaml)
      String modelMetadataJson = generateModelMetadataJson(trainRecord);
      zos.putNextEntry(new ZipEntry("dataset/model_metadata.json"));
      zos.write(modelMetadataJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("dataset/model_metadata.json");

      // Generate model_image_structure.json content (placed in dataset/ root)
      String imageStructureJson =
          generateImageStructureJson(
              trainingImages, devImages, testImages, labelsByImageId, null, false);
      zos.putNextEntry(new ZipEntry("dataset/model_image_structure.json"));
      zos.write(imageStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("dataset/model_image_structure.json");

      // Write empty directory entries to ensure folder structure exists
      writeEmptyDirectoryEntries(zos);

      // Process images in parallel batches
      ExecutorService executor = Executors.newFixedThreadPool(zipThreadPoolSize);
      List<Future<Void>> futures = new ArrayList<>();

      // Process training images (path: dataset/images/train/)
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              trainingImages,
              "train",
              classIdToIndex,
              labelsByImageId,
              zos,
              false,
              writtenEntryPaths));

      // Process validation images (path: dataset/images/val/)
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              devImages,
              "val",
              classIdToIndex,
              labelsByImageId,
              zos,
              false,
              writtenEntryPaths));

      // Process test images (path: dataset/images/test/)
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              testImages,
              "test",
              classIdToIndex,
              labelsByImageId,
              zos,
              false,
              writtenEntryPaths));

      // Wait for all batches to complete
      waitForFutures(futures);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.HOURS);

      // Generate model_file_structure.json as the last entry (placed in dataset/ root)
      String fileStructureJson = generateFileStructureJson(writtenEntryPaths);
      zos.putNextEntry(new ZipEntry("dataset/model_file_structure.json"));
      zos.write(fileStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while generating dataset", e);
    } catch (Exception e) {
      throw new IOException("Failed to generate dataset: " + e.getMessage(), e);
    }

    zipFilePaths.add(zipPath.toString());

    // Check if file exceeds size limit and split if necessary
    File zipFile = zipPath.toFile();
    if (zipFile.length() > yoloZipFileMaxSize) {
      log.info(
          "Zip file exceeds size limit ({} > {}), splitting into parts",
          zipFile.length(),
          yoloZipFileMaxSize);
      zipFilePaths.clear();
      zipFilePaths.addAll(splitZipFileByBytes(zipFile, baseDir, trainRecord.getTrackId()));
      Files.deleteIfExists(zipPath);
    }

    return zipFilePaths;
  }

  /**
   * Generate Segmentation dataset in YOLO format from snapshot data. Structure is identical to
   * Object Detection but labels contain polygon coordinates. - dataset/images/train/,
   * dataset/images/val/, dataset/images/test/ - dataset/labels/train/, dataset/labels/val/,
   * dataset/labels/test/ - dataset/data.yaml
   *
   * <p>Optimized with streaming pipeline: BufferedOutputStream, BEST_SPEED compression, parallel
   * batch processing, and batch database queries.
   */
  private List<String> generateSegmentationDatasetFromSnapshot(
      Path baseDir,
      List<SnapshotProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<SnapshotImage> trainingImages,
      List<SnapshotImage> devImages,
      List<SnapshotImage> testImages,
      List<SnapshotImageLabel> allLabels,
      TrainingRecord trainRecord)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    String zipFileName = trainRecord.getTrackId() + ".zip";
    Path zipPath = baseDir.resolve(zipFileName);

    // Pre-build labels map for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));

    // Use streaming pipeline with BufferedOutputStream and fast compression
    // Collect all written entry paths for generating model_file_structure.json
    List<String> writtenEntryPaths = Collections.synchronizedList(new ArrayList<>());

    try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
        BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
        ZipOutputStream zos = new ZipOutputStream(bos)) {

      // Use fast compression for better performance
      zos.setLevel(Deflater.BEST_SPEED);

      // Generate data.yaml content (same format as detection)
      String dataYaml = generateDataYamlFromSnapshot(baseDir.toString(), classes, "segment");
      zos.putNextEntry(new ZipEntry("dataset/data.yaml"));
      zos.write(dataYaml.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("dataset/data.yaml");

      // Generate model_image_structure.json content (placed in dataset/ root)
      String imageStructureJson =
          generateImageStructureJson(
              trainingImages, devImages, testImages, labelsByImageId, null, false);
      zos.putNextEntry(new ZipEntry("dataset/model_image_structure.json"));
      zos.write(imageStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("dataset/model_image_structure.json");

      // Write empty directory entries to ensure folder structure exists
      writeEmptyDirectoryEntries(zos);

      // Process images in parallel batches
      ExecutorService executor = Executors.newFixedThreadPool(zipThreadPoolSize);
      List<Future<Void>> futures = new ArrayList<>();

      // Process training images (with segmentation format)
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              trainingImages,
              "train",
              classIdToIndex,
              labelsByImageId,
              zos,
              true,
              writtenEntryPaths));

      // Process validation images
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              devImages,
              "val",
              classIdToIndex,
              labelsByImageId,
              zos,
              true,
              writtenEntryPaths));

      // Process test images
      futures.addAll(
          processSnapshotImagesInBatches(
              executor,
              testImages,
              "test",
              classIdToIndex,
              labelsByImageId,
              zos,
              true,
              writtenEntryPaths));

      // Wait for all batches to complete
      waitForFutures(futures);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.HOURS);

      // Generate model_file_structure.json as the last entry (placed in dataset/ root)
      String fileStructureJson = generateFileStructureJson(writtenEntryPaths);
      zos.putNextEntry(new ZipEntry("dataset/model_file_structure.json"));
      zos.write(fileStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while generating dataset", e);
    } catch (Exception e) {
      throw new IOException("Failed to generate dataset: " + e.getMessage(), e);
    }

    zipFilePaths.add(zipPath.toString());

    // Check if file exceeds size limit and split if necessary
    File zipFile = zipPath.toFile();
    if (zipFile.length() > yoloZipFileMaxSize) {
      log.info(
          "Zip file exceeds size limit ({} > {}), splitting into parts",
          zipFile.length(),
          yoloZipFileMaxSize);
      zipFilePaths.clear();
      zipFilePaths.addAll(splitZipFileByBytes(zipFile, baseDir, trainRecord.getTrackId()));
      Files.deleteIfExists(zipPath);
    }

    return zipFilePaths;
  }

  /**
   * Generate Classification dataset in YOLO format from snapshot data. Structure (NO data.yaml
   * needed - YOLO auto-infers class from folder names): - train/class_name/image.jpg -
   * val/class_name/image.jpg - test/class_name/image.jpg
   *
   * <p>Optimized with streaming pipeline: BufferedOutputStream, BEST_SPEED compression, parallel
   * batch processing, and batch database queries.
   */
  private List<String> generateClassificationDatasetFromSnapshot(
      Path baseDir,
      List<SnapshotProjectClass> classes,
      Map<Long, Integer> classIdToIndex,
      List<SnapshotImage> trainingImages,
      List<SnapshotImage> devImages,
      List<SnapshotImage> testImages,
      List<SnapshotImageLabel> allLabels,
      TrainingRecord trainRecord)
      throws IOException {

    List<String> zipFilePaths = new ArrayList<>();
    String zipFileName = trainRecord.getTrackId() + ".zip";
    Path zipPath = baseDir.resolve(zipFileName);

    // Pre-build labels map for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));

    // Build class ID to name map
    Map<Long, String> classIdToName =
        classes.stream()
            .collect(
                Collectors.toMap(SnapshotProjectClass::getId, SnapshotProjectClass::getClassName));

    // Use streaming pipeline with BufferedOutputStream and fast compression
    // Collect all written entry paths for generating model_file_structure.json
    List<String> writtenEntryPaths = Collections.synchronizedList(new ArrayList<>());

    try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
        BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
        ZipOutputStream zos = new ZipOutputStream(bos)) {

      // Use fast compression for better performance
      zos.setLevel(Deflater.BEST_SPEED);

      // NOTE: Classification does NOT need data.yaml file
      // YOLO automatically maps folder names to class IDs (0-indexed)

      // Generate model_image_structure.json content (placed in dataset/ root)
      String imageStructureJson =
          generateImageStructureJson(
              trainingImages, devImages, testImages, labelsByImageId, classIdToName, true);
      zos.putNextEntry(new ZipEntry("model_image_structure.json"));
      zos.write(imageStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      writtenEntryPaths.add("model_image_structure.json");

      // Write empty directory entries for classification to ensure folder structure exists
      writeEmptyClassificationDirectoryEntries(zos, classes);

      // Process images in parallel batches
      ExecutorService executor = Executors.newFixedThreadPool(zipThreadPoolSize);
      List<Future<Void>> futures = new ArrayList<>();

      // Process images for classification (organized by class folders)
      futures.addAll(
          processSnapshotClassificationImagesInBatches(
              executor,
              trainingImages,
              "train",
              classIdToName,
              labelsByImageId,
              zos,
              writtenEntryPaths));
      futures.addAll(
          processSnapshotClassificationImagesInBatches(
              executor, devImages, "val", classIdToName, labelsByImageId, zos, writtenEntryPaths));
      futures.addAll(
          processSnapshotClassificationImagesInBatches(
              executor,
              testImages,
              "test",
              classIdToName,
              labelsByImageId,
              zos,
              writtenEntryPaths));

      // Wait for all batches to complete
      waitForFutures(futures);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.HOURS);

      // Generate model_file_structure.json as the last entry (same level as train/val/test)
      String fileStructureJson = generateFileStructureJson(writtenEntryPaths);
      zos.putNextEntry(new ZipEntry("model_file_structure.json"));
      zos.write(fileStructureJson.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while generating dataset", e);
    } catch (Exception e) {
      throw new IOException("Failed to generate dataset: " + e.getMessage(), e);
    }

    zipFilePaths.add(zipPath.toString());

    // Check if file exceeds size limit and split if necessary
    File zipFile = zipPath.toFile();
    if (zipFile.length() > yoloZipFileMaxSize) {
      log.info(
          "Zip file exceeds size limit ({} > {}), splitting into parts",
          zipFile.length(),
          yoloZipFileMaxSize);
      zipFilePaths.clear();
      zipFilePaths.addAll(splitZipFileByBytes(zipFile, baseDir, trainRecord.getTrackId()));
      Files.deleteIfExists(zipPath);
    }

    return zipFilePaths;
  }

  /** Generate data.yaml content for YOLO detection/segmentation from snapshot classes. */
  private String generateDataYamlFromSnapshot(
      String basePath, List<SnapshotProjectClass> classes, String task) {
    StringBuilder yaml = new StringBuilder();
    yaml.append("# YOLO Dataset Configuration\n");
    yaml.append("# Path will be configured by data scientist\n");
    yaml.append("path: dataset\n\n");
    yaml.append("# Dataset splits (relative to path)\n");
    yaml.append("train: images/train\n");
    yaml.append("val: images/val\n");
    yaml.append("test: images/test\n\n");
    yaml.append("# Class names (ID: name mapping)\n");
    yaml.append("names:\n");
    for (int i = 0; i < classes.size(); i++) {
      yaml.append("  ").append(i).append(": ").append(classes.get(i).getClassName()).append("\n");
    }
    return yaml.toString();
  }

  /**
   * Generate model_metadata.json content containing model training parameters. This file is placed
   * alongside data.yaml in the dataset folder.
   *
   * @param trainRecord the training record containing model parameters
   * @return JSON string with model metadata
   */
  private String generateModelMetadataJson(TrainingRecord trainRecord) {

    try {
      java.util.LinkedHashMap<String, Object> metadata = new java.util.LinkedHashMap<>();
      metadata.put(
          "job_id",
          this.configurationValueService
              .getConfigurationItemForKey("Databricks_Job_ID")
              .getConfigurationValue());
      metadata.put("project_name", trainRecord.getProject().getName());
      metadata.put("exp_name", trainRecord.getModelAlias());
      metadata.put("model_name", trainRecord.getModelSize());

      // Parse transformParam JSON string to Object to avoid double escaping
      if (trainRecord.getTransformParam() != null && !trainRecord.getTransformParam().isBlank()) {
        metadata.put(
            "transformParam",
            objectMapper.readValue(trainRecord.getTransformParam(), Object.class));
      } else {
        metadata.put("transformParam", null);
      }

      // Parse modelParam JSON string to Object to avoid double escaping
      if (trainRecord.getModelParam() != null && !trainRecord.getModelParam().isBlank()) {
        metadata.put(
            "modelParam", objectMapper.readValue(trainRecord.getModelParam(), Object.class));
      } else {
        metadata.put("modelParam", null);
      }

      metadata.put("trainingCount", trainRecord.getTrainingCount());
      metadata.put("devCount", trainRecord.getDevCount());
      metadata.put("testCount", trainRecord.getTestCount());
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
    } catch (JsonProcessingException e) {
      log.error("Failed to generate model metadata JSON", e);
      throw new TrainingException("Failed to generate model metadata", e);
    }
  }

  /**
   * Generate model_image_structure.json content containing the image file structure in the zip.
   * This file records the hierarchical folder structure with image names. Format: nested objects
   * where each folder is a level, leaf nodes are arrays of image names. For Object
   * Detection/Segmentation: { "dataset": { "images": { "train": [...], "val": [...], "test": [...]
   * } } } For Classification: { "train": { "className": [...] }, "val": { "className": [...] }, ...
   * }
   *
   * @param trainingImages list of training images
   * @param devImages list of validation images
   * @param testImages list of test images
   * @param labelsByImageId map of image ID to labels (used for classification to get class name)
   * @param classIdToName map of class ID to class name (used for classification)
   * @param isClassification whether this is a classification dataset
   * @return JSON string with image structure
   */
  private String generateImageStructureJson(
      List<SnapshotImage> trainingImages,
      List<SnapshotImage> devImages,
      List<SnapshotImage> testImages,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      Map<Long, String> classIdToName,
      boolean isClassification) {
    try {
      // Use a flat map to collect images by path first
      java.util.LinkedHashMap<String, List<String>> flatStructure = new java.util.LinkedHashMap<>();

      // Process training images
      addImagesToFlatStructure(
          flatStructure, trainingImages, "train", labelsByImageId, classIdToName, isClassification);

      // Process validation images
      addImagesToFlatStructure(
          flatStructure, devImages, "val", labelsByImageId, classIdToName, isClassification);

      // Process test images
      addImagesToFlatStructure(
          flatStructure, testImages, "test", labelsByImageId, classIdToName, isClassification);

      // Convert flat structure to nested hierarchical structure
      java.util.LinkedHashMap<String, Object> nestedStructure = buildNestedStructure(flatStructure);

      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(nestedStructure);
    } catch (JsonProcessingException e) {
      log.error("Failed to generate image structure JSON", e);
      throw new TrainingException("Failed to generate image structure", e);
    }
  }

  /**
   * Add images to the flat structure map with folder path as key and image names as values.
   *
   * @param structure the map to add images to
   * @param images list of images to process
   * @param split the split name (train/val/test)
   * @param labelsByImageId map of image ID to labels
   * @param classIdToName map of class ID to class name
   * @param isClassification whether this is a classification dataset
   */
  private void addImagesToFlatStructure(
      Map<String, List<String>> structure,
      List<SnapshotImage> images,
      String split,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      Map<Long, String> classIdToName,
      boolean isClassification) {

    for (SnapshotImage image : images) {
      if (image.getFileId() == null || image.getFileName() == null) {
        continue;
      }

      String folderPath;
      if (isClassification) {
        // For classification, get the class name from labels
        List<SnapshotImageLabel> labels =
            labelsByImageId.getOrDefault(image.getId(), Collections.emptyList());
        if (labels.isEmpty()) {
          continue;
        }
        SnapshotImageLabel primaryLabel = labels.get(0);
        if (primaryLabel.getClassId() == null) {
          continue;
        }
        String className = classIdToName.get(primaryLabel.getClassId());
        if (className == null) {
          continue;
        }
        String safeClassName = className.replaceAll("[^a-zA-Z0-9_-]", "_");
        folderPath = split + "/" + safeClassName;
      } else {
        // For object detection/segmentation
        folderPath = "dataset/images/" + split;
      }

      // Add image name to the folder's list
      structure.computeIfAbsent(folderPath, k -> new ArrayList<>()).add(image.getFileName());
    }
  }

  /**
   * Build a nested hierarchical structure from a flat path-to-images map. Converts
   * "dataset/images/train" -> ["img1.jpg"] to { "dataset": { "images": { "train": ["img1.jpg"] } }
   * }
   *
   * @param flatStructure the flat map with path as key and image list as value
   * @return nested hierarchical map
   */
  @SuppressWarnings("unchecked")
  private java.util.LinkedHashMap<String, Object> buildNestedStructure(
      Map<String, List<String>> flatStructure) {
    java.util.LinkedHashMap<String, Object> root = new java.util.LinkedHashMap<>();

    for (Map.Entry<String, List<String>> entry : flatStructure.entrySet()) {
      String path = entry.getKey();
      List<String> images = entry.getValue();

      // Split path into parts
      String[] parts = path.split("/");

      // Navigate/create nested structure
      java.util.LinkedHashMap<String, Object> current = root;
      for (int i = 0; i < parts.length - 1; i++) {
        String part = parts[i];
        if (!current.containsKey(part)) {
          current.put(part, new java.util.LinkedHashMap<String, Object>());
        }
        current = (java.util.LinkedHashMap<String, Object>) current.get(part);
      }

      // Set the leaf node (last part) to the image list
      current.put(parts[parts.length - 1], images);
    }

    return root;
  }

  /**
   * Create empty directory entries for YOLO detection/segmentation dataset structure. This ensures
   * train/val/test folders exist even when there are no images.
   */
  private List<ZipEntryData> createEmptyDirectoryEntries() {
    List<ZipEntryData> entries = new ArrayList<>();
    String[] splits = {"train", "val", "test"};

    for (String split : splits) {
      // Add empty directory entries (directories in zip end with /)
      entries.add(new ZipEntryData("dataset/images/" + split + "/", new byte[0]));
      entries.add(new ZipEntryData("dataset/labels/" + split + "/", new byte[0]));
    }

    return entries;
  }

  /**
   * Write empty directory entries for YOLO detection/segmentation dataset structure directly to
   * ZipOutputStream. This ensures train/val/test folders exist even when there are no images.
   */
  private void writeEmptyDirectoryEntries(ZipOutputStream zos) throws IOException {
    String[] splits = {"train", "val", "test"};
    for (String split : splits) {
      zos.putNextEntry(new ZipEntry("dataset/images/" + split + "/"));
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("dataset/labels/" + split + "/"));
      zos.closeEntry();
    }
  }

  /**
   * Write empty directory entries for YOLO classification dataset structure directly to
   * ZipOutputStream. This ensures train/val/test folders with class subfolders exist.
   */
  private void writeEmptyClassificationDirectoryEntries(
      ZipOutputStream zos, List<SnapshotProjectClass> classes) throws IOException {
    String[] splits = {"train", "val", "test"};
    for (String split : splits) {
      // Add split directory
      zos.putNextEntry(new ZipEntry(split + "/"));
      zos.closeEntry();
      // Add class subdirectories for each split
      for (SnapshotProjectClass cls : classes) {
        String safeClassName = cls.getClassName().replaceAll("[^a-zA-Z0-9_-]", "_");
        zos.putNextEntry(new ZipEntry(split + "/" + safeClassName + "/"));
        zos.closeEntry();
      }
    }
  }

  /**
   * Process snapshot images in batches with parallel execution for YOLO detection/segmentation.
   *
   * @param executor the thread pool executor
   * @param images list of images to process
   * @param split the split name (train/val/test)
   * @param classIdToIndex mapping from class ID to YOLO index
   * @param labelsByImageId pre-built map of image ID to labels
   * @param zos the ZipOutputStream to write to
   * @param isSegmentation whether to use segmentation format
   * @return list of futures for tracking completion
   */
  private List<Future<Void>> processSnapshotImagesInBatches(
      ExecutorService executor,
      List<SnapshotImage> images,
      String split,
      Map<Long, Integer> classIdToIndex,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      ZipOutputStream zos,
      boolean isSegmentation,
      List<String> writtenEntryPaths) {

    List<Future<Void>> futures = new ArrayList<>();

    for (int i = 0; i < images.size(); i += zipBatchSize) {
      int end = Math.min(i + zipBatchSize, images.size());
      List<SnapshotImage> batch = new ArrayList<>(images.subList(i, end));

      futures.add(
          executor.submit(
              () -> {
                processSnapshotYoloBatch(
                    batch,
                    split,
                    classIdToIndex,
                    labelsByImageId,
                    zos,
                    isSegmentation,
                    writtenEntryPaths);
                return null;
              }));
    }

    return futures;
  }

  /**
   * Process a batch of snapshot images for YOLO detection/segmentation format. Uses batch database
   * query for image files.
   */
  private void processSnapshotYoloBatch(
      List<SnapshotImage> batch,
      String split,
      Map<Long, Integer> classIdToIndex,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      ZipOutputStream zos,
      boolean isSegmentation,
      List<String> writtenEntryPaths)
      throws IOException {

    // Batch query image file data
    List<Long> fileIds =
        batch.stream()
            .map(SnapshotImage::getFileId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    if (fileIds.isEmpty()) {
      return;
    }

    // Batch fetch image files
    List<ImageFile> imageFiles = imageFileRepository.findAllById(fileIds);
    Map<Long, byte[]> imageDataMap =
        imageFiles.stream()
            .collect(Collectors.toMap(ImageFile::getId, ImageFile::getImageFileStream));

    for (SnapshotImage image : batch) {
      if (image.getFileId() == null) {
        continue;
      }

      byte[] imageData = imageDataMap.get(image.getFileId());
      if (imageData == null) {
        continue;
      }

      // Write image file: dataset/images/{split}/{filename}
      String imagePath = "dataset/images/" + split + "/" + image.getFileName();
      synchronized (zos) {
        zos.putNextEntry(new ZipEntry(imagePath));
        zos.write(imageData);
        zos.closeEntry();
      }
      writtenEntryPaths.add(imagePath);

      // Generate and write label file
      List<SnapshotImageLabel> labels =
          labelsByImageId.getOrDefault(image.getId(), Collections.emptyList());
      String labelContent =
          generateYoloLabelContentFromSnapshot(
              labels, classIdToIndex, image.getWidth(), image.getHeight(), isSegmentation);

      String labelFileName = image.getFileName().replaceAll("\\.[^.]+$", ".txt");
      String labelPath = "dataset/labels/" + split + "/" + labelFileName;
      synchronized (zos) {
        zos.putNextEntry(new ZipEntry(labelPath));
        zos.write(labelContent.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
      }
      writtenEntryPaths.add(labelPath);
    }
  }

  /**
   * Process snapshot images in batches with parallel execution for classification.
   *
   * @param executor the thread pool executor
   * @param images list of images to process
   * @param split the split name (train/val/test)
   * @param classIdToName mapping from class ID to class name
   * @param labelsByImageId pre-built map of image ID to labels
   * @param zos the ZipOutputStream to write to
   * @return list of futures for tracking completion
   */
  private List<Future<Void>> processSnapshotClassificationImagesInBatches(
      ExecutorService executor,
      List<SnapshotImage> images,
      String split,
      Map<Long, String> classIdToName,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      ZipOutputStream zos,
      List<String> writtenEntryPaths) {

    List<Future<Void>> futures = new ArrayList<>();

    for (int i = 0; i < images.size(); i += zipBatchSize) {
      int end = Math.min(i + zipBatchSize, images.size());
      List<SnapshotImage> batch = new ArrayList<>(images.subList(i, end));

      futures.add(
          executor.submit(
              () -> {
                processSnapshotClassificationBatch(
                    batch, split, classIdToName, labelsByImageId, zos, writtenEntryPaths);
                return null;
              }));
    }

    return futures;
  }

  /**
   * Process a batch of snapshot images for classification format. Uses batch database query for
   * image files.
   */
  private void processSnapshotClassificationBatch(
      List<SnapshotImage> batch,
      String split,
      Map<Long, String> classIdToName,
      Map<Long, List<SnapshotImageLabel>> labelsByImageId,
      ZipOutputStream zos,
      List<String> writtenEntryPaths)
      throws IOException {

    // Batch query image file data
    List<Long> fileIds =
        batch.stream()
            .map(SnapshotImage::getFileId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    if (fileIds.isEmpty()) {
      return;
    }

    // Batch fetch image files
    List<ImageFile> imageFiles = imageFileRepository.findAllById(fileIds);
    Map<Long, byte[]> imageDataMap =
        imageFiles.stream()
            .collect(Collectors.toMap(ImageFile::getId, ImageFile::getImageFileStream));

    for (SnapshotImage image : batch) {
      if (image.getFileId() == null) {
        continue;
      }

      byte[] imageData = imageDataMap.get(image.getFileId());
      if (imageData == null) {
        continue;
      }

      // Get the primary class for this image
      List<SnapshotImageLabel> labels =
          labelsByImageId.getOrDefault(image.getId(), Collections.emptyList());
      if (labels.isEmpty()) {
        continue;
      }

      SnapshotImageLabel primaryLabel = labels.get(0);
      if (primaryLabel.getClassId() == null) {
        continue;
      }

      String className = classIdToName.get(primaryLabel.getClassId());
      if (className == null) {
        continue;
      }

      // Sanitize class name for folder
      String safeClassName = className.replaceAll("[^a-zA-Z0-9_-]", "_");
      String imagePath = split + "/" + safeClassName + "/" + image.getFileName();

      // Write to ZIP (synchronized for thread safety)
      synchronized (zos) {
        zos.putNextEntry(new ZipEntry(imagePath));
        zos.write(imageData);
        zos.closeEntry();
      }
      writtenEntryPaths.add(imagePath);
    }
  }

  /** Wait for all futures to complete and handle exceptions. */
  private void waitForFutures(List<Future<Void>> futures) throws Exception {
    for (Future<Void> future : futures) {
      try {
        future.get();
      } catch (java.util.concurrent.ExecutionException e) {
        throw new IOException(
            "Batch processing failed: " + e.getCause().getMessage(), e.getCause());
      }
    }
  }

  /**
   * Create empty directory entries for YOLO classification dataset structure. This ensures
   * train/val/test folders with class subfolders exist even when there are no images.
   */
  private List<ZipEntryData> createEmptyClassificationDirectoryEntries(
      List<SnapshotProjectClass> classes) {
    List<ZipEntryData> entries = new ArrayList<>();
    String[] splits = {"train", "val", "test"};

    for (String split : splits) {
      // Add split directory
      entries.add(new ZipEntryData(split + "/", new byte[0]));

      // Add class subdirectories for each split
      for (SnapshotProjectClass cls : classes) {
        String safeClassName = cls.getClassName().replaceAll("[^a-zA-Z0-9_-]", "_");
        entries.add(new ZipEntryData(split + "/" + safeClassName + "/", new byte[0]));
      }
    }

    return entries;
  }

  /**
   * Process snapshot images for YOLO detection/segmentation format. Images go to:
   * dataset/images/{split}/ Labels go to: dataset/labels/{split}/
   */
  private List<ZipEntryData> processSnapshotImagesForYolo(
      List<SnapshotImage> images,
      Map<Long, Integer> classIdToIndex,
      String split,
      boolean isSegmentation,
      List<SnapshotImageLabel> allLabels) {

    List<ZipEntryData> entries = new ArrayList<>();

    // Create a map of image ID to labels for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));

    for (SnapshotImage image : images) {
      // Get image file
      if (image.getFileId() != null) {
        Optional<byte[]> imageData = imageFileRepository.findImageFileStreamById(image.getFileId());
        if (imageData.isPresent()) {
          // Image path: dataset/images/{split}/{filename}
          String imagePath = "dataset/images/" + split + "/" + image.getFileName();
          entries.add(new ZipEntryData(imagePath, imageData.get()));

          // Get labels for this image from the pre-loaded map
          List<SnapshotImageLabel> labels =
              labelsByImageId.getOrDefault(image.getId(), new ArrayList<>());
          String labelContent =
              generateYoloLabelContentFromSnapshot(
                  labels, classIdToIndex, image.getWidth(), image.getHeight(), isSegmentation);

          // Create label file (same name as image but .txt extension)
          // Label path: dataset/labels/{split}/{filename}.txt
          String labelFileName = image.getFileName().replaceAll("\\.[^.]+$", ".txt");
          String labelPath = "dataset/labels/" + split + "/" + labelFileName;
          entries.add(new ZipEntryData(labelPath, labelContent.getBytes(StandardCharsets.UTF_8)));
        }
      }
    }

    return entries;
  }

  /**
   * Generate YOLO label content from snapshot image labels. Format for detection: class_id center_x
   * center_y width height (normalized 0-1) Format for segmentation: class_id x1 y1 x2 y2 ... xn yn
   * (normalized polygon coordinates)
   */
  private String generateYoloLabelContentFromSnapshot(
      List<SnapshotImageLabel> labels,
      Map<Long, Integer> classIdToIndex,
      Integer imageWidth,
      Integer imageHeight,
      boolean isSegmentation) {

    StringBuilder content = new StringBuilder();

    for (SnapshotImageLabel label : labels) {
      if (label.getClassId() == null || label.getPosition() == null) {
        continue;
      }

      Integer classIndex = classIdToIndex.get(label.getClassId());
      if (classIndex == null) {
        continue;
      }

      try {
        // Parse position JSON
        @SuppressWarnings("unchecked")
        Map<String, Object> position = objectMapper.readValue(label.getPosition(), Map.class);

        if (isSegmentation) {
          // For segmentation, use polygon points if available
          content.append(generateSegmentationLabel(classIndex, position, imageWidth, imageHeight));
        } else {
          // For detection, use bounding box
          content.append(generateDetectionLabel(classIndex, position, imageWidth, imageHeight));
        }
      } catch (JsonProcessingException e) {
        log.warn("Failed to parse label position for snapshot label ID: {}", label.getId());
      }
    }

    return content.toString();
  }

  /** Process snapshot images for classification format (organized by class folders). */
  private List<ZipEntryData> processSnapshotImagesForClassification(
      List<SnapshotImage> images,
      List<SnapshotProjectClass> classes,
      String split,
      List<SnapshotImageLabel> allLabels) {

    List<ZipEntryData> entries = new ArrayList<>();
    Map<Long, String> classIdToName =
        classes.stream()
            .collect(
                Collectors.toMap(SnapshotProjectClass::getId, SnapshotProjectClass::getClassName));

    // Create a map of image ID to labels for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));

    for (SnapshotImage image : images) {
      if (image.getFileId() != null) {
        Optional<byte[]> imageData = imageFileRepository.findImageFileStreamById(image.getFileId());
        if (imageData.isPresent()) {
          // Get the primary class for this image from the pre-loaded map
          List<SnapshotImageLabel> labels =
              labelsByImageId.getOrDefault(image.getId(), new ArrayList<>());
          if (!labels.isEmpty()) {
            SnapshotImageLabel primaryLabel = labels.get(0);
            if (primaryLabel.getClassId() != null) {
              String className = classIdToName.get(primaryLabel.getClassId());
              if (className != null) {
                // Sanitize class name for folder
                String safeClassName = className.replaceAll("[^a-zA-Z0-9_-]", "_");
                String imagePath = split + "/" + safeClassName + "/" + image.getFileName();
                entries.add(new ZipEntryData(imagePath, imageData.get()));
              }
            }
          }
        }
      }
    }

    return entries;
  }

  /**
   * Generate data.yaml content for YOLO detection/segmentation. Format follows Ultralytics YOLO
   * standard: - path: dataset root directory (relative path for portability) - train/val/test:
   * relative paths to image directories - names: class ID to name mapping NOTE: No 'task' or 'nc'
   * fields - YOLO infers these automatically
   */
  private String generateDataYaml(String basePath, List<ProjectClass> classes, String task) {
    StringBuilder yaml = new StringBuilder();
    yaml.append("# YOLO Dataset Configuration\n");
    yaml.append("# Path will be configured by data scientist\n");
    yaml.append("path: dataset\n\n");
    yaml.append("# Dataset splits (relative to path)\n");
    yaml.append("train: images/train\n");
    yaml.append("val: images/val\n");
    yaml.append("test: images/test\n\n");
    yaml.append("# Class names (ID: name mapping)\n");
    yaml.append("names:\n");
    for (int i = 0; i < classes.size(); i++) {
      yaml.append("  ").append(i).append(": ").append(classes.get(i).getClassName()).append("\n");
    }
    return yaml.toString();
  }

  /**
   * Process images for YOLO detection/segmentation format. Images go to: dataset/images/{split}/
   * Labels go to: dataset/labels/{split}/
   */
  private List<ZipEntryData> processImagesForYolo(
      List<Image> images, Map<Long, Integer> classIdToIndex, String split, boolean isSegmentation) {

    List<ZipEntryData> entries = new ArrayList<>();

    for (Image image : images) {
      // Get image file
      if (image.getFileId() != null) {
        Optional<byte[]> imageData = imageFileRepository.findImageFileStreamById(image.getFileId());
        if (imageData.isPresent()) {
          // Image path: dataset/images/{split}/{filename}
          String imagePath = "dataset/images/" + split + "/" + image.getFileName();
          entries.add(new ZipEntryData(imagePath, imageData.get()));

          // Get labels for this image
          List<ImageLabel> labels = imageLabelRepository.findByImageId(image.getId());
          String labelContent =
              generateYoloLabelContent(
                  labels, classIdToIndex, image.getWidth(), image.getHeight(), isSegmentation);

          // Create label file (same name as image but .txt extension)
          // Label path: dataset/labels/{split}/{filename}.txt
          String labelFileName = image.getFileName().replaceAll("\\.[^.]+$", ".txt");
          String labelPath = "dataset/labels/" + split + "/" + labelFileName;
          entries.add(new ZipEntryData(labelPath, labelContent.getBytes(StandardCharsets.UTF_8)));
        }
      }
    }

    return entries;
  }

  /**
   * Generate YOLO label content from image labels. Format for detection: class_id center_x center_y
   * width height (normalized 0-1) Format for segmentation: class_id x1 y1 x2 y2 ... xn yn
   * (normalized polygon coordinates)
   */
  private String generateYoloLabelContent(
      List<ImageLabel> labels,
      Map<Long, Integer> classIdToIndex,
      Integer imageWidth,
      Integer imageHeight,
      boolean isSegmentation) {

    StringBuilder content = new StringBuilder();

    for (ImageLabel label : labels) {
      if (label.getProjectClass() == null || label.getPosition() == null) {
        continue;
      }

      Integer classIndex = classIdToIndex.get(label.getProjectClass().getId());
      if (classIndex == null) {
        continue;
      }

      try {
        // Parse position JSON
        @SuppressWarnings("unchecked")
        Map<String, Object> position = objectMapper.readValue(label.getPosition(), Map.class);

        if (isSegmentation) {
          // For segmentation, use polygon points if available
          content.append(generateSegmentationLabel(classIndex, position, imageWidth, imageHeight));
        } else {
          // For detection, use bounding box
          content.append(generateDetectionLabel(classIndex, position, imageWidth, imageHeight));
        }
      } catch (JsonProcessingException e) {
        log.warn("Failed to parse label position for label ID: {}", label.getId());
      }
    }

    return content.toString();
  }

  /** Generate detection label line in YOLO format. */
  private String generateDetectionLabel(
      Integer classIndex, Map<String, Object> position, Integer imageWidth, Integer imageHeight) {

    // Try to get bounding box coordinates
    Double x = getDoubleValue(position, "x");
    Double y = getDoubleValue(position, "y");
    Double width = getDoubleValue(position, "width");
    Double height = getDoubleValue(position, "height");

    if (x == null || y == null || width == null || height == null) {
      return "";
    }

    // Convert to YOLO format (center_x, center_y, width, height) normalized to 0-1
    double centerX = (x + width / 2.0) / imageWidth;
    double centerY = (y + height / 2.0) / imageHeight;
    double normWidth = width / imageWidth;
    double normHeight = height / imageHeight;

    return String.format(
        "%d %.6f %.6f %.6f %.6f\n", classIndex, centerX, centerY, normWidth, normHeight);
  }

  /** Generate segmentation label line in YOLO format. */
  @SuppressWarnings("unchecked")
  private String generateSegmentationLabel(
      Integer classIndex, Map<String, Object> position, Integer imageWidth, Integer imageHeight) {

    // Try to get polygon points
    Object pointsObj = position.get("points");
    if (pointsObj instanceof List) {
      List<Object> points = (List<Object>) pointsObj;
      StringBuilder sb = new StringBuilder();
      sb.append(classIndex);

      for (Object point : points) {
        if (point instanceof Map) {
          Map<String, Object> p = (Map<String, Object>) point;
          Double px = getDoubleValue(p, "x");
          Double py = getDoubleValue(p, "y");
          if (px != null && py != null) {
            sb.append(String.format(" %.6f %.6f", px / imageWidth, py / imageHeight));
          }
        } else if (point instanceof List) {
          List<Object> coords = (List<Object>) point;
          if (coords.size() >= 2) {
            Double px = ((Number) coords.get(0)).doubleValue();
            Double py = ((Number) coords.get(1)).doubleValue();
            sb.append(String.format(" %.6f %.6f", px / imageWidth, py / imageHeight));
          }
        }
      }
      sb.append("\n");
      return sb.toString();
    }

    // Fallback to bounding box if no polygon points
    return generateDetectionLabel(classIndex, position, imageWidth, imageHeight);
  }

  /** Process images for classification format (organized by class folders). */
  private List<ZipEntryData> processImagesForClassification(
      List<Image> images, List<ProjectClass> classes, String split) {

    List<ZipEntryData> entries = new ArrayList<>();
    Map<Long, String> classIdToName =
        classes.stream().collect(Collectors.toMap(ProjectClass::getId, ProjectClass::getClassName));

    for (Image image : images) {
      if (image.getFileId() != null) {
        Optional<byte[]> imageData = imageFileRepository.findImageFileStreamById(image.getFileId());
        if (imageData.isPresent()) {
          // Get the primary class for this image
          List<ImageLabel> labels = imageLabelRepository.findByImageId(image.getId());
          if (!labels.isEmpty()) {
            ImageLabel primaryLabel = labels.get(0);
            if (primaryLabel.getProjectClass() != null) {
              String className = classIdToName.get(primaryLabel.getProjectClass().getId());
              if (className != null) {
                // Sanitize class name for folder
                String safeClassName = className.replaceAll("[^a-zA-Z0-9_-]", "_");
                String imagePath = split + "/" + safeClassName + "/" + image.getFileName();
                entries.add(new ZipEntryData(imagePath, imageData.get()));
              }
            }
          }
        }
      }
    }

    return entries;
  }

  /**
   * Create zip files with size limit. First generates a complete zip file with the full folder
   * structure, then splits it into multiple part files by bytes if the size exceeds the limit. The
   * part files can be merged back using: - Windows: copy /b part1+part2+... dataset.zip - Linux:
   * cat part* > dataset.zip
   */
  private List<String> createZipFilesWithSizeLimit(
      Path baseDir, List<ZipEntryData> entries, String baseName) throws IOException {

    List<String> zipFilePaths = new ArrayList<>();

    // Step 1: Create the complete zip file first
    String completeZipPath = createZipFile(baseDir, entries, baseName, 0);
    File completeZipFile = new File(completeZipPath);
    long completeZipSize = completeZipFile.length();

    log.info(
        "Created complete zip file: {} with size: {} bytes, max size limit: {} bytes",
        completeZipPath,
        completeZipSize,
        yoloZipFileMaxSize);

    // Step 2: Check if the complete zip file exceeds the size limit
    if (completeZipSize <= yoloZipFileMaxSize) {
      // File is within limit, return as is
      zipFilePaths.add(completeZipPath);
      return zipFilePaths;
    }

    // Step 3: File exceeds limit, split by bytes into multiple part files
    log.info(
        "Complete zip file exceeds size limit, splitting into part files. Total size: {}, Limit: {}",
        completeZipSize,
        yoloZipFileMaxSize);

    zipFilePaths.addAll(splitZipFileByBytes(completeZipFile, baseDir, baseName));

    // Delete the original oversized zip file after splitting
    Files.deleteIfExists(completeZipFile.toPath());

    log.info("Split into {} part files", zipFilePaths.size());
    return zipFilePaths;
  }

  /**
   * Split a zip file into multiple part files by bytes. Part files are named:
   * baseName_part001.zip.part, baseName_part002.zip.part, etc. These can be merged back to restore
   * the original zip file.
   */
  private List<String> splitZipFileByBytes(File zipFile, Path baseDir, String baseName)
      throws IOException {

    List<String> partFilePaths = new ArrayList<>();
    long fileSize = zipFile.length();
    int totalParts = (int) Math.ceil((double) fileSize / yoloZipFileMaxSize);

    try (java.io.FileInputStream fis = new java.io.FileInputStream(zipFile)) {
      byte[] buffer = new byte[8192]; // 8KB buffer for reading
      int partNumber = 1;
      long bytesWrittenToPart = 0;
      FileOutputStream currentPartStream = null;
      String currentPartPath = null;

      try {
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
          // Start a new part file if needed
          if (currentPartStream == null || bytesWrittenToPart >= yoloZipFileMaxSize) {
            // Close previous part stream
            if (currentPartStream != null) {
              currentPartStream.close();
            }

            // Create new part file
            currentPartPath =
                baseDir
                    .resolve(String.format("%s_part%03d.zip.part", baseName, partNumber))
                    .toString();
            currentPartStream = new FileOutputStream(currentPartPath);
            partFilePaths.add(currentPartPath);
            partNumber++;
            bytesWrittenToPart = 0;
          }

          // Calculate how many bytes to write to current part
          long remainingInPart = yoloZipFileMaxSize - bytesWrittenToPart;
          int bytesToWrite = (int) Math.min(bytesRead, remainingInPart);

          currentPartStream.write(buffer, 0, bytesToWrite);
          bytesWrittenToPart += bytesToWrite;

          // If we have leftover bytes, they go to the next part
          if (bytesToWrite < bytesRead) {
            currentPartStream.close();

            currentPartPath =
                baseDir
                    .resolve(String.format("%s_part%03d.zip.part", baseName, partNumber))
                    .toString();
            currentPartStream = new FileOutputStream(currentPartPath);
            partFilePaths.add(currentPartPath);
            partNumber++;

            currentPartStream.write(buffer, bytesToWrite, bytesRead - bytesToWrite);
            bytesWrittenToPart = bytesRead - bytesToWrite;
          }
        }
      } finally {
        if (currentPartStream != null) {
          currentPartStream.close();
        }
      }
    }

    log.info(
        "Split zip file {} ({} bytes) into {} part files",
        zipFile.getName(),
        fileSize,
        partFilePaths.size());

    return partFilePaths;
  }

  /** Create a single zip file from entries. */
  private String createZipFile(Path baseDir, List<ZipEntryData> entries, String baseName, int index)
      throws IOException {

    String fileName = index == 0 ? baseName + ".zip" : baseName + "_" + index + ".zip";
    Path zipPath = baseDir.resolve(fileName);

    try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      for (ZipEntryData entry : entries) {
        ZipEntry zipEntry = new ZipEntry(entry.path);
        zos.putNextEntry(zipEntry);
        zos.write(entry.data);
        zos.closeEntry();
      }
    }

    log.info("Created zip file: {} with {} entries", zipPath, entries.size());
    return zipPath.toString();
  }

  /** Helper method to get Double value from map. */
  private Double getDoubleValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return null;
  }

  /**
   * Generate model_file_structure.json content recording all file paths and file names in the zip.
   * The output is a JSON array of objects, each with "path" (relative path inside zip) and
   * "fileName" (just the file name portion).
   *
   * @param entryPaths list of all zip entry paths (excluding directories)
   * @return JSON string with file structure
   */
  private String generateFileStructureJson(List<String> entryPaths) {
    try {
      List<Map<String, String>> fileEntries = new ArrayList<>();

      for (String entryPath : entryPaths) {
        String fileName = Paths.get(entryPath).getFileName().toString();

        Map<String, String> fileInfo = new java.util.LinkedHashMap<>();
        fileInfo.put("path", entryPath);
        fileInfo.put("fileName", fileName);
        fileEntries.add(fileInfo);
      }

      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileEntries);
    } catch (JsonProcessingException e) {
      log.error("Failed to generate file structure JSON", e);
      throw new TrainingException("Failed to generate file structure", e);
    }
  }

  /** Inner class to hold zip entry data. */
  private static class ZipEntryData {
    final String path;
    final byte[] data;

    ZipEntryData(String path, byte[] data) {
      this.path = path;
      this.data = data;
    }
  }
}
