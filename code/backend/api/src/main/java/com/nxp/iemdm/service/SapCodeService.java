package com.nxp.iemdm.service;

import com.nxp.iemdm.model.location.SapCode;
import java.util.List;

public interface SapCodeService {
  List<SapCode> getAllSapCodes();

  SapCode getSapCode(String sapCodeName);

  SapCode saveSapCode(SapCode sapCode);

  void deleteSapCode(String sapCodeName);
}
