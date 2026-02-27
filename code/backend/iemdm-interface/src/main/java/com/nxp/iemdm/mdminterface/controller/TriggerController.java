package com.nxp.iemdm.mdminterface.controller;

import com.nxp.iemdm.mdminterface.service.TriggerService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trigger")
public class TriggerController {

  private final TriggerService triggerService;

  public TriggerController(TriggerService triggerService) {
    this.triggerService = triggerService;
  }

  @MethodLog
  @GetMapping(
      path = "/registerTibcoJob/{tibcoJobType}/{userWbi}",
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<String> registerTibcoJob(
      @PathVariable("tibcoJobType") String tibcoJobType,
      @PathVariable("userWbi") String triggeredByWbi) {
    if (this.triggerService.enqueueJob(tibcoJobType, triggeredByWbi)) {
      return ResponseEntity.ok().body(String.format("%s enqueued and run", tibcoJobType));
    }
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Job already enqueued!");
  }
}
