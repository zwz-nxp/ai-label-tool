package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.Template;
import com.nxp.iemdm.service.TemplateService;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/template")
public class TemplateController {

  private final TemplateService templateService;

  @Autowired
  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<Template> getAllTemplates() {
    return templateService.getAllTemplates();
  }

  @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON)
  public Template getTemplate(@PathVariable("name") String name) {
    return templateService.getTemplate(name);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public Template saveTemplate(
      @RequestBody @Valid Template template, @AuthenticationPrincipal IEMDMPrincipal user) {
    template.setUpdatedBy(user.getUsername());
    template.setLastUpdated(Instant.now());

    return templateService.saveTemplate(template);
  }
}
