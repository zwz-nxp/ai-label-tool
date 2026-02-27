package com.nxp.iemdm.operational.controller.landingai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for file upload operations. Handles temporary file storage for Test Model
 * feature.
 */
@RestController
@RequestMapping("/operational/landingai/files")
@CrossOrigin(origins = "*")
@Slf4j
public class FileUploadController {

  @Value("${landingai.upload.temp-dir:C:/temp/landingai/uploads}")
  private String uploadTempDir;

  /**
   * Upload a zip file for test model processing.
   *
   * @param file Multipart file (zip)
   * @param trackId Track ID for organizing files
   * @return Response with file path information
   */
  @PostMapping(value = "/upload", consumes = "multipart/form-data")
  public ResponseEntity<Map<String, String>> uploadFile(
      @RequestParam("file") MultipartFile file, @RequestParam("trackId") String trackId) {

    log.info(
        "Received file upload request - filename: {}, size: {}, trackId: {}",
        file.getOriginalFilename(),
        file.getSize(),
        trackId);

    try {
      // 驗證檔案
      if (file.isEmpty()) {
        log.error("Uploaded file is empty");
        return ResponseEntity.badRequest()
            .body(Map.of("error", "File is empty", "status", "ERROR"));
      }

      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
        log.error("Invalid file type: {}", originalFilename);
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Only ZIP files are allowed", "status", "ERROR"));
      }

      // 建立上傳目錄
      Path uploadDir = Paths.get(uploadTempDir, trackId);
      Files.createDirectories(uploadDir);
      log.info("Created upload directory: {}", uploadDir.toAbsolutePath());

      // 產生唯一檔名 (保留原始副檔名)
      String uniqueFilename =
          UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
      Path targetPath = uploadDir.resolve(uniqueFilename);

      // 儲存檔案
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      log.info("File saved to: {}", targetPath.toAbsolutePath());

      // 回傳檔案資訊
      Map<String, String> response = new HashMap<>();
      response.put("status", "SUCCESS");
      response.put("filename", uniqueFilename);
      response.put("zipPath", uploadDir.toAbsolutePath().toString() + "/");
      response.put("fullPath", targetPath.toAbsolutePath().toString());
      response.put("size", String.valueOf(file.getSize()));

      return ResponseEntity.ok(response);

    } catch (IOException e) {
      log.error("Error uploading file", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to upload file: " + e.getMessage(), "status", "ERROR"));
    }
  }

  /**
   * Delete uploaded file after processing.
   *
   * @param trackId Track ID
   * @param filename Filename to delete
   * @return Response with deletion status
   */
  @DeleteMapping("/delete")
  public ResponseEntity<Map<String, String>> deleteFile(
      @RequestParam("trackId") String trackId, @RequestParam("filename") String filename) {

    log.info("Deleting file - trackId: {}, filename: {}", trackId, filename);

    try {
      Path filePath = Paths.get(uploadTempDir, trackId, filename);

      if (Files.exists(filePath)) {
        Files.delete(filePath);
        log.info("File deleted: {}", filePath.toAbsolutePath());
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "File deleted"));
      } else {
        log.warn("File not found: {}", filePath.toAbsolutePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("status", "ERROR", "message", "File not found"));
      }

    } catch (IOException e) {
      log.error("Error deleting file", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("status", "ERROR", "message", "Failed to delete file: " + e.getMessage()));
    }
  }
}
