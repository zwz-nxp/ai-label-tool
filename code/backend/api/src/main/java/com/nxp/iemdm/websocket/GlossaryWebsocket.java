package com.nxp.iemdm.websocket;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import com.nxp.iemdm.service.GlossaryService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GlossaryWebsocket {
  private final GlossaryService glossaryService;

  @Autowired
  public GlossaryWebsocket(GlossaryService glossaryService) {
    this.glossaryService = glossaryService;
  }

  @MethodLog
  @SubscribeMapping("/glossary/all")
  public List<GlossaryItem> getWeekPlanningCalendarForCapacity() {
    return glossaryService.getAllGlossaryItems();
  }
}
