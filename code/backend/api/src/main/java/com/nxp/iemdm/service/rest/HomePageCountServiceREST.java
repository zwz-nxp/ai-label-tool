package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.notification.HomePageCount;
import com.nxp.iemdm.service.HomePageCountService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HomePageCountServiceREST implements HomePageCountService {
  private final RestTemplate restTemplate;
  private final String notificationServiceUri;

  @Autowired
  public HomePageCountServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.notificationservice.uri}") String notificationServiceUri) {
    this.restTemplate = restTemplate;
    this.notificationServiceUri = notificationServiceUri;
  }

  @Override
  public HomePageCount getHomePageCount(String wbi, String maxNotifications) {
    Map<String, Object> params = new HashMap<>();
    params.put("wbi", wbi);
    params.put("maxNotifications", maxNotifications);

    ResponseEntity<HomePageCount> responseEntity =
        this.restTemplate.getForEntity(
            notificationServiceUri + "/homepagecount/{wbi}/{maxNotifications}",
            HomePageCount.class,
            params);
    return responseEntity.getBody();
  }
}
