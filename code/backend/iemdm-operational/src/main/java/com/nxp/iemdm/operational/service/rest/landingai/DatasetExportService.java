package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.*;
import com.nxp.iemdm.shared.repository.jpa.landingai.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for exporting training datasets with streaming pipeline optimization. Implements
 * high-performance export using in-memory streaming to avoid disk I/O bottlenecks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetExportService {

  private final ProjectRepository projectRepository;
  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ImageFileRepository imageFileRepository;
  private final ProjectClassRepository projectClassRepository;
  private final SnapshotImageRepository snapshotImageRepository;
  private final SnapshotImageLabelRepository snapshotImageLabelRepository;
  private final SnapshotProjectClassRepository snapshotProjectClassRepository;
  private final SnapshotRepository snapshotRepository;

  private static final int BATCH_SIZE = 100;
  private static final int THREAD_POOL_SIZE = 4;
  private static final long MAX_ZIP_SIZE = 5L * 1024 * 1024 * 1024; // 5GB

  /** Export dataset as ZIP file with optimized streaming pipeline */
  @Transactional(readOnly = true)
  public File exportDataset(Long projectId, List<Long> imageIds)
      throws IOException, ExecutionException, InterruptedException {
    log.info(
        "Starting dataset export for project ID: {}. Image filter: {}",
        projectId,
        imageIds != null ? imageIds.size() + " selected" : "all");

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

    // Create temporary file for ZIP
    Path tempFile = Files.createTempFile("dataset-export-" + projectId + "-", ".zip");

    try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
        BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
        ZipOutputStream zipOut = new ZipOutputStream(bos)) {

      // Use fast compression for better performance
      zipOut.setLevel(Deflater.BEST_SPEED);

      // Export based on project type
      if ("Classification".equalsIgnoreCase(project.getType())) {
        exportClassificationDataset(projectId, imageIds, zipOut);
      } else if ("Object Detection".equalsIgnoreCase(project.getType())
          || "Segmentation".equalsIgnoreCase(project.getType())) {
        exportObjectDetectionDataset(projectId, imageIds, zipOut);
      } else {
        throw new IllegalArgumentException("Unsupported project type: " + project.getType());
      }
    }

    log.info(
        "Dataset export completed for project ID: {}. File size: {} bytes",
        projectId,
        tempFile.toFile().length());

    return tempFile.toFile();
  }

  /**
   * Export Classification dataset structure: train/ class1/ image1.jpg image2.jpg class2/ val/
   * class1/ test/ class1/
   */
  private void exportClassificationDataset(
      Long projectId, List<Long> imageIds, ZipOutputStream zipOut)
      throws IOException, ExecutionException, InterruptedException {

    log.info("Exporting Classification dataset for project: {}", projectId);

    // Get all classes
    List<ProjectClass> classes = projectClassRepository.findByProject_Id(projectId);
    Map<Long, String> classIdToName = new HashMap<>();
    for (ProjectClass pc : classes) {
      classIdToName.put(pc.getId(), pc.getClassName());
    }

    // Create empty split folders (train/val/test/unassigned) to ensure they always exist
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("unassigned/"));
      zipOut.closeEntry();
    }

    // Process images in batches with parallel processing
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    List<Future<Void>> futures = new ArrayList<>();

    int offset = 0;
    while (true) {
      List<Image> images;
      if (imageIds != null && !imageIds.isEmpty()) {
        // Export selected images only - fetch in batches from the filtered list
        int endIndex = Math.min(offset + BATCH_SIZE, imageIds.size());
        if (offset >= imageIds.size()) {
          break;
        }
        List<Long> batchIds = imageIds.subList(offset, endIndex);
        images = imageRepository.findAllById(batchIds);
      } else {
        // Export all images in project
        images = imageRepository.findByProjectIdOrderById(projectId, offset, BATCH_SIZE);
      }

      if (images.isEmpty()) {
        break;
      }

      // Process batch in parallel
      List<Image> batchCopy = new ArrayList<>(images);
      Future<Void> future =
          executor.submit(
              () -> {
                processClassificationBatch(batchCopy, classIdToName, zipOut);
                return null;
              });
      futures.add(future);

      offset += BATCH_SIZE;
    }

    // Wait for all batches to complete
    for (Future<Void> future : futures) {
      future.get();
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
  }

  private void processClassificationBatch(
      List<Image> images, Map<Long, String> classIdToName, ZipOutputStream zipOut)
      throws IOException {
    for (Image image : images) {
      // Get image labels (all labels in la_images_label are ground truth)
      List<ImageLabel> labels = imageLabelRepository.findByImage_Id(image.getId());

      if (labels.isEmpty()) {
        log.warn("Image {} has no ground truth labels, skipping", image.getId());
        continue;
      }

      // Get first label's class
      ImageLabel label = labels.get(0);
      String className = classIdToName.get(label.getProjectClass().getId());
      if (className == null) {
        log.warn(
            "Class not found for label {}, skipping image {}",
            label.getProjectClass().getId(),
            image.getId());
        continue;
      }

      // Determine split folder (train/val/test)
      String splitFolder = getSplitFolder(image.getSplit());

      // Get image file data and unique filename
      ImageFileData imageFileData = getImageFileData(image.getFileId());
      if (imageFileData == null || imageFileData.data == null) {
        log.warn("Image file not found for image {}, skipping", image.getId());
        continue;
      }

      // Use unique filename from la_images_file table
      String uniqueFileName = imageFileData.uniqueFileName;

      // Build ZIP path: train/class1/unique_image.jpg
      String zipPath = String.format("%s/%s/%s", splitFolder, className, uniqueFileName);

      // Write to ZIP (synchronized for thread safety)
      synchronized (zipOut) {
        zipOut.putNextEntry(new ZipEntry(zipPath));
        zipOut.write(imageFileData.data);
        zipOut.closeEntry();
      }
    }
  }

  /**
   * Export Object Detection dataset structure: dataset/ data.yaml images/ train/ image1.jpg val/
   * test/ labels/ train/ image1.txt val/ test/
   */
  /**
   * Export Object Detection dataset structure: dataset/ data.yaml images/ train/ image1.jpg val/
   * test/ labels/ train/ image1.txt val/ test/
   */
  private void exportObjectDetectionDataset(
      Long projectId, List<Long> imageIds, ZipOutputStream zipOut)
      throws IOException, ExecutionException, InterruptedException {

    log.info("Exporting Object Detection dataset for project: {}", projectId);

    // Get all classes and create ID mapping (ordered by class name for consistency)
    List<ProjectClass> classes = projectClassRepository.findByProject_Id(projectId);
    classes.sort((a, b) -> a.getClassName().compareTo(b.getClassName()));

    Map<Long, Integer> classIdToIndex = new HashMap<>();
    Map<Long, String> classIdToName = new HashMap<>();
    for (int i = 0; i < classes.size(); i++) {
      ProjectClass pc = classes.get(i);
      classIdToIndex.put(pc.getId(), i);
      classIdToName.put(pc.getId(), pc.getClassName());
    }

    // Generate data.yaml content
    StringBuilder yamlContent = new StringBuilder();
    yamlContent.append("# YOLO Dataset Configuration\n");
    yamlContent.append("# Path will be configured by data scientist\n");
    yamlContent.append("path: dataset\n");
    yamlContent.append("\n");
    yamlContent.append("# Dataset splits (relative to path)\n");
    yamlContent.append("train: images/train\n");
    yamlContent.append("val: images/val\n");
    yamlContent.append("test: images/test\n");
    yamlContent.append("\n");
    yamlContent.append("# Class names (ID: name mapping)\n");
    yamlContent.append("names:\n");
    for (int i = 0; i < classes.size(); i++) {
      yamlContent.append(String.format("  %d: %s\n", i, classes.get(i).getClassName()));
    }

    // Write data.yaml to ZIP root
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("dataset/data.yaml"));
      zipOut.write(yamlContent.toString().getBytes());
      zipOut.closeEntry();
    }

    // Create empty split folders to ensure they always exist
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("dataset/images/train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/unassigned/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/unassigned/"));
      zipOut.closeEntry();
    }

    // Process images in batches
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    List<Future<Void>> futures = new ArrayList<>();

    int offset = 0;
    while (true) {
      List<Image> images;
      if (imageIds != null && !imageIds.isEmpty()) {
        // Export selected images only - fetch in batches from the filtered list
        int endIndex = Math.min(offset + BATCH_SIZE, imageIds.size());
        if (offset >= imageIds.size()) {
          break;
        }
        List<Long> batchIds = imageIds.subList(offset, endIndex);
        images = imageRepository.findAllById(batchIds);
      } else {
        // Export all images in project
        images = imageRepository.findByProjectIdOrderById(projectId, offset, BATCH_SIZE);
      }

      if (images.isEmpty()) {
        break;
      }

      // Process batch in parallel
      List<Image> batchCopy = new ArrayList<>(images);
      Future<Void> future =
          executor.submit(
              () -> {
                processObjectDetectionBatch(batchCopy, classIdToIndex, zipOut);
                return null;
              });
      futures.add(future);

      offset += BATCH_SIZE;
    }

    // Wait for all batches to complete
    for (Future<Void> future : futures) {
      future.get();
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);
  }

  private void processObjectDetectionBatch(
      List<Image> images, Map<Long, Integer> classIdToIndex, ZipOutputStream zipOut)
      throws IOException {
    for (Image image : images) {
      // Determine split folder
      String splitFolder = getSplitFolder(image.getSplit());

      // Get image file data and unique filename
      ImageFileData imageFileData = getImageFileData(image.getFileId());
      if (imageFileData == null || imageFileData.data == null) {
        log.warn("Image file not found for image {}, skipping", image.getId());
        continue;
      }

      // Use unique filename from la_images_file table
      String uniqueFileName = imageFileData.uniqueFileName;

      // Write image file: dataset/images/train/unique_image.jpg
      String imagePath = String.format("dataset/images/%s/%s", splitFolder, uniqueFileName);
      synchronized (zipOut) {
        zipOut.putNextEntry(new ZipEntry(imagePath));
        zipOut.write(imageFileData.data);
        zipOut.closeEntry();
      }

      // Generate label file content
      String labelFileName = uniqueFileName.replaceFirst("\\.[^.]+$", ".txt");
      String labelPath = String.format("dataset/labels/%s/%s", splitFolder, labelFileName);

      // Check if image is marked as "no class" (empty label file)
      if (Boolean.TRUE.equals(image.getIsNoClass())) {
        // Write empty label file for is_no_class images
        synchronized (zipOut) {
          zipOut.putNextEntry(new ZipEntry(labelPath));
          zipOut.write(new byte[0]); // Empty file
          zipOut.closeEntry();
        }
      } else {
        // Generate label file content from labels
        List<ImageLabel> labels = imageLabelRepository.findByImage_Id(image.getId());

        if (!labels.isEmpty()) {
          StringBuilder labelContent = new StringBuilder();
          for (ImageLabel label : labels) {
            Integer classIndex = classIdToIndex.get(label.getProjectClass().getId());
            if (classIndex != null && label.getPosition() != null) {
              // Parse position JSON: {"type":"rectangle","x": 0.5, "y": 0.5, "width": 0.1,
              // "height": 0.1}
              String position = label.getPosition();
              String labelLine = formatObjectDetectionLabel(classIndex, position);
              labelContent.append(labelLine).append("\n");
            }
          }

          // Write label file: dataset/labels/train/unique_image.txt
          synchronized (zipOut) {
            zipOut.putNextEntry(new ZipEntry(labelPath));
            zipOut.write(labelContent.toString().getBytes());
            zipOut.closeEntry();
          }
        }
      }
    }
  }

  /** Format label line for object detection: ClassIndex x y width height */
  private String formatObjectDetectionLabel(Integer classIndex, String positionJson) {
    try {
      // Simple JSON parsing - extract only numeric fields
      positionJson =
          positionJson.replace("{", "").replace("}", "").replace("\"", "").replace(" ", "");

      Map<String, String> values = new HashMap<>();
      for (String pair : positionJson.split(",")) {
        String[] kv = pair.split(":");
        if (kv.length == 2) {
          values.put(kv[0], kv[1]);
        }
      }

      // Only parse numeric fields, skip "type" field
      double x = parseDouble(values.get("x"), 0.0);
      double y = parseDouble(values.get("y"), 0.0);
      double width = parseDouble(values.get("width"), 0.0);
      double height = parseDouble(values.get("height"), 0.0);

      return String.format("%d %.6f %.6f %.6f %.6f", classIndex, x, y, width, height);
    } catch (Exception e) {
      log.error("Failed to parse position JSON: {}", positionJson, e);
      return classIndex + " 0 0 0 0";
    }
  }

  /** Safely parse double value */
  private double parseDouble(String value, double defaultValue) {
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /** Get split folder name (train/val/test/unassigned) */
  private String getSplitFolder(String split) {
    if (split == null || split.isEmpty() || "unassigned".equalsIgnoreCase(split)) {
      return "unassigned";
    }

    String lowerSplit = split.toLowerCase();

    // Handle both "train" and "training" values
    if (lowerSplit.equals("train") || lowerSplit.equals("training")) {
      return "train";
    }

    switch (lowerSplit) {
      case "dev":
        return "val";
      case "test":
        return "test";
      default:
        return "unassigned";
    }
  }

  /** Get image file data from database and return both data and unique filename */
  private ImageFileData getImageFileData(Long fileId) {
    if (fileId == null) {
      return null;
    }

    Optional<ImageFile> imageFileOpt = imageFileRepository.findById(fileId);
    if (imageFileOpt.isEmpty()) {
      return null;
    }

    ImageFile imageFile = imageFileOpt.get();
    return new ImageFileData(imageFile.getImageFileStream(), imageFile.getFileName());
  }

  /** Helper class to return both image data and unique filename */
  private static class ImageFileData {
    final byte[] data;
    final String uniqueFileName;

    ImageFileData(byte[] data, String uniqueFileName) {
      this.data = data;
      this.uniqueFileName = uniqueFileName;
    }
  }

  /**
   * Export snapshot dataset as ZIP file
   *
   * @param snapshotId the snapshot ID
   * @return File containing the exported dataset
   */
  @Transactional(readOnly = true)
  public File exportSnapshotDataset(Long snapshotId)
      throws IOException, ExecutionException, InterruptedException {
    log.info("Starting snapshot dataset export for snapshot ID: {}", snapshotId);

    // Get snapshot to determine project type
    Snapshot snapshot =
        snapshotRepository
            .findById(snapshotId)
            .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + snapshotId));

    Project project = snapshot.getProject();
    if (project == null) {
      throw new IllegalArgumentException("Project not found for snapshot: " + snapshotId);
    }

    // Create temporary file for ZIP
    Path tempFile = Files.createTempFile("snapshot-export-" + snapshotId + "-", ".zip");

    try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
        BufferedOutputStream bos = new BufferedOutputStream(fos, 64 * 1024);
        ZipOutputStream zipOut = new ZipOutputStream(bos)) {

      // Use fast compression for better performance
      zipOut.setLevel(Deflater.BEST_SPEED);

      // Export based on project type
      if ("Classification".equalsIgnoreCase(project.getType())) {
        exportSnapshotClassificationDataset(snapshotId, zipOut);
      } else if ("Object Detection".equalsIgnoreCase(project.getType())
          || "Segmentation".equalsIgnoreCase(project.getType())) {
        exportSnapshotObjectDetectionDataset(snapshotId, zipOut);
      } else {
        throw new IllegalArgumentException("Unsupported project type: " + project.getType());
      }
    }

    log.info(
        "Snapshot dataset export completed for snapshot ID: {}. File size: {} bytes",
        snapshotId,
        tempFile.toFile().length());

    return tempFile.toFile();
  }

  /** Export Classification dataset from snapshot */
  private void exportSnapshotClassificationDataset(Long snapshotId, ZipOutputStream zipOut)
      throws IOException {
    log.info("Exporting Classification snapshot dataset for snapshot: {}", snapshotId);

    // Get all classes from snapshot
    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findBySnapshotIdOrderBySequence(snapshotId);
    Map<Long, String> classIdToName = new HashMap<>();
    for (SnapshotProjectClass pc : classes) {
      classIdToName.put(pc.getId(), pc.getClassName());
    }

    // Create empty split folders
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("unassigned/"));
      zipOut.closeEntry();
    }

    // Get all snapshot images
    List<SnapshotImage> images = snapshotImageRepository.findBySnapshotId(snapshotId);
    log.info("Found {} images in snapshot {}", images.size(), snapshotId);

    // Get all labels for this snapshot
    List<SnapshotImageLabel> allLabels = snapshotImageLabelRepository.findBySnapshotId(snapshotId);
    Map<Long, List<SnapshotImageLabel>> labelsByImageId = new HashMap<>();
    for (SnapshotImageLabel label : allLabels) {
      labelsByImageId.computeIfAbsent(label.getImageId(), k -> new ArrayList<>()).add(label);
    }

    // Process each image
    for (SnapshotImage image : images) {
      List<SnapshotImageLabel> labels = labelsByImageId.get(image.getId());

      if (labels == null || labels.isEmpty()) {
        log.warn("Snapshot image {} has no labels, skipping", image.getId());
        continue;
      }

      // Get first label's class
      SnapshotImageLabel label = labels.get(0);
      String className = classIdToName.get(label.getClassId());
      if (className == null) {
        log.warn(
            "Class not found for label {}, skipping image {}", label.getClassId(), image.getId());
        continue;
      }

      // Determine split folder
      String splitFolder = getSplitFolder(image.getSplit());

      // Get image file data
      ImageFileData imageFileData = getImageFileData(image.getFileId());
      if (imageFileData == null || imageFileData.data == null) {
        log.warn("Image file not found for snapshot image {}, skipping", image.getId());
        continue;
      }

      // Build ZIP path
      String zipPath =
          String.format("%s/%s/%s", splitFolder, className, imageFileData.uniqueFileName);

      // Write to ZIP
      synchronized (zipOut) {
        zipOut.putNextEntry(new ZipEntry(zipPath));
        zipOut.write(imageFileData.data);
        zipOut.closeEntry();
      }
    }
  }

  /** Export Object Detection dataset from snapshot */
  private void exportSnapshotObjectDetectionDataset(Long snapshotId, ZipOutputStream zipOut)
      throws IOException {
    log.info("Exporting Object Detection snapshot dataset for snapshot: {}", snapshotId);

    // Get all classes from snapshot and create ID mapping
    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findBySnapshotIdOrderBySequence(snapshotId);
    classes.sort((a, b) -> a.getClassName().compareTo(b.getClassName()));

    Map<Long, Integer> classIdToIndex = new HashMap<>();
    for (int i = 0; i < classes.size(); i++) {
      SnapshotProjectClass pc = classes.get(i);
      classIdToIndex.put(pc.getId(), i);
    }

    // Generate data.yaml content
    StringBuilder yamlContent = new StringBuilder();
    yamlContent.append("# YOLO Dataset Configuration\n");
    yamlContent.append("# Path will be configured by data scientist\n");
    yamlContent.append("path: dataset\n");
    yamlContent.append("\n");
    yamlContent.append("# Dataset splits (relative to path)\n");
    yamlContent.append("train: images/train\n");
    yamlContent.append("val: images/val\n");
    yamlContent.append("test: images/test\n");
    yamlContent.append("\n");
    yamlContent.append("# Class names (ID: name mapping)\n");
    yamlContent.append("names:\n");
    for (int i = 0; i < classes.size(); i++) {
      yamlContent.append(String.format("  %d: %s\n", i, classes.get(i).getClassName()));
    }

    // Write data.yaml to ZIP root
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("dataset/data.yaml"));
      zipOut.write(yamlContent.toString().getBytes());
      zipOut.closeEntry();
    }

    // Create empty split folders
    synchronized (zipOut) {
      zipOut.putNextEntry(new ZipEntry("dataset/images/train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/images/unassigned/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/train/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/val/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/test/"));
      zipOut.closeEntry();
      zipOut.putNextEntry(new ZipEntry("dataset/labels/unassigned/"));
      zipOut.closeEntry();
    }

    // Get all snapshot images
    List<SnapshotImage> images = snapshotImageRepository.findBySnapshotId(snapshotId);
    log.info("Found {} images in snapshot {}", images.size(), snapshotId);

    // Get all labels for this snapshot
    List<SnapshotImageLabel> allLabels = snapshotImageLabelRepository.findBySnapshotId(snapshotId);
    Map<Long, List<SnapshotImageLabel>> labelsByImageId = new HashMap<>();
    for (SnapshotImageLabel label : allLabels) {
      labelsByImageId.computeIfAbsent(label.getImageId(), k -> new ArrayList<>()).add(label);
    }

    // Process each image
    for (SnapshotImage image : images) {
      // Determine split folder
      String splitFolder = getSplitFolder(image.getSplit());

      // Get image file data
      ImageFileData imageFileData = getImageFileData(image.getFileId());
      if (imageFileData == null || imageFileData.data == null) {
        log.warn("Image file not found for snapshot image {}, skipping", image.getId());
        continue;
      }

      // Write image file
      String imagePath =
          String.format("dataset/images/%s/%s", splitFolder, imageFileData.uniqueFileName);
      synchronized (zipOut) {
        zipOut.putNextEntry(new ZipEntry(imagePath));
        zipOut.write(imageFileData.data);
        zipOut.closeEntry();
      }

      // Generate label file
      String labelFileName = imageFileData.uniqueFileName.replaceFirst("\\.[^.]+$", ".txt");
      String labelPath = String.format("dataset/labels/%s/%s", splitFolder, labelFileName);

      // Check if image is marked as "no class" (empty label file)
      if (Boolean.TRUE.equals(image.getIsNoClass())) {
        // Write empty label file for is_no_class images
        synchronized (zipOut) {
          zipOut.putNextEntry(new ZipEntry(labelPath));
          zipOut.write(new byte[0]); // Empty file
          zipOut.closeEntry();
        }
      } else {
        // Generate label file content from labels
        List<SnapshotImageLabel> labels = labelsByImageId.get(image.getId());

        if (labels != null && !labels.isEmpty()) {
          StringBuilder labelContent = new StringBuilder();
          for (SnapshotImageLabel label : labels) {
            Integer classIndex = classIdToIndex.get(label.getClassId());
            if (classIndex != null && label.getPosition() != null) {
              String labelLine = formatObjectDetectionLabel(classIndex, label.getPosition());
              labelContent.append(labelLine).append("\n");
            }
          }

          // Write label file
          synchronized (zipOut) {
            zipOut.putNextEntry(new ZipEntry(labelPath));
            zipOut.write(labelContent.toString().getBytes());
            zipOut.closeEntry();
          }
        }
      }
    }
  }
}
