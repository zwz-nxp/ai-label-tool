package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import com.nxp.iemdm.operational.repository.jpa.GlossaryItemRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/glossary")
public class GlossaryService {

  private final GlossaryItemRepository glossaryItemRepository;

  @Autowired
  public GlossaryService(GlossaryItemRepository glossaryItemRepository) {
    this.glossaryItemRepository = glossaryItemRepository;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<GlossaryItem> getAllGlossaryItems() {
    return glossaryItemRepository.findAll();
  }

  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public GlossaryItem saveGlossaryItem(@RequestBody GlossaryItem glossaryItem) {
    glossaryItem = glossaryItemRepository.save(glossaryItem);
    return glossaryItem;
  }
}
