package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ProjectMetadata;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ProjectMetadataServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ProjectMetadataServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  public List<ProjectMetadata> getMetadataByProjectId(Long projectId) {
    String url =
        operationalServiceURI + "/operational/landingai/project-metadata/project/" + projectId;
    log.info("Calling operational service: {}", url);

    ResponseEntity<List<ProjectMetadata>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProjectMetadata>>() {});

    return response.getBody();
  }

  public ProjectMetadata getMetadataById(Long id) {
    String url = operationalServiceURI + "/operational/landingai/project-metadata/" + id;
    log.info("Calling operational service: {}", url);
    return restTemplate.getForObject(url, ProjectMetadata.class);
  }

  /**
   * Create a new metadata definition
   *
   * @param projectId the project ID to associate the metadata with
   * @param metadata the metadata to create
   * @return the created metadata
   */
  public ProjectMetadata createMetadata(Long projectId, ProjectMetadata metadata) {
    String url =
        operationalServiceURI + "/operational/landingai/project-metadata?projectId=" + projectId;
    log.info("Calling operational service: {}", url);
    return restTemplate.postForObject(url, metadata, ProjectMetadata.class);
  }

  public ProjectMetadata updateMetadata(Long id, ProjectMetadata metadata) {
    String url = operationalServiceURI + "/operational/landingai/project-metadata/" + id;
    log.info("Calling operational service: {}", url);

    HttpEntity<ProjectMetadata> request = new HttpEntity<>(metadata);
    ResponseEntity<ProjectMetadata> response =
        restTemplate.exchange(url, HttpMethod.PUT, request, ProjectMetadata.class);

    return response.getBody();
  }

  public void deleteMetadata(Long id) {
    String url = operationalServiceURI + "/operational/landingai/project-metadata/" + id;
    log.info("Calling operational service: {}", url);
    restTemplate.delete(url);
  }
}
