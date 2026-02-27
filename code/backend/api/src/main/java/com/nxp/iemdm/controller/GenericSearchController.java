package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.model.search.GenericSearchArguments;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.service.GenericSearchService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genericSearch")
public class GenericSearchController {
  private final GenericSearchService genericSearchService;

  public GenericSearchController(GenericSearchService genericSearchService) {
    this.genericSearchService = genericSearchService;
  }

  @MethodLog
  @PostMapping(
      path = "/person",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Person> searchPerson(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArgs) {

    return this.genericSearchService.searchPerson(searchArgs, page, size);
  }

  @MethodLog
  @PostMapping(
      path = "/location",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Location> searchLocation(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArgs) {

    return this.genericSearchService.searchLocation(searchArgs, page, size);
  }

  @MethodLog
  @PostMapping(
      path = "/manufacturer",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Manufacturer> searchManufacturerCodes(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArgs) {

    return this.genericSearchService.searchManufacturer(searchArgs, page, size);
  }
}
