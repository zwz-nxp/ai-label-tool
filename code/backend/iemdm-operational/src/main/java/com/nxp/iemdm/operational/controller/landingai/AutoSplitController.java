package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.operational.service.landingai.AutoSplitService;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitRequestDTO;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/operational/landingai/auto-split")
@RequiredArgsConstructor
public class AutoSplitController {

  private final AutoSplitService autoSplitService;

  @GetMapping("/stats")
  public ResponseEntity<AutoSplitStatsDTO> getAutoSplitStats(
      @RequestParam Long projectId, @RequestParam(defaultValue = "false") Boolean includeAssigned) {
    log.info(
        "GET /operational/landingai/auto-split/stats - projectId: {}, includeAssigned: {}",
        projectId,
        includeAssigned);

    AutoSplitStatsDTO stats = autoSplitService.getAutoSplitStats(projectId, includeAssigned);
    return ResponseEntity.ok(stats);
  }

  @PostMapping("/assign")
  public ResponseEntity<Integer> assignSplits(@RequestBody AutoSplitRequestDTO request) {
    log.info(
        "POST /operational/landingai/auto-split/assign - projectId: {}", request.getProjectId());

    Integer updatedCount = autoSplitService.assignSplits(request);
    return ResponseEntity.ok(updatedCount);
  }
}
