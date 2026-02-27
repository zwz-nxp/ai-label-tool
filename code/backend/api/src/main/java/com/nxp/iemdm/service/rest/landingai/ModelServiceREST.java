package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.service.ModelService;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelRequest;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelResponse;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** REST implementation of ModelService that calls the operational service layer. */
@Slf4j
@Service
public class ModelServiceREST implements ModelService {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ModelServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  @Override
  public List<ModelWithMetricsDto> getAllModels() {
    log.info("REST Service: Getting all models");

    String url = operationalServiceURI + "/operational/landingai/models";

    ResponseEntity<ModelWithMetricsDto[]> responseEntity =
        restTemplate.getForEntity(url, ModelWithMetricsDto[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public Page<Model> getModelsPage(Pageable pageable) {
    log.info("REST Service: Getting models page: {}", pageable);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/models/page")
            .queryParam("page", pageable.getPageNumber())
            .queryParam("size", pageable.getPageSize())
            .toUriString();

    ResponseEntity<Page<Model>> responseEntity =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<Page<Model>>() {});

    return responseEntity.getBody();
  }

  @Override
  public ModelWithMetricsDto getModelById(Long id) {
    log.info("REST Service: Getting model by id: {}", id);

    String url = operationalServiceURI + "/operational/landingai/models/" + id;

    ResponseEntity<ModelWithMetricsDto> responseEntity =
        restTemplate.getForEntity(url, ModelWithMetricsDto.class);

    return responseEntity.getBody();
  }

  @Override
  public List<ModelWithMetricsDto> getModelsByProjectId(Long projectId) {
    log.info("REST Service: Getting models for project id: {}", projectId);

    String url = operationalServiceURI + "/operational/landingai/models/project/" + projectId;

    ResponseEntity<ModelWithMetricsDto[]> responseEntity =
        restTemplate.getForEntity(url, ModelWithMetricsDto[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public ModelWithMetricsDto toggleFavorite(Long id) {
    log.info("REST Service: Toggling favorite for model id: {}", id);

    String url = operationalServiceURI + "/operational/landingai/models/" + id + "/favorite";

    ResponseEntity<ModelWithMetricsDto> responseEntity =
        restTemplate.exchange(url, HttpMethod.PUT, null, ModelWithMetricsDto.class);

    return responseEntity.getBody();
  }

  @Override
  public List<ModelWithMetricsDto> searchModels(
      String query, Boolean favoritesOnly, Long projectId) {
    log.info(
        "REST Service: Searching models - query: {}, favoritesOnly: {}, projectId: {}",
        query,
        favoritesOnly,
        projectId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/models/search")
            .queryParam("query", query)
            .queryParam("favoritesOnly", favoritesOnly)
            .queryParam("projectId", projectId)
            .toUriString();

    ResponseEntity<ModelWithMetricsDto[]> responseEntity =
        restTemplate.getForEntity(url, ModelWithMetricsDto[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public void deleteModel(Long id) {
    log.info("REST Service: Deleting model id: {}", id);

    String url = operationalServiceURI + "/operational/landingai/models/" + id;

    restTemplate.delete(url);
  }

  @Override
  public List<?> getPredictionLabels(Long modelId) {
    log.info("REST Service: Getting all prediction labels for model id: {}", modelId);

    String url =
        operationalServiceURI + "/operational/landingai/models/" + modelId + "/prediction-labels";

    ResponseEntity<List> responseEntity = restTemplate.getForEntity(url, List.class);

    return responseEntity.getBody();
  }

  @Override
  public GenerateModelResponse generateModelWithNewThreshold(
      Long sourceModelId, GenerateModelRequest request) {
    log.info(
        "REST Service: Generating new model from source: {} with threshold: {}",
        sourceModelId,
        request.getNewThreshold());

    String url =
        operationalServiceURI
            + "/operational/landingai/models/"
            + sourceModelId
            + "/generate-with-threshold";

    ResponseEntity<GenerateModelResponse> responseEntity =
        restTemplate.postForEntity(url, request, GenerateModelResponse.class);

    return responseEntity.getBody();
  }
}
