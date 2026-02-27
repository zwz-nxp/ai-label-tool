package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import com.nxp.iemdm.service.TrainingRecordService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** REST implementation of TrainingRecordService that calls the operational service layer. */
@Slf4j
@Service
public class TrainingRecordServiceREST implements TrainingRecordService {

  private final RestTemplate restTemplate;
  private final String trainingRecordServiceUri;

  @Autowired
  public TrainingRecordServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.trainingrecordservice.uri:http://localhost:8080}")
          String trainingRecordServiceUri) {
    this.restTemplate = restTemplate;
    this.trainingRecordServiceUri = trainingRecordServiceUri;
  }

  @Override
  public Optional<TrainingRecord> getTrainingRecordById(Long id) {
    log.info("REST Service: Getting training record by id: {}", id);

    String url = trainingRecordServiceUri + "/operational/landingai/training-records/" + id;

    try {
      ResponseEntity<TrainingRecord> responseEntity =
          restTemplate.getForEntity(url, TrainingRecord.class);

      return Optional.ofNullable(responseEntity.getBody());
    } catch (Exception e) {
      log.warn("Training record not found with id: {}", id);
      return Optional.empty();
    }
  }
}
