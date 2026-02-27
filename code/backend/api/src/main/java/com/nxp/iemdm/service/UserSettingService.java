package com.nxp.iemdm.service;

import com.nxp.iemdm.model.user.UserSetting;
import java.util.List;

public interface UserSettingService {
  List<UserSetting> getAllSettingsForUser(String wbi);

  UserSetting getUserSettingByUserWbiAndUserSettingKey(String wbi, String userSettingKey);

  UserSetting saveUserSetting(UserSetting userSetting);
}
