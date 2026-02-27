package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.service.ModelParamService;
import com.nxp.iemdm.shared.dto.landingai.ModelParamCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ModelParamDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelParamUpdateRequest;
import java.net.URI;
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

/**
 * REST implementation of ModelParamService that calls the operational service layer. Validates:
 * Requirements 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1
 */
@Slf4j
@Service
public class ModelParamServiceREST implements ModelParamService {

  private final RestTemplate restTemplate;
  private final String modelParamServiceUri;

  @Autowired
  public ModelParamServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.modelparamservice.uri}") String modelParamServiceUri) {
    this.restTemplate = restTemplate;
    this.modelParamServiceUri = modelParamServiceUri;
  }

  @Override
  public List<ModelParamDTO> getModelParamsByLocation(Long locationId) {
    log.info("REST Service: Getting model parameters for location: {}", locationId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params")
            .queryParam("locationId", locationId)
            .toUriString();

    ResponseEntity<ModelParamDTO[]> responseEntity =
        restTemplate.getForEntity(url, ModelParamDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public List<ModelParamDTO> getModelParamsByLocationAndType(Long locationId, String modelType) {
    log.info(
        "REST Service: Getting model parameters for location: {} and type: {}",
        locationId,
        modelType);

    URI uri =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params")
            .queryParam("locationId", locationId)
            .queryParam("modelType", modelType)
            .build()
            .toUri();

    ResponseEntity<ModelParamDTO[]> responseEntity =
        restTemplate.getForEntity(uri, ModelParamDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public List<ModelParamDTO> searchModelParamsByName(Long locationId, String modelName) {
    log.info(
        "REST Service: Searching model parameters for location: {} with name: {}",
        locationId,
        modelName);

    URI uri =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params/search")
            .queryParam("locationId", locationId)
            .queryParam("modelName", modelName)
            .build()
            .toUri();

    ResponseEntity<ModelParamDTO[]> responseEntity =
        restTemplate.getForEntity(uri, ModelParamDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public ModelParamDTO getModelParamById(Long id) {
    log.info("REST Service: Getting model parameter by id: {}", id);

    String url = modelParamServiceUri + "/operational/landingai/model-params/" + id;

    ResponseEntity<ModelParamDTO> responseEntity =
        restTemplate.getForEntity(url, ModelParamDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public ModelParamDTO createModelParam(
      ModelParamCreateRequest request, Long locationId, String userId) {
    log.info(
        "REST Service: Creating model parameter: name={}, type={}, location={}, user={}",
        request.getModelName(),
        request.getModelType(),
        locationId,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params")
            .queryParam("locationId", locationId)
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<ModelParamCreateRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<ModelParamDTO> responseEntity =
        restTemplate.postForEntity(url, requestEntity, ModelParamDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public ModelParamDTO updateModelParam(Long id, ModelParamUpdateRequest request, String userId) {
    log.info(
        "REST Service: Updating model parameter: id={}, name={}, type={}, user={}",
        id,
        request.getModelName(),
        request.getModelType(),
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params/" + id)
            .queryParam("userId", userId)
            .toUriString();

    HttpEntity<ModelParamUpdateRequest> requestEntity = new HttpEntity<>(request);

    ResponseEntity<ModelParamDTO> responseEntity =
        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ModelParamDTO.class);

    return responseEntity.getBody();
  }

  @Override
  public void deleteModelParam(Long id, String userId) {
    log.info("REST Service: Deleting model parameter: id={}, user={}", id, userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                modelParamServiceUri + "/operational/landingai/model-params/" + id)
            .queryParam("userId", userId)
            .toUriString();

    restTemplate.delete(url);
  }
}
