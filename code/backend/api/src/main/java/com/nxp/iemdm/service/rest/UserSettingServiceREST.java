package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.user.UserSetting;
import com.nxp.iemdm.service.UserSettingService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserSettingServiceREST implements UserSettingService {

  private final RestTemplate restTemplate;
  private final String userSettingServiceUri;

  @Autowired
  public UserSettingServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.usersettingservice.uri}") String userSettingServiceUri) {
    this.restTemplate = restTemplate;
    this.userSettingServiceUri = userSettingServiceUri;
  }

  @Override
  public List<UserSetting> getAllSettingsForUser(String wbi) {
    Map<String, Object> params = new HashMap<>();

    params.put("wbi", wbi);

    ResponseEntity<UserSetting[]> responseEntity =
        this.restTemplate.getForEntity(
            userSettingServiceUri + "/usersetting/{wbi}", UserSetting[].class, params);

    return Arrays.asList(responseEntity.getBody());
  }

  @Override
  public UserSetting getUserSettingByUserWbiAndUserSettingKey(String wbi, String userSettingKey) {
    ResponseEntity<UserSetting> responseEntity =
        this.restTemplate.getForEntity(
            userSettingServiceUri + "/usersetting/{wbi}/{key}",
            UserSetting.class,
            Map.of("wbi", wbi, "key", userSettingKey));

    return responseEntity.getBody();
  }

  @Override
  public UserSetting saveUserSetting(UserSetting userSetting) {
    Map<String, Object> params = new HashMap<>();
    params.put("userSetting", userSetting);

    ResponseEntity<UserSetting> responseEntity =
        this.restTemplate.postForEntity(
            userSettingServiceUri + "/usersetting", userSetting, UserSetting.class, params);

    return responseEntity.getBody();
  }
}
