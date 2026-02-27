package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.notification.HomePageCount;
import com.nxp.iemdm.service.ConfigurationValueService;
import com.nxp.iemdm.service.HomePageCountService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.ws.rs.core.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomePageCountController {
  private final HomePageCountService homePageCountService;
  private final ConfigurationValueService configurationValueService;

  public HomePageCountController(
      HomePageCountService homePageCountService,
      ConfigurationValueService configurationValueService) {
    super();
    this.homePageCountService = homePageCountService;
    this.configurationValueService = configurationValueService;
  }

  @MethodLog
  @GetMapping(path = "/homepagecount", produces = MediaType.APPLICATION_JSON)
  public HomePageCount getHomepageCount(@AuthenticationPrincipal IEMDMPrincipal user) {
    return this.homePageCountService.getHomePageCount(
        user.getUsername(), this.configurationValueService.getMaxNotifications());
  }
}
