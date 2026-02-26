package com.nxp.iemdm.shared.intf.operational;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import java.util.List;

public interface VersionInfoService {
  VersionInfo getServices() throws NotFoundException;

  List<VersionInfo> getAbout() throws NotFoundException;
}
