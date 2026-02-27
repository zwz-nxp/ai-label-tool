package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.location.SapCode;
import com.nxp.iemdm.service.SapCodeService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sapcode")
public class SapCodeController {

  private final SapCodeService sapCodeService;

  @Autowired
  public SapCodeController(SapCodeService sapCodeService) {
    this.sapCodeService = sapCodeService;
  }

  @MethodLog
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public List<SapCode> getAllSapCodes() {
    return new ArrayList<>(this.sapCodeService.getAllSapCodes());
  }

  @MethodLog
  @GetMapping(path = "/{sapCodeName}", produces = MediaType.APPLICATION_JSON)
  public SapCode getSapCode(@PathVariable("sapCodeName") @NotBlank String sapCodeName) {
    return this.sapCodeService.getSapCode(sapCodeName);
  }

  @MethodLog
  @PreAuthorize("hasGlobalRole('Administrator_System')")
  @PostMapping(
      path = "/save",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public SapCode saveSapCode(
      @RequestBody SapCode sapCode, @AuthenticationPrincipal IEMDMPrincipal currentUser) {
    sapCode.setLastUpdated(Instant.now());
    sapCode.setUpdatedBy(currentUser.getUsername());
    return this.sapCodeService.saveSapCode(sapCode);
  }

  @MethodLog
  @DeleteMapping(path = "/{sapCode}")
  public void deleteSapCode(@PathVariable("sapCode") @NotBlank String sapCodeName) {
    this.sapCodeService.deleteSapCode(sapCodeName);
  }
}
