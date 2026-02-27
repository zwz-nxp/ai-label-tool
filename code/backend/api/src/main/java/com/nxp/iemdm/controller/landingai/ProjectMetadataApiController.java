package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ProjectMetadata;
import com.nxp.iemdm.service.rest.landingai.ProjectMetadataServiceREST;
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
@RequestMapping("/api/landingai/project-metadata")
@RequiredArgsConstructor
public class ProjectMetadataApiController {

  private final ProjectMetadataServiceREST projectMetadataServiceREST;

  @GetMapping(value = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ProjectMetadata>> getMetadataByProjectId(
      @PathVariable Long projectId) {
    log.info("API request to get metadata for project: {}", projectId);
    List<ProjectMetadata> metadata = projectMetadataServiceREST.getMetadataByProjectId(projectId);
    return ResponseEntity.ok(metadata);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectMetadata> getMetadataById(@PathVariable Long id) {
    log.info("API request to get metadata: {}", id);
    ProjectMetadata metadata = projectMetadataServiceREST.getMetadataById(id);
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
    log.info("API request to create metadata: {} for project: {}", metadata.getName(), projectId);
    ProjectMetadata created = projectMetadataServiceREST.createMetadata(projectId, metadata);
    return ResponseEntity.ok(created);
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectMetadata> updateMetadata(
      @PathVariable Long id, @RequestBody ProjectMetadata metadata) {
    log.info("API request to update metadata: {}", id);
    ProjectMetadata updated = projectMetadataServiceREST.updateMetadata(id, metadata);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMetadata(@PathVariable Long id) {
    log.info("API request to delete metadata: {}", id);
    projectMetadataServiceREST.deleteMetadata(id);
    return ResponseEntity.ok().build();
  }
}
