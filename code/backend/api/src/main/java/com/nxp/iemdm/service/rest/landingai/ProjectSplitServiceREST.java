package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ProjectSplit;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO;
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

/**
 * REST Service for ProjectSplit operations in the API Layer. Uses RestTemplate to call Operational
 * Layer endpoints.
 *
 * <p>Requirements: 23.7, 23.5
 */
@Service
@Slf4j
public class ProjectSplitServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ProjectSplitServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Get split preview data for a project. Returns aggregated split distribution data for
   * visualization.
   *
   * @param projectId the project ID
   * @return split preview data
   */
  @MethodLog
  public SplitPreviewDTO getSplitPreview(Long projectId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<SplitPreviewDTO> response =
          restTemplate.getForEntity(
              operationalServiceURI
                  + "/operational/landingai/project-splits/preview?projectId={projectId}",
              SplitPreviewDTO.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get split preview for project {}: {}",
          projectId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Create a new project split configuration
   *
   * @param projectSplit the project split to create
   * @return the created project split
   */
  @MethodLog
  public ProjectSplit createProjectSplit(ProjectSplit projectSplit) {
    try {
      ResponseEntity<ProjectSplit> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/project-splits",
              projectSplit,
              ProjectSplit.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to create project split: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get a project split by ID
   *
   * @param splitId the project split ID
   * @return the project split
   */
  @MethodLog
  public ProjectSplit getProjectSplitById(Long splitId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("splitId", splitId);

      ResponseEntity<ProjectSplit> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/project-splits/{splitId}",
              ProjectSplit.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get project split {}: {}", splitId, e.getMessage());
      throw e;
    }
  }

  /**
   * Get all project splits for a specific project
   *
   * @param projectId the project ID
   * @return list of project splits
   */
  @MethodLog
  public List<ProjectSplit> getProjectSplitsByProjectId(Long projectId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<ProjectSplit[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/project-splits/project/{projectId}",
              ProjectSplit[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get project splits for project {}: {}",
          projectId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing project split
   *
   * @param splitId the project split ID to update
   * @param projectSplit the updated project split data
   * @return the updated project split
   */
  @MethodLog
  public ProjectSplit updateProjectSplit(Long splitId, ProjectSplit projectSplit) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("splitId", splitId);

      ResponseEntity<ProjectSplit> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/project-splits/{splitId}",
              HttpMethod.PUT,
              new org.springframework.http.HttpEntity<>(projectSplit),
              ProjectSplit.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update project split {}: {}",
          splitId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a project split
   *
   * @param splitId the project split ID to delete
   */
  @MethodLog
  public void deleteProjectSplit(Long splitId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("splitId", splitId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/project-splits/{splitId}", params);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to delete project split {}: {}",
          splitId,
          e.getMessage());
      throw e;
    }
  }
}
