package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import com.nxp.iemdm.service.GlossaryService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {
  private final GlossaryService glossaryService;

  @Autowired
  public GlossaryController(GlossaryService glossaryService) {
    this.glossaryService = glossaryService;
  }

  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public GlossaryItem saveGlossaryItem(@RequestBody @Valid GlossaryItem glossaryItem) {
    return glossaryService.saveGlossaryItem(glossaryItem);
  }

  @MethodLog
  @GetMapping(
      path = "/all",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public List<GlossaryItem> getAllGlossaryItems() {
    return this.glossaryService.getAllGlossaryItems();
  }
}
