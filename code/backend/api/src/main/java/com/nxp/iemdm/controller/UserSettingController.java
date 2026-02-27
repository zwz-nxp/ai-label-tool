package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserSetting;
import com.nxp.iemdm.service.ConfigurationValueService;
import com.nxp.iemdm.service.UserSettingService;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usersetting")
public class UserSettingController {

  private final UserSettingService userSettingService;
  private final ConfigurationValueService configurationValueService;

  @Autowired
  public UserSettingController(
      UserSettingService userSettingService, ConfigurationValueService configurationValueService) {
    this.userSettingService = userSettingService;
    this.configurationValueService = configurationValueService;
  }

  @GetMapping(path = "/{wbi}/{setting}", produces = MediaType.APPLICATION_JSON)
  public UserSetting getSettingForUser(
      @PathVariable("wbi") String wbi, @PathVariable("setting") String setting) {
    try {
      return userSettingService.getUserSettingByUserWbiAndUserSettingKey(wbi, setting);
    } catch (Exception notFoundException) {
      return null;
    }
  }

  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<UserSetting> getAllSettingsForUser(@AuthenticationPrincipal IEMDMPrincipal user) {
    List<UserSetting> userSettings = userSettingService.getAllSettingsForUser(user.getUsername());

    if (this.containsKey(userSettings, "debouncetime")) {
      return userSettings;
    }
    List<UserSetting> usArray = new ArrayList<>(userSettings);

    UserSetting debounceTimeConfigSetting = new UserSetting();
    Person p = new Person();
    p.setWbi(user.getUsername());

    debounceTimeConfigSetting.setUser(p);
    debounceTimeConfigSetting.setKey("debouncetime");
    debounceTimeConfigSetting.setValue(this.configurationValueService.getDebounceTime());

    usArray.add(debounceTimeConfigSetting);
    return usArray;
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
  public UserSetting saveUserSetting(@RequestBody UserSetting userSetting) {
    return userSettingService.saveUserSetting(userSetting);
  }

  public boolean containsKey(final List<UserSetting> list, final String name) {
    return list.stream().anyMatch(o -> o.getKey().equals(name));
  }
}
