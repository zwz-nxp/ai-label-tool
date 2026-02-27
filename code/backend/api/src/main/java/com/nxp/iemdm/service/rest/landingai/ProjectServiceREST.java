package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.service.ProjectService;
import com.nxp.iemdm.shared.dto.landingai.ProjectCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectUpdateRequest;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** REST implementation of ProjectService that calls the operational service layer. */
@Slf4j
@Service
public class ProjectServiceREST implements ProjectService {

  private final RestTemplate restTemplate;
  private final String projectServiceUri;

  @Autowired
  public ProjectServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.projectservice.uri:http://localhost:8080}") String projectServiceUri) {
    this.restTemplate = restTemplate;
    this.projectServiceUri = projectServiceUri;
  }

  @Override
  public List<ProjectListItemDTO> getProjectsForUser(
      String userId, Long locationId, boolean viewAll) {

    log.info(
        "REST Service: Getting projects for user: {}, location: {}, viewAll: {}",
        userId,
        locationId,
        viewAll);

    String url =
        UriComponentsBuilder.fromHttpUrl(projectServiceUri + "/operational/landingai/projects")
            .queryParam("userId", userId)
            .queryParam("locationId", locationId)
            .queryParam("viewAll", viewAll)
            .toUriString();

    ResponseEntity<ProjectListItemDTO[]> responseEntity =
        restTemplate.getForEntity(url, ProjectListItemDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public ProjectDTO createProject(ProjectCreateRequest request, String userId, Long locationId) {

    log.info(
        "REST Service: Creating project: name={}, type={}, user={}, location={}",
        request.getName(),
        request.getType(),
        userId,
        locationId);

    String url =
        UriComponentsBuilder.fromHttpUrl(projectServiceUri + "/operational/landingai/projects")
            .queryParam("userId", userId)
            .queryParam("locationId", locationId)
            .toUriString();

    HttpEntity<ProjectCreateRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<ProjectDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, ProjectDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public ProjectDTO getProjectById(Long id) {

    log.info("REST Service: Getting project by id: {}", id);

    String url = projectServiceUri + "/operational/landingai/projects/" + id;

    ResponseEntity<ProjectDTO> responseEntity = restTemplate.getForEntity(url, ProjectDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public ProjectDTO updateProject(Long id, ProjectUpdateRequest request, String userId) {

    log.info(
        "REST Service: Updating project: id={}, name={}, modelName={}, user={}",
        id,
        request.getName(),
        request.getModelName(),
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                projectServiceUri + "/operational/landingai/projects/" + id)
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<ProjectUpdateRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<ProjectDTO> responseEntity =
        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ProjectDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public void deleteProject(Long id, String userId) {

    log.info("REST Service: Deleting project: id={}, user={}", id, userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                projectServiceUri + "/operational/landingai/projects/" + id)
            .queryParam("userId", userId)
            .toUriString();

    restTemplate.delete(url);
  }
}
