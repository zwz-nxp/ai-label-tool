package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.operational.service.rest.landingai.DatasetExportService;
import java.io.File;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Image Export operations in the Operational Layer. */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/images")
@RequiredArgsConstructor
public class ImageExportController {

  private final DatasetExportService datasetExportService;

  /**
   * Export dataset as ZIP file for training
   *
   * @param projectId the project ID
   * @param imageIds optional list of image IDs to export (if null, exports all images)
   * @return ZIP file containing the dataset
   */
  @PostMapping("/project/{projectId}/export-dataset")
  public ResponseEntity<Resource> exportDataset(
      @PathVariable("projectId") Long projectId,
      @RequestBody(required = false) java.util.List<Long> imageIds) {
    try {
      log.info(
          "Operational layer: Exporting dataset for project: {}. Images: {}",
          projectId,
          imageIds != null ? imageIds.size() + " selected" : "all");

      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      // Call the export service
      File zipFile = datasetExportService.exportDataset(projectId, imageIds);

      // Read file into byte array
      byte[] data = Files.readAllBytes(zipFile.toPath());
      ByteArrayResource resource = new ByteArrayResource(data);

      // Clean up temp file
      zipFile.delete();

      // Generate filename
      String filename =
          String.format(
              "dataset-project-%d-%s.zip",
              projectId,
              java.time.LocalDateTime.now()
                  .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(data.length)
          .body(resource);

    } catch (Exception e) {
      log.error(
          "Operational layer: Error exporting dataset for project {}: {}",
          projectId,
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
