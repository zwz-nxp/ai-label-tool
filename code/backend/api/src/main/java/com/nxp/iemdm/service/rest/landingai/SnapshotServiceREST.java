package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.service.SnapshotService;
import com.nxp.iemdm.shared.dto.landingai.CreateProjectFromSnapshotRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.SnapshotDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotPreviewStatsDTO;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** REST implementation of SnapshotService that calls the operational service layer. */
@Slf4j
@Service
public class SnapshotServiceREST implements SnapshotService {

  private final RestTemplate restTemplate;
  private final String snapshotServiceUri;

  @Autowired
  public SnapshotServiceREST(
      RestTemplate restTemplate, @Value("${rest.snapshotservice.uri}") String snapshotServiceUri) {
    this.restTemplate = restTemplate;
    this.snapshotServiceUri = snapshotServiceUri;
  }

  @Override
  public SnapshotDTO createSnapshot(SnapshotCreateRequest request, String userId) {

    log.info(
        "REST Service: Creating snapshot: name={}, projectId={}, user={}",
        request.getSnapshotName(),
        request.getProjectId(),
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(snapshotServiceUri + "/operational/landingai/snapshots")
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<SnapshotCreateRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<SnapshotDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, SnapshotDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public List<SnapshotDTO> getSnapshotsForProject(Long projectId) {

    log.info("REST Service: Getting snapshots for project: {}", projectId);

    String url = snapshotServiceUri + "/operational/landingai/snapshots/project/" + projectId;

    ResponseEntity<SnapshotDTO[]> responseEntity =
        restTemplate.getForEntity(url, SnapshotDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public SnapshotDTO getSnapshotById(Long id) {

    log.info("REST Service: Getting snapshot by id: {}", id);

    String url = snapshotServiceUri + "/operational/landingai/snapshots/" + id;

    ResponseEntity<SnapshotDTO> responseEntity = restTemplate.getForEntity(url, SnapshotDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public void deleteSnapshot(Long id, String userId) {

    log.info("REST Service: Deleting snapshot: id={}, user={}", id, userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                snapshotServiceUri + "/operational/landingai/snapshots/" + id)
            .queryParam("userId", userId)
            .toUriString();

    restTemplate.delete(url);
  }

  @Override
  public SnapshotPreviewStatsDTO getSnapshotPreviewStats(Long projectId) {

    log.info("REST Service: Getting snapshot preview stats for project: {}", projectId);

    String url =
        snapshotServiceUri
            + "/operational/landingai/snapshots/project/"
            + projectId
            + "/preview-stats";

    ResponseEntity<SnapshotPreviewStatsDTO> responseEntity =
        restTemplate.getForEntity(url, SnapshotPreviewStatsDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public Object getSnapshotImages(
      Long snapshotId,
      int page,
      int size,
      String sortBy,
      com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest) {

    log.info(
        "REST Service: Getting snapshot images: snapshotId={}, page={}, size={}, sortBy={}, filters={}",
        snapshotId,
        page,
        size,
        sortBy,
        filterRequest);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                snapshotServiceUri
                    + "/operational/landingai/snapshots/"
                    + snapshotId
                    + "/images/search")
            .queryParam("page", page)
            .queryParam("size", size)
            .queryParam("sortBy", sortBy)
            .toUriString();

    // Send filter request in body
    HttpEntity<com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest> requestEntity =
        filterRequest != null ? new HttpEntity<>(filterRequest) : new HttpEntity<>(null);

    ResponseEntity<Object> responseEntity =
        restTemplate.postForEntity(url, requestEntity, Object.class);

    return responseEntity.getBody();
  }

  @Override
  public ProjectDTO createProjectFromSnapshot(Long snapshotId, String projectName, String userId) {

    log.info(
        "REST Service: Creating project from snapshot: snapshotId={}, projectName={}, user={}",
        snapshotId,
        projectName,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                snapshotServiceUri
                    + "/operational/landingai/snapshots/"
                    + snapshotId
                    + "/create-project")
            .queryParam("userId", userId)
            .toUriString();

    CreateProjectFromSnapshotRequest request = new CreateProjectFromSnapshotRequest(projectName);
    HttpEntity<CreateProjectFromSnapshotRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<ProjectDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, ProjectDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public void revertProjectToSnapshot(Long snapshotId, Long projectId, String userId) {

    log.info(
        "REST Service: Reverting project to snapshot: snapshotId={}, projectId={}, user={}",
        snapshotId,
        projectId,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/revert")
            .queryParam("projectId", projectId)
            .queryParam("userId", userId)
            .toUriString();

    restTemplate.postForEntity(url, null, Void.class);
  }

  @Override
  public Object getSnapshotClasses(Long snapshotId) {

    log.info("REST Service: Getting snapshot classes: snapshotId={}", snapshotId);

    String url = snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/classes";

    ResponseEntity<Object> responseEntity = restTemplate.getForEntity(url, Object.class);

    return responseEntity.getBody();
  }

  @Override
  public Object getSnapshotTags(Long snapshotId) {

    log.info("REST Service: Getting snapshot tags: snapshotId={}", snapshotId);

    String url = snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/tags";

    ResponseEntity<Object> responseEntity = restTemplate.getForEntity(url, Object.class);

    return responseEntity.getBody();
  }

  @Override
  public Object getSnapshotMetadata(Long snapshotId) {

    log.info("REST Service: Getting snapshot metadata: snapshotId={}", snapshotId);

    String url =
        snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/metadata";

    ResponseEntity<Object> responseEntity = restTemplate.getForEntity(url, Object.class);

    return responseEntity.getBody();
  }

  @Override
  public Object getSnapshotSplits(Long snapshotId) {

    log.info("REST Service: Getting snapshot splits: snapshotId={}", snapshotId);

    String url = snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/splits";

    ResponseEntity<Object> responseEntity = restTemplate.getForEntity(url, Object.class);

    return responseEntity.getBody();
  }

  @Override
  public byte[] downloadSnapshotDataset(Long snapshotId) {
    log.info("REST Service: Downloading snapshot dataset: snapshotId={}", snapshotId);

    String url =
        snapshotServiceUri + "/operational/landingai/snapshots/" + snapshotId + "/export-dataset";

    ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);

    return responseEntity.getBody();
  }
}
