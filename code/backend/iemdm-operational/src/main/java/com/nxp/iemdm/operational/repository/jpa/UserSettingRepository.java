package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.UserSetting;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface UserSettingRepository
    extends CrudRepository<UserSetting, UserSetting.UserSettingKey> {
  List<UserSetting> findAllByUser(Person user);
}
