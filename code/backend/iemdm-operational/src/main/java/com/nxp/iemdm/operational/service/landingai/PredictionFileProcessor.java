package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.shared.dto.landingai.EpochMetrics;
import com.nxp.iemdm.shared.dto.landingai.PredictionLabel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for processing prediction files from Databricks. Handles file download, ZIP extraction,
 * CSV parsing, and cleanup.
 */
@Component
@Slf4j
public class PredictionFileProcessor {

  @Value("${prediction.file.temp-dir:/tmp/training-results}")
  private String tempDir;

  @Value("${prediction.file.max-size:104857600}") // 100MB default
  private long maxFileSize;

  /**
   * Download ZIP file from URL to temporary directory.
   *
   * @param url Prediction file URL
   * @return Path to downloaded file
   * @throws IOException if download fails
   */
  public Path downloadPredictionFile(String url) throws IOException {
    log.debug("Downloading prediction file from: {}", url);

    // Create temp directory if it doesn't exist
    Path tempDirPath = Paths.get(tempDir);
    if (!Files.exists(tempDirPath)) {
      Files.createDirectories(tempDirPath);
    }

    // Generate unique filename
    String filename = "prediction_" + System.currentTimeMillis() + ".zip";
    Path outputPath = tempDirPath.resolve(filename);

    // Download file
    try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile())) {

      byte[] dataBuffer = new byte[8192];
      int bytesRead;
      long totalBytesRead = 0;

      while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
        totalBytesRead += bytesRead;

        // Check file size limit
        if (totalBytesRead > maxFileSize) {
          throw new IOException(
              "File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }

        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }

      log.debug("Successfully downloaded prediction file to: {}", outputPath);
      return outputPath;

    } catch (IOException e) {
      // Clean up partial download
      if (Files.exists(outputPath)) {
        try {
          Files.delete(outputPath);
        } catch (IOException cleanupEx) {
          log.warn("Failed to clean up partial download: {}", cleanupEx.getMessage());
        }
      }
      throw e;
    }
  }

  /**
   * Extract results.csv from ZIP file.
   *
   * @param zipPath Path to ZIP file
   * @return Path to extracted CSV file
   * @throws IOException if extraction fails or results.csv not found
   */
  public Path extractResultsCsv(Path zipPath) throws IOException {
    log.debug("Extracting results.csv from: {}", zipPath);

    Path extractDir = zipPath.getParent().resolve("extracted_" + System.currentTimeMillis());
    Files.createDirectories(extractDir);

    try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipPath))) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (entry.getName().equals("results.csv") || entry.getName().endsWith("/results.csv")) {
          Path outputPath = extractDir.resolve("results.csv");

          try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zipIn.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }

          log.debug("Successfully extracted results.csv to: {}", outputPath);
          return outputPath;
        }
        zipIn.closeEntry();
      }
    }

    throw new IOException("results.csv not found in ZIP file: " + zipPath);
  }

  /**
   * Parse results.csv and extract epoch metrics.
   *
   * @param csvPath Path to CSV file
   * @return List of EpochMetrics
   * @throws IOException if parsing fails
   */
  public List<EpochMetrics> parseResultsCsv(Path csvPath) throws IOException {
    log.debug("Parsing results.csv from: {}", csvPath);

    List<EpochMetrics> metrics = new ArrayList<>();

    try (CSVReader reader = new CSVReader(new InputStreamReader(Files.newInputStream(csvPath)))) {
      List<String[]> rows = reader.readAll();

      if (rows.isEmpty()) {
        log.warn("CSV file is empty: {}", csvPath);
        return metrics;
      }

      // First row is header
      String[] header = rows.get(0);
      int epochIndex = findColumnIndex(header, "epoch");
      int timeIndex = findColumnIndex(header, "time");
      int lossIndex = findColumnIndex(header, "train/box_loss");
      int mapIndex = findColumnIndex(header, "metrics/mAP50(B)");

      if (epochIndex == -1 || lossIndex == -1 || mapIndex == -1) {
        throw new IOException(
            "CSV file missing required columns. Expected: epoch, train/box_loss, metrics/mAP50(B)");
      }

      // Parse data rows
      for (int i = 1; i < rows.size(); i++) {
        String[] row = rows.get(i);
        try {
          Integer epoch = Integer.parseInt(row[epochIndex].trim());
          String time = timeIndex != -1 ? row[timeIndex].trim() : null;
          Double trainBoxLoss = Double.parseDouble(row[lossIndex].trim());
          Double metricsMAP50B = Double.parseDouble(row[mapIndex].trim());

          metrics.add(new EpochMetrics(epoch, time, trainBoxLoss, metricsMAP50B));

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          log.warn("Failed to parse row {}: {}", i, e.getMessage());
          // Continue parsing other rows
        }
      }

      log.debug("Successfully parsed {} epoch metrics from CSV", metrics.size());
      return metrics;

    } catch (CsvException e) {
      throw new IOException("Failed to parse CSV file: " + e.getMessage(), e);
    }
  }

  /**
   * Parse prediction labels from ZIP file. Note: Implementation depends on the actual format of
   * prediction labels in the ZIP. This is a placeholder that should be updated based on actual file
   * format.
   *
   * @param zipPath Path to ZIP file
   * @return List of PredictionLabel
   * @throws IOException if parsing fails
   */
  public List<PredictionLabel> parsePredictionLabels(Path zipPath) throws IOException {
    log.debug("Parsing prediction labels from: {}", zipPath);

    // TODO: Implement based on actual prediction label format in ZIP
    // This is a placeholder implementation
    List<PredictionLabel> labels = new ArrayList<>();

    log.warn(
        "Prediction label parsing not yet implemented. Returning empty list. ZIP: {}", zipPath);

    return labels;
  }

  /**
   * Clean up temporary files.
   *
   * @param paths Paths to delete
   */
  public void cleanupTempFiles(Path... paths) {
    for (Path path : paths) {
      if (path != null && Files.exists(path)) {
        try {
          if (Files.isDirectory(path)) {
            // Delete directory and all contents
            Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before dirs
                .forEach(
                    p -> {
                      try {
                        Files.delete(p);
                      } catch (IOException e) {
                        log.warn("Failed to delete file: {}", p, e);
                      }
                    });
          } else {
            Files.delete(path);
          }
          log.debug("Cleaned up temp file: {}", path);
        } catch (IOException e) {
          log.warn("Failed to clean up temp file: {}", path, e);
        }
      }
    }
  }

  /**
   * Find column index by name (case-insensitive).
   *
   * @param header CSV header row
   * @param columnName Column name to find
   * @return Column index or -1 if not found
   */
  private int findColumnIndex(String[] header, String columnName) {
    for (int i = 0; i < header.length; i++) {
      if (header[i].trim().equalsIgnoreCase(columnName)) {
        return i;
      }
    }
    return -1;
  }
}
