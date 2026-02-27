package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ProjectTag;
import com.nxp.iemdm.service.rest.landingai.ProjectTagServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller for Project Tag operations. Provides RESTful endpoints for frontend communication.
 */
@RestController
@RequestMapping("/api/landingai/project-tags")
@Slf4j
public class ProjectTagApiController {

  private final ProjectTagServiceREST projectTagServiceREST;

  @Autowired
  public ProjectTagApiController(ProjectTagServiceREST projectTagServiceREST) {
    this.projectTagServiceREST = projectTagServiceREST;
  }

  /**
   * Create a new project tag
   *
   * @param projectId the project ID
   * @param projectTag the tag to create
   * @return the created tag with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectTag> createTag(
      @RequestParam("projectId") Long projectId, @RequestBody @Valid ProjectTag projectTag) {
    try {
      ProjectTag createdTag = projectTagServiceREST.createTag(projectId, projectTag);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    } catch (Exception e) {
      log.error("Error creating project tag: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all tags for a specific project
   *
   * @param projectId the project ID
   * @return list of tags with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ProjectTag>> getTagsByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ProjectTag> tags = projectTagServiceREST.getTagsByProjectId(projectId);
      return ResponseEntity.ok(tags);
    } catch (Exception e) {
      log.error("Error getting tags for project {}: {}", projectId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing tag
   *
   * @param tagId the tag ID to update
   * @param projectTag the updated tag data
   * @return the updated tag with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{tagId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectTag> updateTag(
      @PathVariable("tagId") Long tagId, @RequestBody @Valid ProjectTag projectTag) {
    try {
      if (tagId == null || tagId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ProjectTag updatedTag = projectTagServiceREST.updateTag(tagId, projectTag);
      return ResponseEntity.ok(updatedTag);
    } catch (Exception e) {
      log.error("Error updating tag {}: {}", tagId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a tag
   *
   * @param tagId the tag ID to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{tagId}")
  public ResponseEntity<Void> deleteTag(@PathVariable("tagId") Long tagId) {
    try {
      if (tagId == null || tagId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      projectTagServiceREST.deleteTag(tagId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting tag {}: {}", tagId, e.getMessage());
      throw e;
    }
  }
}
