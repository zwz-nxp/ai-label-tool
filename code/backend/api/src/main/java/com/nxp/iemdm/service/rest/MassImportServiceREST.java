package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.service.MassImportService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log
@Service
public class MassImportServiceREST implements MassImportService {

  private final RestTemplate restTemplate;
  private final String servicesUri;

  public MassImportServiceREST(
      RestTemplate restTemplate, @Value("${rest.iemdm-services.uri}") String servicesUri) {
    super();
    this.restTemplate = restTemplate;
    this.servicesUri = servicesUri;
  }
}
