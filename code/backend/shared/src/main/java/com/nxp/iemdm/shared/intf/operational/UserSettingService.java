package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.user.UserSetting;
import java.util.List;

public interface UserSettingService {
  List<UserSetting> getAllSettingsForUser(String wbi);

  UserSetting getSettingForUser(String wbi, String settingName) throws NotFoundException;

  UserSetting saveUserSetting(UserSetting userSetting);
}
