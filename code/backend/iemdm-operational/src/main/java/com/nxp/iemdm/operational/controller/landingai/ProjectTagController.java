package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ProjectTag;
import com.nxp.iemdm.operational.service.landingai.ProjectTagService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Project Tag operations in the Operational Layer. */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/project-tags")
public class ProjectTagController {

  private final ProjectTagService projectTagService;

  public ProjectTagController(ProjectTagService projectTagService) {
    this.projectTagService = projectTagService;
  }

  /**
   * Create a new project tag
   *
   * @param projectId the project ID
   * @param projectTag the tag to create
   * @return the created tag
   */
  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<?> createTag(
      @RequestParam("projectId") Long projectId, @RequestBody ProjectTag projectTag) {
    try {
      ProjectTag savedTag = projectTagService.createTag(projectId, projectTag);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedTag);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating tag: {}", e.getMessage());
      return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    } catch (Exception e) {
      log.error("Error creating tag", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all tags for a specific project
   *
   * @param projectId the project ID
   * @return list of tags
   */
  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ProjectTag>> getTagsByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      List<ProjectTag> tags = projectTagService.getTagsByProjectId(projectId);
      return ResponseEntity.ok(tags);
    } catch (Exception e) {
      log.error("Error retrieving tags for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update an existing tag
   *
   * @param tagId the tag ID to update
   * @param projectTag the updated tag data
   * @return the updated tag
   */
  @MethodLog
  @Transactional
  @PutMapping(
      path = "/{tagId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<?> updateTag(
      @PathVariable("tagId") Long tagId, @RequestBody ProjectTag projectTag) {
    try {
      ProjectTag updatedTag = projectTagService.updateTag(tagId, projectTag);
      return ResponseEntity.ok(updatedTag);
    } catch (NotFoundException e) {
      log.error("Tag not found: {}", tagId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error updating tag: {}", e.getMessage());
      return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    } catch (Exception e) {
      log.error("Error updating tag {}", tagId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a tag
   *
   * @param tagId the tag ID to delete
   * @return no content on success
   */
  @MethodLog
  @Transactional
  @DeleteMapping(path = "/{tagId}")
  public ResponseEntity<Void> deleteTag(@PathVariable("tagId") Long tagId) {
    try {
      projectTagService.deleteTag(tagId);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      log.error("Tag not found: {}", tagId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.error("Cannot delete tag: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      log.error("Error deleting tag {}", tagId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
