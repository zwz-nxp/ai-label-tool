package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.NxpProductionYear;
import com.nxp.iemdm.service.NxpProductionYearService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nxpproductionyear")
public class NxpProductionyearController {
  private final NxpProductionYearService nxpProductionYearService;

  @Autowired
  public NxpProductionyearController(NxpProductionYearService nxpProductionYearService) {
    this.nxpProductionYearService = nxpProductionYearService;
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<NxpProductionYear> getAllNxpProductionYears() {
    return nxpProductionYearService.getAllNxpProductionYears();
  }

  @MethodLog
  @GetMapping(path = "/{year}", produces = MediaType.APPLICATION_JSON)
  public NxpProductionYear getNxpProductionYearForYear(@PathVariable("year") Integer year) {
    return nxpProductionYearService.getNxpProductionYearForYear(year);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON)
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  public NxpProductionYear saveNxpProductionYear(
      @RequestBody @Valid NxpProductionYear nxpProductionYear,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    nxpProductionYear.setUpdatedBy(user.getUsername());
    return nxpProductionYearService.saveNxpProductionyear(nxpProductionYear);
  }
}
