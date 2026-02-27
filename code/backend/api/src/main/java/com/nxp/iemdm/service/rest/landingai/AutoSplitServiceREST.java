package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.shared.dto.landingai.AutoSplitRequestDTO;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AutoSplitServiceREST {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public AutoSplitServiceREST(
      RestTemplate restTemplate, @Value("${rest.iemdm-services.uri}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public AutoSplitStatsDTO getAutoSplitStats(Long projectId, Boolean includeAssigned) {
    String url =
        baseUrl
            + "/operational/landingai/auto-split/stats?projectId="
            + projectId
            + "&includeAssigned="
            + includeAssigned;
    log.info("Calling operational service: GET {}", url);

    ResponseEntity<AutoSplitStatsDTO> response =
        restTemplate.getForEntity(url, AutoSplitStatsDTO.class);
    return response.getBody();
  }

  public Integer assignSplits(AutoSplitRequestDTO request) {
    String url = baseUrl + "/operational/landingai/auto-split/assign";
    log.info("Calling operational service: POST {}", url);

    ResponseEntity<Integer> response = restTemplate.postForEntity(url, request, Integer.class);
    return response.getBody();
  }
}
