package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.service.NotificationService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceREST implements NotificationService {
  private final RestTemplate restTemplate;
  private final String notificationServiceUri;

  @Autowired
  public NotificationServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.notificationservice.uri}") String notificationServiceUri) {
    this.restTemplate = restTemplate;
    this.notificationServiceUri = notificationServiceUri;
  }

  @MethodLog
  @Override
  public List<Notification> getAllUnreadNotificationsForUser(String wbi) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);

    ResponseEntity<Notification[]> responseEntity =
        this.restTemplate.getForEntity(
            notificationServiceUri + "/notification/unread/{wbi}", Notification[].class, params);

    List<Notification> notifications = Arrays.asList(responseEntity.getBody());
    return notifications.stream()
        .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
        .collect(Collectors.toList());
  }

  @MethodLog
  @Override
  public List<Notification> getAllNotificationsForUser(String wbi) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);

    ResponseEntity<Notification[]> responseEntity =
        this.restTemplate.getForEntity(
            notificationServiceUri + "/notification/all/{wbi}", Notification[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public Notification saveNotification(Notification notification) {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Notification> responseEntity =
        this.restTemplate.postForEntity(
            notificationServiceUri + "/notification", notification, Notification.class, params);

    return responseEntity.getBody();
  }

  @Override
  public List<Notification> saveAllNotifications(List<Notification> notifications) {
    Map<String, Object> params = new HashMap<>();

    ResponseEntity<Notification[]> responseEntity =
        this.restTemplate.postForEntity(
            notificationServiceUri + "/notification/readAll",
            notifications,
            Notification[].class,
            params);

    return Arrays.asList(responseEntity.getBody());
  }

  @MethodLog
  @Override
  public String createWeeklyNotification(String wbi, Integer locationId) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);
    params.put("location", locationId);

    ResponseEntity<String> responseEntity =
        this.restTemplate.getForEntity(
            notificationServiceUri + "/notification/weekly/{wbi}/{location}", String.class, params);

    return responseEntity.getBody();
  }
}
