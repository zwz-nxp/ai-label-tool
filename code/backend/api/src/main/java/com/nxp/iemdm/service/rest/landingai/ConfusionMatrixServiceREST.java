package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.shared.dto.landingai.CellDetailResponse;
import com.nxp.iemdm.shared.dto.landingai.ConfusionMatrixResponse;
import com.nxp.iemdm.shared.dto.landingai.ImageWithLabelsDTO;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST service for confusion matrix operations. Calls the operational service layer (Port 8081) via
 * REST.
 */
@Slf4j
@Service
public class ConfusionMatrixServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ConfusionMatrixServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Calculate confusion matrix for a model and evaluation set.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return Confusion matrix response
   */
  public ConfusionMatrixResponse calculateConfusionMatrix(Long modelId, String evaluationSet) {
    log.info(
        "REST Service: Calculating confusion matrix for modelId={}, evaluationSet={}",
        modelId,
        evaluationSet);

    String url =
        operationalServiceURI
            + "/operational/landingai/confusion-matrix/"
            + modelId
            + "/"
            + evaluationSet;

    ResponseEntity<ConfusionMatrixResponse> responseEntity =
        restTemplate.getForEntity(url, ConfusionMatrixResponse.class);

    return responseEntity.getBody();
  }

  /**
   * Get detail for a specific cell (GT×Pred combination).
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @param gtClassId Ground truth class ID
   * @param predClassId Prediction class ID
   * @return Cell detail response with images
   */
  public CellDetailResponse getCellDetail(
      Long modelId, String evaluationSet, Long gtClassId, Long predClassId) {
    log.info(
        "REST Service: Getting cell detail for modelId={}, evaluationSet={}, gtClassId={}, predClassId={}",
        modelId,
        evaluationSet,
        gtClassId,
        predClassId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI
                    + "/operational/landingai/confusion-matrix/"
                    + modelId
                    + "/"
                    + evaluationSet
                    + "/cell")
            .queryParam("gtClassId", gtClassId)
            .queryParam("predClassId", predClassId)
            .toUriString();

    ResponseEntity<CellDetailResponse> responseEntity =
        restTemplate.getForEntity(url, CellDetailResponse.class);

    return responseEntity.getBody();
  }

  /**
   * Get all images in evaluation set with correctness indicators.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of images with ground truth and prediction labels
   */
  public List<ImageWithLabelsDTO> getAllImages(Long modelId, String evaluationSet) {
    log.info(
        "REST Service: Getting all images for modelId={}, evaluationSet={}",
        modelId,
        evaluationSet);

    String url =
        operationalServiceURI
            + "/operational/landingai/confusion-matrix/"
            + modelId
            + "/"
            + evaluationSet
            + "/all-images";

    ResponseEntity<ImageWithLabelsDTO[]> responseEntity =
        restTemplate.getForEntity(url, ImageWithLabelsDTO[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  /**
   * Get prediction labels for a model filtered by evaluation set. Backend 根據 evaluationSet 參數過濾資料，
   * 只回傳指定 evaluation set 的資料。
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of prediction labels for the specified evaluation set
   */
  public List<?> getPredictionLabels(Long modelId, String evaluationSet) {
    log.info(
        "REST Service: Getting prediction labels for modelId={}, evaluationSet={}",
        modelId,
        evaluationSet);

    String url =
        operationalServiceURI
            + "/operational/landingai/confusion-matrix/"
            + modelId
            + "/"
            + evaluationSet
            + "/prediction-labels";

    ResponseEntity<Object[]> responseEntity = restTemplate.getForEntity(url, Object[].class);

    return Arrays.asList(responseEntity.getBody());
  }

  /**
   * Get ground truth labels for a model's project filtered by evaluation set. Backend 根據
   * evaluationSet 參數過濾資料，只回傳指定 evaluation set 的資料。
   *
   * @param modelId Model ID (用來找到對應的 snapshot)
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of ground truth labels for the specified evaluation set
   */
  public List<?> getGroundTruthLabels(Long modelId, String evaluationSet) {
    log.info(
        "REST Service: Getting ground truth labels for modelId={}, evaluationSet={}",
        modelId,
        evaluationSet);

    String url =
        operationalServiceURI
            + "/operational/landingai/confusion-matrix/"
            + modelId
            + "/"
            + evaluationSet
            + "/ground-truth-labels";

    ResponseEntity<Object[]> responseEntity = restTemplate.getForEntity(url, Object[].class);

    return Arrays.asList(responseEntity.getBody());
  }
}
