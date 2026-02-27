package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import java.util.List;

public interface VersionInfoService {
  VersionInfo getServicesVersionInfo();

  List<VersionInfo> getAllVersionInfos();
}
