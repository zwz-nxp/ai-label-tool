package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.NxpProductionYear;
import java.util.List;

public interface NxpProductionYearService {
  NxpProductionYear getNxpProductionYearForYear(Integer year);

  NxpProductionYear saveNxpProductionyear(NxpProductionYear nxpProductionYear);

  List<NxpProductionYear> getAllNxpProductionYears();
}
