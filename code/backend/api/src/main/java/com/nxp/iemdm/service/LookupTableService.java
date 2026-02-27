package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.LocalLookupData;
import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.consumption.LookupTable;
import java.util.List;
import java.util.Map;

public interface LookupTableService {
  Map<String, List<LookupTable>> getLookupTables();

  GlobalLookupData getGlobalLookupData();

  LocalLookupData getLocalLookupData(Integer locationId);
}
