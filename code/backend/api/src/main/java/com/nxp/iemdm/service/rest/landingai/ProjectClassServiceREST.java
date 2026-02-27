package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ProjectClassDTO;
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
 * REST Service for Project Class operations in the API Layer. Uses RestTemplate to call Operational
 * Layer endpoints.
 *
 * <p>Requirements: 23.3, 23.5
 */
@Service
@Slf4j
public class ProjectClassServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ProjectClassServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Create a new project class
   *
   * @param projectId the project ID to associate the class with
   * @param projectClass the class to create
   * @return the created class
   */
  @MethodLog
  public ProjectClass createClass(Long projectId, ProjectClass projectClass) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<ProjectClass> response =
          restTemplate.postForEntity(
              operationalServiceURI
                  + "/operational/landingai/project-classes?projectId={projectId}",
              projectClass,
              ProjectClass.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to create class: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all classes for a specific project
   *
   * @param projectId the project ID
   * @return list of class DTOs with label counts
   */
  @MethodLog
  public List<ProjectClassDTO> getClassesByProjectId(Long projectId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      ResponseEntity<ProjectClassDTO[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/project-classes/project/{projectId}",
              ProjectClassDTO[].class,
              params);

      ProjectClassDTO[] body = response.getBody();
      if (body == null) {
        log.warn("Received null body from operational layer for project {}", projectId);
        return List.of();
      }

      log.info("Retrieved {} classes for project {}", body.length, projectId);
      return Arrays.asList(body);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get classes for project {}: {}",
          projectId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing class
   *
   * @param classId the class ID to update
   * @param projectClass the updated class data
   * @return the updated class
   */
  @MethodLog
  public ProjectClass updateClass(Long classId, ProjectClass projectClass) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("classId", classId);

      ResponseEntity<ProjectClass> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/project-classes/{classId}",
              HttpMethod.PUT,
              new org.springframework.http.HttpEntity<>(projectClass),
              ProjectClass.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to update class {}: {}", classId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a class
   *
   * @param classId the class ID to delete
   */
  @MethodLog
  public void deleteClass(Long classId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("classId", classId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/project-classes/{classId}", params);
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to delete class {}: {}", classId, e.getMessage());
      throw e;
    }
  }
}
