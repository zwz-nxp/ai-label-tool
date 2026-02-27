package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserSetting;
import com.nxp.iemdm.operational.repository.jpa.UserSettingRepository;
import com.nxp.iemdm.shared.intf.operational.UserSettingService;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/usersetting")
@Component
public class UserSettingServiceImpl implements UserSettingService {

  private final UserSettingRepository userSettingRepository;

  public UserSettingServiceImpl(UserSettingRepository userSettingRepository) {
    this.userSettingRepository = userSettingRepository;
  }

  @GetMapping(path = "/{wbi}", produces = MediaType.APPLICATION_JSON)
  public List<UserSetting> getAllSettingsForUser(@PathVariable("wbi") String wbi) {
    Person user = new Person();
    user.setWbi(wbi);

    return userSettingRepository.findAllByUser(user);
  }

  @GetMapping(path = "/{wbi}/{setting}", produces = MediaType.APPLICATION_JSON)
  public UserSetting getSettingForUser(
      @PathVariable("wbi") String wbi, @PathVariable("setting") String settingName)
      throws NotFoundException {
    UserSetting.UserSettingKey userSettingKey = new UserSetting.UserSettingKey(wbi, settingName);

    return userSettingRepository.findById(userSettingKey).orElseThrow(NotFoundException::new);
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
  public UserSetting saveUserSetting(@RequestBody UserSetting userSetting) {
    return userSettingRepository.save(userSetting);
  }
}
