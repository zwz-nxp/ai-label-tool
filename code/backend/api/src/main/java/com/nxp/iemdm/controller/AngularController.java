package com.nxp.iemdm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AngularController {

  @RequestMapping(
      value = {
        "/",
        "/admin/sites",
        "/admin/plant-codes",
        "/admin/production-years",
        "/admin/resource-classes",
        "/admin/planning-flag-control",
        "/equipment/installed-base",
        "/equipment/equipment-codes",
        "/planning/downtime-calendar",
        "/planning/site-capacity",
        "/planning/resource-groups",
        "/planning/nxp1-capacity-statement",
        "/planning/nxp2-capacity-statement",
        "/planning/planning-flags",
        "/planning/llc-rg-capacity",
        "/products/consumption-parameters",
        "/products/financial-yield",
        "/products/financial-usage-rate",
        "/products/data-quality",
        "/products/mass-upload-usage-rate",
        "/products/mass-upload-planning-flow",
        "/products/test-flow-data-quality",
        "/products/activation-flows-overview",
        "/products/test-flow-details",
        "/products/yield-boundaries",
        "/products/packing-flow-details",
        "/products/step-mapping",
        "/products/pov-efficiencies",
        "/products/pov-params",
        "/products/manual-flow",
        "/products/missing-link",
        "/settings/user-management",
        "/settings/approval-management",
        "/settings/glossary",
        "/settings/sys-admin-tools",
        "/settings/scheduled-jobs",
        "/profile/worklist",
        "/profile/worklist/approval/*",
        "/profile/worklist/request/*",
        "/profile/my-profile",
        "/mass-upload",
        "/landingai",
        "/landingai/projects",
        "/landingai/projects/*",
        "/landingai/projects/*/*",
        "/landingai/images/*",
        "/landingai/labelling/*/*",
        "/landingai/model/*",
        "/landingai/model/*/*"
      })
  public String redirect() {
    // Forward to home page so that route is preserved.
    return "forward:/index.html";
  }
}
