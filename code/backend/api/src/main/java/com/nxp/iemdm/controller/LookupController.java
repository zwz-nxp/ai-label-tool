package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.LocalLookupData;
import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.consumption.LookupTable;
import com.nxp.iemdm.service.LookupTableService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LookupController {
  private final LookupTableService lookupTableService;

  @Autowired
  public LookupController(LookupTableService lookupTableService) {
    this.lookupTableService = lookupTableService;
  }

  @MethodLog
  @GetMapping(path = "/lookuptables/all", produces = MediaType.APPLICATION_JSON)
  public Map<String, List<LookupTable>> getLookupTables() {
    return this.lookupTableService.getLookupTables();
  }

  @MethodLog
  @GetMapping(path = "/lookupdata/global", produces = MediaType.APPLICATION_JSON)
  public GlobalLookupData getLookupData() {
    return this.lookupTableService.getGlobalLookupData();
  }

  @MethodLog
  @GetMapping(path = "/lookupdata/local/{locationId}", produces = MediaType.APPLICATION_JSON)
  public LocalLookupData getLookupUsernames(@PathVariable("locationId") Integer locationId) {
    return this.lookupTableService.getLocalLookupData(locationId);
  }
}
