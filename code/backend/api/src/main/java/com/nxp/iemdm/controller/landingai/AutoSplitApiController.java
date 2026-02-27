package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.rest.landingai.AutoSplitServiceREST;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitRequestDTO;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/landingai/auto-split")
@RequiredArgsConstructor
public class AutoSplitApiController {

  private final AutoSplitServiceREST autoSplitServiceREST;

  @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AutoSplitStatsDTO> getAutoSplitStats(
      @RequestParam Long projectId, @RequestParam(defaultValue = "false") Boolean includeAssigned) {
    log.info(
        "GET /api/landingai/auto-split/stats - projectId: {}, includeAssigned: {}",
        projectId,
        includeAssigned);

    AutoSplitStatsDTO stats = autoSplitServiceREST.getAutoSplitStats(projectId, includeAssigned);
    return ResponseEntity.ok(stats);
  }

  @PostMapping(
      value = "/assign",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Integer> assignSplits(@RequestBody AutoSplitRequestDTO request) {
    log.info("POST /api/landingai/auto-split/assign - projectId: {}", request.getProjectId());

    Integer updatedCount = autoSplitServiceREST.assignSplits(request);
    return ResponseEntity.ok(updatedCount);
  }
}
