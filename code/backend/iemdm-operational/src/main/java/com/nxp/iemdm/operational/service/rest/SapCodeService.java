package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.model.location.SapCode;
import com.nxp.iemdm.operational.repository.jpa.SapCodeConsParamsRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sapcode")
public class SapCodeService {
  private final SapCodeConsParamsRepository sapCodeConsParamsRepository;
  private final UpdateService updateService;

  @Autowired
  public SapCodeService(
      SapCodeConsParamsRepository sapCodeConsParamsRepository, UpdateService updateService) {
    this.sapCodeConsParamsRepository = sapCodeConsParamsRepository;
    this.updateService = updateService;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<SapCode> getAllSapCodes() {
    return this.sapCodeConsParamsRepository.findAll();
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{sapCodeName}", produces = MediaType.APPLICATION_JSON)
  public SapCode getSapCodes(@PathVariable("sapCodeName") String sapCodeName) {
    Optional<SapCode> sapCodeOpt = this.sapCodeConsParamsRepository.findById(sapCodeName);
    return sapCodeOpt.orElse(null);
  }

  @MethodLog
  @Transactional
  @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON)
  public SapCode saveSapCode(@RequestBody SapCode sapCode) {
    return this.sapCodeConsParamsRepository.save(sapCode);
  }

  @MethodLog
  @Transactional
  @DeleteMapping(path = "/{sapCodeName}", produces = MediaType.APPLICATION_JSON)
  public void deleteSapCode(@PathVariable("sapCodeName") String sapCodeName) {
    Optional<SapCode> sapCodeOpt = this.sapCodeConsParamsRepository.findById(sapCodeName);
    if (sapCodeOpt.isPresent()) {
      this.sapCodeConsParamsRepository.delete(sapCodeOpt.get());
    }
  }

  // ------ private ---------

  @MethodLog
  @Transactional
  public void sendSapCode() {
    Update update = new Update(UpdateType.SAPCODE, 0, null, null);
    this.updateService.update(update);
  }
}
