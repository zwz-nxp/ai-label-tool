package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ProjectTag;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/** REST Service for Project Tag operations in the API Layer. */
@Service
@Slf4j
public class ProjectTagServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ProjectTagServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Create a new project tag
   *
   * @param projectId the project ID
   * @param projectTag the tag to create
   * @return the created tag
   */
  @MethodLog
  public ProjectTag createTag(Long projectId, ProjectTag projectTag) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<ProjectTag> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/project-tags?projectId={projectId}",
              projectTag,
              ProjectTag.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to create tag: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all tags for a specific project
   *
   * @param projectId the project ID
   * @return list of tags
   */
  @MethodLog
  public List<ProjectTag> getTagsByProjectId(Long projectId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<ProjectTag[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/project-tags/project/{projectId}",
              ProjectTag[].class,
              params);

      ProjectTag[] body = response.getBody();
      if (body == null) {
        log.warn("Received null body from operational layer for project {}", projectId);
        return List.of();
      }

      log.info("Retrieved {} tags for project {}", body.length, projectId);
      return Arrays.asList(body);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get tags for project {}: {}",
          projectId,
          e.getMessage());
      throw e;
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
  public ProjectTag updateTag(Long tagId, ProjectTag projectTag) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("tagId", tagId);

      ResponseEntity<ProjectTag> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/project-tags/{tagId}",
              HttpMethod.PUT,
              new org.springframework.http.HttpEntity<>(projectTag),
              ProjectTag.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to update tag {}: {}", tagId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a tag
   *
   * @param tagId the tag ID to delete
   */
  @MethodLog
  public void deleteTag(Long tagId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("tagId", tagId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/project-tags/{tagId}", params);
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to delete tag {}: {}", tagId, e.getMessage());
      throw e;
    }
  }
}
