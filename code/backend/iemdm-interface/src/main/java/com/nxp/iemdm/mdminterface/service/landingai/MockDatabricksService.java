package com.nxp.iemdm.mdminterface.service.landingai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.MockConfig;
import com.nxp.iemdm.mdminterface.dto.request.*;
import com.nxp.iemdm.mdminterface.dto.response.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Mock implementation of DatabricksService for development and testing. Generates 6 output files
 * matching Databricks YOLO training output: - results.csv (epoch metrics) - args.yaml (training
 * hyperparameters) - objectdetection_predictions_metrics.json (aggregated metrics) -
 * YOLO_ObjectDetection_train_predictions.json - YOLO_ObjectDetection_val_predictions.json -
 * YOLO_ObjectDetection_test_predictions.json
 */
@Service
@ConditionalOnProperty(name = "databricks.mode", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockDatabricksService implements DatabricksService {

  private final Random random = new Random();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockConfig mockConfig;

  private static final String TEMP_SUBDIR = "temp";
  private static final String RESULTS_SUBDIR = "results";

  // Default image size for pixel coordinate calculation
  private static final int DEFAULT_IMG_WIDTH = 640;
  private static final int DEFAULT_IMG_HEIGHT = 480;

  @Override
  public TrainingDataResponse submitTraining(TrainingDataRequest request) {
    log.info(
        "[MOCK] Submitting training - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      String firstZipFilename = request.getFirstZipFilename();
      if (firstZipFilename == null) {
        log.error("[MOCK] No zip files provided");
        return new TrainingDataResponse("No zip files provided", request.getTrackId());
      }

      String zipFullPath = request.getZipPath() + firstZipFilename;
      Path zipFile = Paths.get(zipFullPath);

      if (!Files.exists(zipFile)) {
        log.error("[MOCK] Zip file not found: {}", zipFullPath);
        return new TrainingDataResponse("Zip file not found: " + zipFullPath, request.getTrackId());
      }

      // Create temp directory
      Path tempBaseDir = Paths.get(mockConfig.getBaseDir(), TEMP_SUBDIR);
      Files.createDirectories(tempBaseDir);
      Path tempDir = tempBaseDir.resolve(request.getTrackId() + "_" + System.currentTimeMillis());
      Files.createDirectories(tempDir);
      log.info("[MOCK] Extracting zip to: {}", tempDir.toAbsolutePath());

      unzip(zipFile, tempDir);

      Path datasetRoot = findDatasetRoot(tempDir);
      log.info("[MOCK] Dataset root detected at: {}", datasetRoot.toAbsolutePath());

      // Read class names from data.yaml or model_metadata.json
      List<String> classNames = readClassNames(datasetRoot);
      log.info("[MOCK] Classes: {}", classNames);

      // Create results directory
      Path resultsDir = Paths.get(mockConfig.getBaseDir(), RESULTS_SUBDIR, request.getTrackId());
      Files.createDirectories(resultsDir);

      // Process each split and generate prediction files
      Map<String, List<Map<String, Object>>> splitPredictions = new LinkedHashMap<>();
      String[] splits = {"train", "val", "test"};

      int totalImages = 0;
      int totalCorrectPredictions = 0;
      List<Double> allPrecisions = new ArrayList<>();
      List<Double> allRecalls = new ArrayList<>();

      for (String split : splits) {
        List<Map<String, Object>> predictions = processDatasetSplit(datasetRoot, split, classNames);
        splitPredictions.put(split, predictions);
        totalImages += predictions.size();

        // Calculate metrics from predictions
        for (Map<String, Object> pred : predictions) {
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> preds = (List<Map<String, Object>>) pred.get("predictions");
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> gts = (List<Map<String, Object>>) pred.get("ground_truth");
          if (!preds.isEmpty() && !gts.isEmpty()) {
            totalCorrectPredictions++;
          }
        }
      }

      // 1. Generate results.csv
      int epochs = 10 + random.nextInt(11); // 10-20 epochs
      generateResultsCsv(resultsDir, epochs, classNames.size());
      log.info("[MOCK] Generated results.csv with {} epochs", epochs);

      // 2. Generate args.yaml
      generateArgsYaml(resultsDir, epochs, classNames.size());
      log.info("[MOCK] Generated args.yaml");

      // 3. Generate objectdetection_predictions_metrics.json
      generatePredictionMetrics(resultsDir, classNames.size(), splitPredictions);
      log.info("[MOCK] Generated objectdetection_predictions_metrics.json");

      // 4-6. Generate split prediction files
      for (String split : splits) {
        String filename = "YOLO_ObjectDetection_" + split + "_predictions.json";
        Path predFile = resultsDir.resolve(filename);
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(predFile.toFile(), splitPredictions.get(split));
        log.info(
            "[MOCK] Generated {} with {} images", filename, splitPredictions.get(split).size());
      }

      // Cleanup temp directory
      if (mockConfig.isCleanupTemp()) {
        deleteDirectory(tempDir);
        log.info("[MOCK] Cleaned up temp directory");
      }

      // Generate a mock runId (similar to real Databricks run IDs)
      Long mockRunId = System.currentTimeMillis() * 1000 + random.nextInt(1000);
      log.info("[MOCK] Generated mock runId: {}", mockRunId);

      TrainingDataResponse response = new TrainingDataResponse("", request.getTrackId());
      response.setRunId(mockRunId);
      return response;

    } catch (Exception e) {
      log.error("[MOCK] Error processing training request", e);
      return new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
    }
  }

  /** Process a dataset split (train/val/test) and generate predictions. */
  private List<Map<String, Object>> processDatasetSplit(
      Path datasetRoot, String split, List<String> classNames) throws IOException {
    List<Map<String, Object>> results = new ArrayList<>();

    Path labelsDir = datasetRoot.resolve("labels").resolve(split);
    Path imagesDir = datasetRoot.resolve("images").resolve(split);

    if (!Files.exists(labelsDir) || !Files.isDirectory(labelsDir)) {
      log.info("[MOCK] No labels directory for {} split", split);
      return results;
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(labelsDir, "*.txt")) {
      for (Path labelFile : stream) {
        String baseName = labelFile.getFileName().toString().replace(".txt", "");
        String imageName = findImageFile(imagesDir, baseName);

        // Read ground truth from label file
        List<Map<String, Object>> groundTruths = new ArrayList<>();
        List<Map<String, Object>> predictions = new ArrayList<>();
        List<String> lines = Files.readAllLines(labelFile);

        for (String line : lines) {
          String[] parts = line.trim().split("\\s+");
          if (parts.length >= 5) {
            int classId = Integer.parseInt(parts[0]);
            double xcenter = Double.parseDouble(parts[1]);
            double ycenter = Double.parseDouble(parts[2]);
            double width = Double.parseDouble(parts[3]);
            double height = Double.parseDouble(parts[4]);

            String className =
                classId < classNames.size() ? classNames.get(classId) : "class_" + classId;

            // Ground truth entry
            Map<String, Object> gt = new LinkedHashMap<>();
            gt.put("class_id", classId);
            gt.put("class_name", className);
            gt.put(
                "bbox_pixel",
                Arrays.asList(
                    xcenter * DEFAULT_IMG_WIDTH,
                    ycenter * DEFAULT_IMG_HEIGHT,
                    width * DEFAULT_IMG_WIDTH,
                    height * DEFAULT_IMG_HEIGHT));
            gt.put("bbox_normalized", Arrays.asList(xcenter, ycenter, width, height));
            groundTruths.add(gt);

            // Generate prediction based on ground truth with slight variation
            Map<String, Object> pred = new LinkedHashMap<>();
            pred.put("class_id", classId); // Same class as ground truth
            pred.put("class_name", className);
            pred.put("confidence", 0.70 + random.nextDouble() * 0.29); // 0.70-0.99

            // Apply ±5% variation to bbox
            double predXcenter = applyVariation(xcenter, 0.05);
            double predYcenter = applyVariation(ycenter, 0.05);
            double predWidth = applyVariation(width, 0.05);
            double predHeight = applyVariation(height, 0.05);

            pred.put(
                "bbox_pixel",
                Arrays.asList(
                    predXcenter * DEFAULT_IMG_WIDTH,
                    predYcenter * DEFAULT_IMG_HEIGHT,
                    predWidth * DEFAULT_IMG_WIDTH,
                    predHeight * DEFAULT_IMG_HEIGHT));
            pred.put(
                "bbox_normalized", Arrays.asList(predXcenter, predYcenter, predWidth, predHeight));
            predictions.add(pred);
          }
        }

        if (!groundTruths.isEmpty()) {
          Map<String, Object> imageResult = new LinkedHashMap<>();
          imageResult.put("image", imageName);
          imageResult.put("predictions", predictions);
          imageResult.put("ground_truth", groundTruths);

          // Calculate IoUs between predictions and ground truths
          List<Double> ious = calculateIoUs(predictions, groundTruths);
          imageResult.put("ious", ious);

          // Threshold results
          Map<String, Double> thresholdResults = new LinkedHashMap<>();
          double maxIoU = ious.isEmpty() ? 0.0 : Collections.max(ious);
          for (double thresh = 0.50; thresh <= 0.95; thresh += 0.05) {
            thresholdResults.put(String.format("IoU@%.2f", thresh), maxIoU >= thresh ? 1.0 : 0.0);
          }
          imageResult.put("threshold_results", thresholdResults);

          // mAP for this image
          double mapValue = 0.70 + random.nextDouble() * 0.25; // 0.70-0.95
          imageResult.put("mAP@0.5:0.95", mapValue);

          // YOLO decision (highest confidence prediction)
          if (!predictions.isEmpty()) {
            Map<String, Object> bestPred =
                predictions.stream()
                    .max(Comparator.comparingDouble(p -> (Double) p.get("confidence")))
                    .orElse(predictions.get(0));

            Map<String, Object> yoloDecision = new LinkedHashMap<>();
            yoloDecision.put("main_class_id", bestPred.get("class_id"));
            yoloDecision.put("main_class_name", bestPred.get("class_name"));
            yoloDecision.put("main_confidence", bestPred.get("confidence"));
            yoloDecision.put("bbox_pixel", bestPred.get("bbox_pixel"));
            yoloDecision.put("bbox_normalized", bestPred.get("bbox_normalized"));
            imageResult.put("yolo_decision", yoloDecision);
          }

          results.add(imageResult);
        }
      }
    }

    log.info("[MOCK] Processed {} images from {} split", results.size(), split);
    return results;
  }

  /** Calculate IoU values between predictions and ground truths. */
  private List<Double> calculateIoUs(
      List<Map<String, Object>> predictions, List<Map<String, Object>> groundTruths) {
    List<Double> ious = new ArrayList<>();
    for (Map<String, Object> pred : predictions) {
      for (Map<String, Object> gt : groundTruths) {
        @SuppressWarnings("unchecked")
        List<Double> predBbox = (List<Double>) pred.get("bbox_normalized");
        @SuppressWarnings("unchecked")
        List<Double> gtBbox = (List<Double>) gt.get("bbox_normalized");

        // Calculate IoU (simplified - using center format)
        double iou = calculateBoxIoU(predBbox, gtBbox);
        ious.add(iou);
      }
    }
    return ious;
  }

  /** Calculate IoU between two boxes in [xcenter, ycenter, width, height] format. */
  private double calculateBoxIoU(List<Double> box1, List<Double> box2) {
    // Convert center format to corner format
    double x1_min = box1.get(0) - box1.get(2) / 2;
    double y1_min = box1.get(1) - box1.get(3) / 2;
    double x1_max = box1.get(0) + box1.get(2) / 2;
    double y1_max = box1.get(1) + box1.get(3) / 2;

    double x2_min = box2.get(0) - box2.get(2) / 2;
    double y2_min = box2.get(1) - box2.get(3) / 2;
    double x2_max = box2.get(0) + box2.get(2) / 2;
    double y2_max = box2.get(1) + box2.get(3) / 2;

    // Calculate intersection
    double inter_x_min = Math.max(x1_min, x2_min);
    double inter_y_min = Math.max(y1_min, y2_min);
    double inter_x_max = Math.min(x1_max, x2_max);
    double inter_y_max = Math.min(y1_max, y2_max);

    double inter_width = Math.max(0, inter_x_max - inter_x_min);
    double inter_height = Math.max(0, inter_y_max - inter_y_min);
    double inter_area = inter_width * inter_height;

    // Calculate union
    double area1 = box1.get(2) * box1.get(3);
    double area2 = box2.get(2) * box2.get(3);
    double union_area = area1 + area2 - inter_area;

    return union_area > 0 ? inter_area / union_area : 0.0;
  }

  /** Generate results.csv with epoch-by-epoch training metrics. */
  private void generateResultsCsv(Path resultsDir, int epochs, int numClasses) throws IOException {
    Path csvFile = resultsDir.resolve("results.csv");

    StringBuilder csv = new StringBuilder();
    // Header matching YOLO output
    csv.append("epoch,train/box_loss,train/cls_loss,train/dfl_loss,metrics/precision(B),");
    csv.append(
        "metrics/recall(B),metrics/mAP50(B),metrics/mAP50-95(B),val/box_loss,val/cls_loss,val/dfl_loss,lr/pg0,lr/pg1,lr/pg2\n");

    // Generate decreasing loss and increasing metrics over epochs
    for (int epoch = 1; epoch <= epochs; epoch++) {
      double progress = (double) epoch / epochs;

      // Losses decrease over time
      double trainBoxLoss = 1.5 - (1.2 * progress) + randomVariation(0.1);
      double trainClsLoss = 2.0 - (1.5 * progress) + randomVariation(0.1);
      double trainDflLoss = 1.2 - (0.8 * progress) + randomVariation(0.05);
      double valBoxLoss = 1.6 - (1.1 * progress) + randomVariation(0.1);
      double valClsLoss = 2.1 - (1.4 * progress) + randomVariation(0.1);
      double valDflLoss = 1.3 - (0.7 * progress) + randomVariation(0.05);

      // Metrics increase over time
      double precision = 0.5 + (0.4 * progress) + randomVariation(0.05);
      double recall = 0.4 + (0.45 * progress) + randomVariation(0.05);
      double map50 = 0.5 + (0.4 * progress) + randomVariation(0.05);
      double map5095 = 0.3 + (0.5 * progress) + randomVariation(0.05);

      // Learning rate decreases
      double lr = 0.01 * (1 - 0.9 * progress);

      csv.append(
          String.format(
              "%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.6f,%.6f,%.6f\n",
              epoch,
              Math.max(0.1, trainBoxLoss),
              Math.max(0.1, trainClsLoss),
              Math.max(0.1, trainDflLoss),
              Math.min(0.99, Math.max(0.1, precision)),
              Math.min(0.99, Math.max(0.1, recall)),
              Math.min(0.99, Math.max(0.1, map50)),
              Math.min(0.99, Math.max(0.1, map5095)),
              Math.max(0.1, valBoxLoss),
              Math.max(0.1, valClsLoss),
              Math.max(0.1, valDflLoss),
              lr,
              lr,
              lr));
    }

    Files.writeString(csvFile, csv.toString());
  }

  /** Generate args.yaml with training hyperparameters. */
  private void generateArgsYaml(Path resultsDir, int epochs, int numClasses) throws IOException {
    Path yamlFile = resultsDir.resolve("args.yaml");

    Map<String, Object> args = new LinkedHashMap<>();
    args.put("task", "detect");
    args.put("mode", "train");
    args.put("model", "yolov8n.yaml");
    args.put("data", "data.yaml");
    args.put("epochs", epochs);
    args.put("time", null);
    args.put("patience", 100);
    args.put("batch", 16);
    args.put("imgsz", 640);
    args.put("save", true);
    args.put("save_period", -1);
    args.put("cache", false);
    args.put("device", null);
    args.put("workers", 8);
    args.put("project", null);
    args.put("name", "train");
    args.put("exist_ok", false);
    args.put("pretrained", "yolov8n.pt");
    args.put("optimizer", "auto");
    args.put("verbose", true);
    args.put("seed", 0);
    args.put("deterministic", true);
    args.put("single_cls", false);
    args.put("rect", false);
    args.put("cos_lr", false);
    args.put("close_mosaic", 10);
    args.put("resume", false);
    args.put("amp", true);
    args.put("fraction", 1.0);
    args.put("profile", false);
    args.put("freeze", null);
    args.put("multi_scale", 0.0);
    args.put("overlap_mask", true);
    args.put("mask_ratio", 4);
    args.put("dropout", 0.0);
    args.put("val", true);
    args.put("split", "val");
    args.put("save_json", false);
    args.put("conf", null);
    args.put("iou", 0.7);
    args.put("max_det", 300);
    args.put("half", false);
    args.put("dnn", false);
    args.put("plots", true);
    args.put("source", null);
    args.put("vid_stride", 1);
    args.put("stream_buffer", false);
    args.put("visualize", false);
    args.put("augment", false);
    args.put("agnostic_nms", false);
    args.put("classes", null);
    args.put("retina_masks", false);
    args.put("embed", null);
    args.put("show", false);
    args.put("save_frames", false);
    args.put("save_txt", false);
    args.put("save_conf", false);
    args.put("save_crop", false);
    args.put("show_labels", true);
    args.put("show_conf", true);
    args.put("show_boxes", true);
    args.put("line_width", null);
    args.put("format", "torchscript");
    args.put("keras", false);
    args.put("optimize", false);
    args.put("int8", false);
    args.put("dynamic", false);
    args.put("simplify", true);
    args.put("opset", null);
    args.put("workspace", null);
    args.put("nms", false);
    args.put("lr0", 0.01);
    args.put("lrf", 0.01);
    args.put("momentum", 0.937);
    args.put("weight_decay", 0.0005);
    args.put("warmup_epochs", 3.0);
    args.put("warmup_momentum", 0.8);
    args.put("warmup_bias_lr", 0.1);
    args.put("box", 7.5);
    args.put("cls", 0.5);
    args.put("dfl", 1.5);
    args.put("pose", 12.0);
    args.put("kobj", 1.0);
    args.put("nbs", 64);
    args.put("hsv_h", 0.015);
    args.put("hsv_s", 0.7);
    args.put("hsv_v", 0.4);
    args.put("degrees", 0.0);
    args.put("translate", 0.1);
    args.put("scale", 0.5);
    args.put("shear", 0.0);
    args.put("perspective", 0.0);
    args.put("flipud", 0.0);
    args.put("fliplr", 0.5);
    args.put("bgr", 0.0);
    args.put("mosaic", 1.0);
    args.put("mixup", 0.0);
    args.put("copy_paste", 0.0);
    args.put("copy_paste_mode", "flip");
    args.put("auto_augment", "randaugment");
    args.put("erasing", 0.4);
    args.put("cfg", null);
    args.put("tracker", "botsort.yaml");
    args.put("save_dir", resultsDir.toAbsolutePath().toString());

    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    Yaml yaml = new Yaml(options);

    try (FileWriter writer = new FileWriter(yamlFile.toFile())) {
      yaml.dump(args, writer);
    }
  }

  /** Generate objectdetection_predictions_metrics.json with aggregated metrics. */
  private void generatePredictionMetrics(
      Path resultsDir, int numClasses, Map<String, List<Map<String, Object>>> splitPredictions)
      throws IOException {

    Path metricsFile = resultsDir.resolve("objectdetection_predictions_metrics.json");

    Map<String, Object> metrics = new LinkedHashMap<>();

    // Per-class metrics (generate reasonable values)
    List<Double> precisionPerClass = new ArrayList<>();
    List<Double> recallPerClass = new ArrayList<>();
    List<Double> f1PerClass = new ArrayList<>();
    List<Double> map50PerClass = new ArrayList<>();
    List<Double> mapPerClass = new ArrayList<>();

    for (int i = 0; i < numClasses; i++) {
      double precision = 0.6 + random.nextDouble() * 0.4; // 0.6-1.0
      double recall = 0.5 + random.nextDouble() * 0.5; // 0.5-1.0
      double f1 = 2 * precision * recall / (precision + recall + 0.0001);
      double map50 = 0.6 + random.nextDouble() * 0.39; // 0.6-0.99
      double map = 0.4 + random.nextDouble() * 0.5; // 0.4-0.9

      precisionPerClass.add(Math.round(precision * 10000.0) / 10000.0);
      recallPerClass.add(Math.round(recall * 10000.0) / 10000.0);
      f1PerClass.add(Math.round(f1 * 10000.0) / 10000.0);
      map50PerClass.add(Math.round(map50 * 10000.0) / 10000.0);
      mapPerClass.add(Math.round(map * 10000.0) / 10000.0);
    }

    metrics.put("precision_per_class", precisionPerClass);
    metrics.put("recall_per_class", recallPerClass);
    metrics.put("f1_per_class", f1PerClass);
    metrics.put("map50_per_class", map50PerClass);
    metrics.put("map_per_class", mapPerClass);

    // Mean metrics
    metrics.put("precision_mean", calculateMean(precisionPerClass));
    metrics.put("recall_mean", calculateMean(recallPerClass));
    metrics.put("f1_mean", calculateMean(f1PerClass));
    metrics.put("map50", calculateMean(map50PerClass));
    metrics.put("map50_95", calculateMean(mapPerClass));

    // Confusion matrix (numClasses+1 x numClasses+1 for background class)
    // Using 80 classes like COCO for compatibility
    int matrixSize = 81;
    List<List<Double>> confusionMatrix = new ArrayList<>();
    for (int i = 0; i < matrixSize; i++) {
      List<Double> row = new ArrayList<>();
      for (int j = 0; j < matrixSize; j++) {
        if (i < numClasses && i == j) {
          // Diagonal - correct predictions
          row.add((double) (1 + random.nextInt(5)));
        } else {
          row.add(0.0);
        }
      }
      confusionMatrix.add(row);
    }
    metrics.put("confusion_matrix", confusionMatrix);

    objectMapper.writerWithDefaultPrettyPrinter().writeValue(metricsFile.toFile(), metrics);
  }

  private double calculateMean(List<Double> values) {
    if (values.isEmpty()) return 0.0;
    double sum = values.stream().mapToDouble(Double::doubleValue).sum();
    return Math.round((sum / values.size()) * 10000.0) / 10000.0;
  }

  private double randomVariation(double maxVariation) {
    return (random.nextDouble() * 2 - 1) * maxVariation;
  }

  // ============ Existing methods (getTrainingResults, testModel, etc.) ============

  // ==========================================================================
  // DEPRECATED: Old method that returns parsed JSON data directly.
  // Replaced by getTrainingResultsAsFilePaths() which returns file paths.
  // Kept commented out for reference. Remove once confirmed no longer needed.
  // ==========================================================================
  // @Override
  // public TrainingResultsResponse getTrainingResults(TrainingResultsRequest request) {
  //   ... (full mock implementation removed for clarity)
  //   Reads from: results dir -> objectdetection_predictions_metrics.json, results.csv,
  //               YOLO_ObjectDetection_val_predictions.json
  //   Returns: TrainingResultsResponse with metrics, charts, and prediction images
  // }

  @Override
  public TrainingResultsFilePathsResponse getTrainingResultsAsFilePaths(
      TrainingResultsRequest request) {
    log.info(
        "[MOCK] Retrieving training results as file paths for trackId: {}", request.getTrackId());

    Path resultsDir = Paths.get(mockConfig.getBaseDir(), RESULTS_SUBDIR, request.getTrackId());

    if (!Files.exists(resultsDir)) {
      return TrainingResultsFilePathsResponse.builder()
          .trackId(request.getTrackId())
          .status("ERROR")
          .error("Training results not found for trackId: " + request.getTrackId())
          .build();
    }

    // Build response with all 6 file paths
    return TrainingResultsFilePathsResponse.builder()
        .trackId(request.getTrackId())
        .results(resultsDir.resolve("results.csv").toString())
        .args(resultsDir.resolve("args.yaml").toString())
        .predictionMetrics(
            resultsDir.resolve("objectdetection_predictions_metrics.json").toString())
        .trainPrediction(
            resultsDir.resolve("YOLO_ObjectDetection_train_predictions.json").toString())
        .valPrediction(resultsDir.resolve("YOLO_ObjectDetection_val_predictions.json").toString())
        .testPrediction(resultsDir.resolve("YOLO_ObjectDetection_test_predictions.json").toString())
        .status("SUCCESS")
        .build();
  }

  @Override
  public TrainingDataResponse testModel(ModelTestRequest request) {
    log.info(
        "[MOCK] Testing model - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      String firstZipFilename = request.getFirstZipFilename();
      if (firstZipFilename == null) {
        return new TrainingDataResponse("No zip files provided", request.getTrackId());
      }

      String zipFullPath = request.getZipPath() + firstZipFilename;
      Path zipFile = Paths.get(zipFullPath);

      if (!Files.exists(zipFile)) {
        return new TrainingDataResponse("Zip file not found: " + zipFullPath, request.getTrackId());
      }

      Path tempBaseDir = Paths.get(mockConfig.getBaseDir(), TEMP_SUBDIR);
      Files.createDirectories(tempBaseDir);
      Path tempDir =
          tempBaseDir.resolve("test_" + request.getTrackId() + "_" + System.currentTimeMillis());
      Files.createDirectories(tempDir);

      unzip(zipFile, tempDir);
      Path datasetRoot = findDatasetRoot(tempDir);
      List<String> classNames = readClassNames(datasetRoot);
      int numClasses = classNames.isEmpty() ? 3 : classNames.size();

      // Create results directory for test model output
      Path resultsDir =
          Paths.get(mockConfig.getBaseDir(), RESULTS_SUBDIR, "test_" + request.getTrackId());
      Files.createDirectories(resultsDir);

      // Find all images and generate predictions (no ground truth for test)
      List<String> imageFiles = findAllImages(datasetRoot);
      List<Map<String, Object>> testPredictions = new ArrayList<>();

      for (String imageName : imageFiles) {
        Map<String, Object> imageResult = new LinkedHashMap<>();
        imageResult.put("image", imageName);

        List<Map<String, Object>> predictions = new ArrayList<>();
        int numDetections = 1 + random.nextInt(5);

        for (int i = 0; i < numDetections; i++) {
          int classId = random.nextInt(numClasses);
          String className =
              classId < classNames.size() ? classNames.get(classId) : "class_" + classId;
          double confidence = 0.70 + random.nextDouble() * 0.29;
          double xcenter = 0.1 + random.nextDouble() * 0.8;
          double ycenter = 0.1 + random.nextDouble() * 0.8;
          double width = 0.05 + random.nextDouble() * 0.3;
          double height = 0.05 + random.nextDouble() * 0.3;

          Map<String, Object> pred = new LinkedHashMap<>();
          pred.put("class_id", classId);
          pred.put("class_name", className);
          pred.put("confidence", Math.round(confidence * 10000.0) / 10000.0);
          pred.put(
              "bbox_pixel",
              Arrays.asList(
                  xcenter * DEFAULT_IMG_WIDTH,
                  ycenter * DEFAULT_IMG_HEIGHT,
                  width * DEFAULT_IMG_WIDTH,
                  height * DEFAULT_IMG_HEIGHT));
          pred.put(
              "bbox_normalized",
              Arrays.asList(
                  Math.round(xcenter * 10000.0) / 10000.0,
                  Math.round(ycenter * 10000.0) / 10000.0,
                  Math.round(width * 10000.0) / 10000.0,
                  Math.round(height * 10000.0) / 10000.0));
          predictions.add(pred);
        }

        imageResult.put("predictions", predictions);
        // No ground_truth for test images

        // YOLO decision (highest confidence prediction)
        if (!predictions.isEmpty()) {
          Map<String, Object> bestPred =
              predictions.stream()
                  .max(Comparator.comparingDouble(p -> (Double) p.get("confidence")))
                  .orElse(predictions.get(0));

          Map<String, Object> yoloDecision = new LinkedHashMap<>();
          yoloDecision.put("main_class_id", bestPred.get("class_id"));
          yoloDecision.put("main_class_name", bestPred.get("class_name"));
          yoloDecision.put("main_confidence", bestPred.get("confidence"));
          yoloDecision.put("bbox_pixel", bestPred.get("bbox_pixel"));
          yoloDecision.put("bbox_normalized", bestPred.get("bbox_normalized"));
          imageResult.put("yolo_decision", yoloDecision);
        }

        testPredictions.add(imageResult);
      }

      // 1. Generate results.csv (simulated training metrics from original model)
      int epochs = 10 + random.nextInt(11);
      generateResultsCsv(resultsDir, epochs, numClasses);
      log.info("[MOCK] Generated results.csv");

      // 2. Generate args.yaml
      generateArgsYaml(resultsDir, epochs, numClasses);
      log.info("[MOCK] Generated args.yaml");

      // 3. Generate objectdetection_predictions_metrics.json
      Map<String, List<Map<String, Object>>> splitPredictions = new LinkedHashMap<>();
      splitPredictions.put("test", testPredictions);
      generatePredictionMetrics(resultsDir, numClasses, splitPredictions);
      log.info("[MOCK] Generated objectdetection_predictions_metrics.json");

      // 4-6. Generate prediction files (only test has data, train/val are empty for test model)
      objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValue(
              resultsDir.resolve("YOLO_ObjectDetection_train_predictions.json").toFile(),
              new ArrayList<>());
      objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValue(
              resultsDir.resolve("YOLO_ObjectDetection_val_predictions.json").toFile(),
              new ArrayList<>());
      objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValue(
              resultsDir.resolve("YOLO_ObjectDetection_test_predictions.json").toFile(),
              testPredictions);
      log.info("[MOCK] Generated prediction files with {} test images", testPredictions.size());

      // Cleanup temp
      if (mockConfig.isCleanupTemp()) {
        deleteDirectory(tempDir);
      }

      // Generate a mock runId (similar to real Databricks run IDs)
      Long mockRunId = System.currentTimeMillis() * 1000 + random.nextInt(1000);
      log.info("[MOCK] Generated mock runId: {}", mockRunId);

      TrainingDataResponse response = new TrainingDataResponse("", request.getTrackId());
      response.setRunId(mockRunId);
      return response;

    } catch (Exception e) {
      log.error("[MOCK] Error testing model", e);
      return new TrainingDataResponse("Error: " + e.getMessage(), request.getTrackId());
    }
  }

  @Override
  public DownloadModelResponse downloadModel(DownloadModelRequest request) {
    log.info(
        "[MOCK] Downloading model: {} version: {}", request.getModelName(), request.getVersion());

    String mockUrl =
        String.format(
            "https://mock-databricks.example.com/files/models/%s/v%d/model_%s.zip",
            request.getModelName(),
            request.getVersion(),
            UUID.randomUUID().toString().substring(0, 8));

    DownloadModelResponse.ArtifactInfo artifact = new DownloadModelResponse.ArtifactInfo(mockUrl);
    return new DownloadModelResponse(
        request.getModelName(),
        request.getVersion(),
        request.getTrackId() != null ? request.getTrackId().toString() : "",
        artifact);
  }

  @Override
  public TrainingResultsFilePathsResponse getTestModelResultsAsFilePaths(
      TrainingResultsRequest request) {
    String trackId = request.getTrackId();
    log.info("[MOCK] Retrieving test model results as file paths for trackId: {}", trackId);

    Path resultsDir = Paths.get(mockConfig.getBaseDir(), RESULTS_SUBDIR, "test_" + trackId);

    if (!Files.exists(resultsDir)) {
      return TrainingResultsFilePathsResponse.builder()
          .trackId(trackId)
          .status("ERROR")
          .error("Test model results not found for trackId: " + trackId)
          .build();
    }

    // Build response with all 6 file paths
    return TrainingResultsFilePathsResponse.builder()
        .trackId(trackId)
        .results(resultsDir.resolve("results.csv").toString())
        .args(resultsDir.resolve("args.yaml").toString())
        .predictionMetrics(
            resultsDir.resolve("objectdetection_predictions_metrics.json").toString())
        .trainPrediction(
            resultsDir.resolve("YOLO_ObjectDetection_train_predictions.json").toString())
        .valPrediction(resultsDir.resolve("YOLO_ObjectDetection_val_predictions.json").toString())
        .testPrediction(resultsDir.resolve("YOLO_ObjectDetection_test_predictions.json").toString())
        .status("SUCCESS")
        .build();
  }

  @Override
  public DeleteModelResponse deleteModel(DeleteModelRequest request) {
    log.info(
        "[MOCK] Deleting model: {} version: {}", request.getModelFullName(), request.getVersion());

    DeleteModelResponse.ModelInfo modelInfo =
        new DeleteModelResponse.ModelInfo(
            request.getModelFullName(), request.getVersion(), request.getTrackId());
    return new DeleteModelResponse("DELETED", "Model removed successfully.", modelInfo);
  }

  @Override
  public String readFileContent(String filePath) {
    log.info("[MOCK] Reading file content from: {}", filePath);

    try {
      // Convert relative path to absolute path
      Path path = Paths.get(filePath);

      // If relative path, resolve from current working directory
      if (!path.isAbsolute()) {
        path = Paths.get(System.getProperty("user.dir"), filePath);
      }

      log.debug("[MOCK] Resolved absolute path: {}", path.toAbsolutePath());

      // Check if file exists
      if (!Files.exists(path)) {
        log.error("[MOCK] File not found: {}", path.toAbsolutePath());
        throw new IOException("File not found: " + filePath);
      }

      // Read file content
      String content = Files.readString(path);
      log.info("[MOCK] Successfully read file, size: {} bytes", content.length());

      return content;

    } catch (IOException e) {
      log.error("[MOCK] Error reading file {}: {}", filePath, e.getMessage(), e);
      throw new RuntimeException("Failed to read file: " + filePath, e);
    }
  }

  // ============ Helper Methods ============

  private List<String> readClassNames(Path datasetRoot) throws IOException {
    List<String> classNames = new ArrayList<>();

    // Try data.yaml first
    Path dataYamlPath = datasetRoot.resolve("data.yaml");
    if (Files.exists(dataYamlPath)) {
      Yaml yaml = new Yaml();
      Map<String, Object> dataYaml = yaml.load(Files.newInputStream(dataYamlPath));
      if (dataYaml.containsKey("names")) {
        Object namesObj = dataYaml.get("names");
        if (namesObj instanceof List) {
          classNames = (List<String>) namesObj;
        }
      }
    }

    // Fallback to model_metadata.json
    if (classNames.isEmpty()) {
      Path metadataPath = datasetRoot.resolve("model_metadata.json");
      if (Files.exists(metadataPath)) {
        JsonNode metadata = objectMapper.readTree(metadataPath.toFile());
        if (metadata.has("classList")) {
          for (JsonNode classItem : metadata.get("classList")) {
            if (classItem.has("name")) {
              classNames.add(classItem.get("name").asText());
            }
          }
        }
      }
    }

    // Default classes if none found
    if (classNames.isEmpty()) {
      classNames = Arrays.asList("class_0", "class_1", "class_2");
    }

    return classNames;
  }

  private Path findDatasetRoot(Path extractedDir) throws IOException {
    if (Files.exists(extractedDir.resolve("labels"))
        || Files.exists(extractedDir.resolve("images"))) {
      return extractedDir;
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(extractedDir)) {
      for (Path subDir : stream) {
        if (Files.isDirectory(subDir)) {
          if (Files.exists(subDir.resolve("labels")) || Files.exists(subDir.resolve("images"))) {
            return subDir;
          }
        }
      }
    }
    return extractedDir;
  }

  private String findImageFile(Path imagesDir, String baseName) {
    String[] extensions = {".jpg", ".jpeg", ".png", ".JPG", ".JPEG", ".PNG", ".bmp", ".BMP"};
    for (String ext : extensions) {
      if (Files.exists(imagesDir.resolve(baseName + ext))) {
        return baseName + ext;
      }
    }
    return baseName + ".jpg";
  }

  private List<String> findAllImages(Path baseDir) throws IOException {
    Set<String> imageFiles = new LinkedHashSet<>();
    String[] extensions = {".jpg", ".jpeg", ".png", ".JPG", ".JPEG", ".PNG", ".bmp", ".BMP"};

    List<Path> dirsToSearch = new ArrayList<>();
    Path imagesDir = baseDir.resolve("images");
    if (Files.exists(imagesDir)) {
      for (String split : new String[] {"train", "val", "test"}) {
        Path splitDir = imagesDir.resolve(split);
        if (Files.exists(splitDir)) dirsToSearch.add(splitDir);
      }
      dirsToSearch.add(imagesDir);
    }
    dirsToSearch.add(baseDir);

    for (Path searchDir : dirsToSearch) {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchDir)) {
        for (Path file : stream) {
          if (Files.isRegularFile(file)) {
            String fileName = file.getFileName().toString();
            for (String ext : extensions) {
              if (fileName.endsWith(ext)) {
                imageFiles.add(fileName);
                break;
              }
            }
          }
        }
      }
    }
    return new ArrayList<>(imageFiles);
  }

  private String generateMetric(double min, double max) {
    return String.format("%.2f", min + random.nextDouble() * (max - min));
  }

  private double applyVariation(double value, double variationPercent) {
    double variation = value * variationPercent * (random.nextDouble() * 2 - 1);
    return Math.max(0.0, Math.min(1.0, value + variation));
  }

  private void unzip(Path zipFile, Path destDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName().replace("\\", "/");
        Path filePath = destDir.resolve(entryName).normalize();

        if (!filePath.startsWith(destDir.normalize())) {
          zis.closeEntry();
          continue;
        }

        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          Files.createDirectories(filePath.getParent());
          Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        zis.closeEntry();
      }
    }
  }

  private void deleteDirectory(Path dir) throws IOException {
    if (Files.exists(dir)) {
      Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                } catch (IOException e) {
                }
              });
    }
  }
}
