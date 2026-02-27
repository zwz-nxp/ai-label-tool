package com.nxp.iemdm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.model.configuration.PageColumnsConfiguration;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserSetting;
import com.nxp.iemdm.service.UserSettingService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pageColumnsConfiguration")
public class PageColumnsConfigurationController {
  private final UserSettingService userSettingService;

  public PageColumnsConfigurationController(UserSettingService userSettingService) {
    this.userSettingService = userSettingService;
  }

  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public UserSetting savePageColumnsConfiguration(
      @RequestBody PageColumnsConfiguration pageColumnsConfiguration) {
    UserSetting userSetting = new UserSetting();
    userSetting.setKey(pageColumnsConfiguration.getPage());
    Person currentUser = new Person();
    currentUser.setWbi(pageColumnsConfiguration.getUser());
    userSetting.setUser(currentUser);

    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    try {
      String jsonString = mapper.writeValueAsString(pageColumnsConfiguration.getConfiguration());
      userSetting.setValue(jsonString);
      return this.userSettingService.saveUserSetting(userSetting);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new BadRequestException(
          "Failed to parse page columns configuration", jsonProcessingException);
    }
  }
}
