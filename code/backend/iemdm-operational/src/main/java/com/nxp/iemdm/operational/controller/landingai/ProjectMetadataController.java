package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.model.landingai.ProjectMetadata;
import com.nxp.iemdm.operational.service.landingai.ProjectMetadataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/operational/landingai/project-metadata")
@RequiredArgsConstructor
public class ProjectMetadataController {

  private final ProjectMetadataService projectMetadataService;

  @GetMapping(value = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ProjectMetadata>> getMetadataByProjectId(
      @PathVariable Long projectId) {
    log.info("REST request to get metadata for project: {}", projectId);
    List<ProjectMetadata> metadata = projectMetadataService.getMetadataByProjectId(projectId);
    return ResponseEntity.ok(metadata);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectMetadata> getMetadataById(@PathVariable Long id) {
    log.info("REST request to get metadata: {}", id);
    ProjectMetadata metadata = projectMetadataService.getMetadataById(id);
    return ResponseEntity.ok(metadata);
  }

  /**
   * Create a new metadata definition
   *
   * @param projectId the project ID to associate the metadata with
   * @param metadata the metadata to create
   * @return the created metadata
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectMetadata> createMetadata(
      @RequestParam("projectId") Long projectId, @RequestBody ProjectMetadata metadata) {
    log.info("REST request to create metadata: {} for project: {}", metadata.getName(), projectId);
    ProjectMetadata created = projectMetadataService.createMetadata(projectId, metadata);
    return ResponseEntity.ok(created);
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectMetadata> updateMetadata(
      @PathVariable Long id, @RequestBody ProjectMetadata metadata) {
    log.info("REST request to update metadata: {}", id);
    ProjectMetadata updated = projectMetadataService.updateMetadata(id, metadata);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMetadata(@PathVariable Long id) {
    log.info("REST request to delete metadata: {}", id);
    try {
      projectMetadataService.deleteMetadata(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalStateException e) {
      log.error("Cannot delete metadata: {}", e.getMessage());
      return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT).build();
    } catch (RuntimeException e) {
      if (e.getMessage() != null && e.getMessage().contains("not found")) {
        log.error("Metadata not found: {}", id);
        return ResponseEntity.notFound().build();
      }
      throw e;
    }
  }
}
