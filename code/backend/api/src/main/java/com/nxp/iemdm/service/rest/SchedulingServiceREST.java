package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.scheduling.JobExecutionLogRequest;
import com.nxp.iemdm.model.scheduling.JobOverview;
import com.nxp.iemdm.shared.dto.scheduling.JobExecutionLogDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SchedulingServiceREST {
  private final RestTemplate restTemplate;
  private final String iemdmServicesUri;

  public SchedulingServiceREST(
      RestTemplate restTemplate, @Value("${rest.iemdm-services.uri}") String iemdmServicesUri) {
    this.restTemplate = restTemplate;
    this.iemdmServicesUri = iemdmServicesUri;
  }

  public List<JobOverview> getScheduledJobOverviews() {
    String uri = this.iemdmServicesUri + "scheduling/scheduled-job-overviews";
    ParameterizedTypeReference<List<JobOverview>> parameterizedTypeReference =
        new ParameterizedTypeReference<>() {};
    return this.restTemplate
        .exchange(uri, HttpMethod.GET, null, parameterizedTypeReference)
        .getBody();
  }

  public List<JobExecutionLogDto> jobExecutionLogs(JobExecutionLogRequest jobExecutionLogRequest) {
    String uri = this.iemdmServicesUri + "scheduling/job-execution-logs";
    ParameterizedTypeReference<List<JobExecutionLogDto>> parameterizedTypeReference =
        new ParameterizedTypeReference<>() {};
    return this.restTemplate
        .exchange(
            uri,
            HttpMethod.POST,
            new HttpEntity<>(jobExecutionLogRequest),
            parameterizedTypeReference)
        .getBody();
  }
}
