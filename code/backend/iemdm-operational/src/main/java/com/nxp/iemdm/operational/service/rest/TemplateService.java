package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.Template;
import com.nxp.iemdm.operational.repository.jpa.TemplateRepository;
import jakarta.ws.rs.core.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template")
public class TemplateService {

  private final TemplateRepository templateRepository;

  public TemplateService(TemplateRepository templateRepository) {
    this.templateRepository = templateRepository;
  }

  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<Template> getAllTemplates() {
    return templateRepository.findAll();
  }

  @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON)
  public Template getTemplate(@PathVariable("name") String name) throws NotFoundException {
    return templateRepository.findById(name).orElseThrow(NotFoundException::new);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public Template saveTemplate(@RequestBody Template template) {
    return templateRepository.save(template);
  }
}
