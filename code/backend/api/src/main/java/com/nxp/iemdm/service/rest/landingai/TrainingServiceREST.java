package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.service.TrainingService;
import com.nxp.iemdm.shared.dto.landingai.ModelConfigDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRecordDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRequest;
import com.nxp.iemdm.shared.dto.landingai.TrainingStatusDTO;
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
import org.springframework.web.util.UriComponentsBuilder;

/** REST implementation of TrainingService that calls the operational service layer. */
@Slf4j
@Service
public class TrainingServiceREST implements TrainingService {

  private final RestTemplate restTemplate;
  private final String trainingServiceUri;

  @Autowired
  public TrainingServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.trainingservice.uri:http://localhost:8080}") String trainingServiceUri) {
    this.restTemplate = restTemplate;
    this.trainingServiceUri = trainingServiceUri;
  }

  @Override
  @Deprecated
  public TrainingRecordDTO startTraining(TrainingRequest request, String userId) {

    log.info(
        "REST Service: Starting training: projectId={}, isCustom={}, user={}",
        request.getProjectId(),
        request.isCustomTraining(),
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(trainingServiceUri + "/landingai/training/start")
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<TrainingRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<TrainingRecordDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, TrainingRecordDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public List<TrainingRecordDTO> startMultiConfigTraining(TrainingRequest request, String userId) {

    log.info(
        "REST Service: Starting multi-config training: projectId={}, configCount={}, user={}",
        request.getProjectId(),
        request.getModelConfigs() != null ? request.getModelConfigs().size() : 0,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(trainingServiceUri + "/landingai/training/start-multi")
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<TrainingRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<List<TrainingRecordDTO>> responseEntity =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            new ParameterizedTypeReference<List<TrainingRecordDTO>>() {});

    return responseEntity.getBody();
  }

  @Override
  public TrainingRecordDTO createTrainingRecord(
      Long projectId, Long snapshotId, ModelConfigDTO modelConfig, String userId) {

    log.info(
        "REST Service: Creating training record: projectId={}, modelAlias={}, user={}",
        projectId,
        modelConfig.getModelAlias(),
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(trainingServiceUri + "/landingai/training/create-record")
            .queryParam("projectId", projectId)
            .queryParam("userId", userId)
            .queryParamIfPresent("snapshotId", java.util.Optional.ofNullable(snapshotId))
            .toUriString();

    HttpEntity<ModelConfigDTO> requestEntity = new HttpEntity<>(modelConfig);

    ResponseEntity<TrainingRecordDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, TrainingRecordDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public TrainingStatusDTO getTrainingStatus(Long id) {

    log.info("REST Service: Getting training status for id: {}", id);

    String url = trainingServiceUri + "/landingai/training/" + id + "/status";

    ResponseEntity<TrainingStatusDTO> responseEntity =
        restTemplate.getForEntity(url, TrainingStatusDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public TrainingRecordDTO getTrainingRecord(Long id) {

    log.info("REST Service: Getting training record by id: {}", id);

    String url = trainingServiceUri + "/landingai/training/" + id;

    ResponseEntity<TrainingRecordDTO> responseEntity =
        restTemplate.getForEntity(url, TrainingRecordDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public void cancelTraining(Long id, String userId) {

    log.info("REST Service: Cancelling training: id={}, user={}", id, userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                trainingServiceUri + "/landingai/training/" + id + "/cancel")
            .queryParam("userId", userId)
            .toUriString();

    restTemplate.delete(url);
  }
}
