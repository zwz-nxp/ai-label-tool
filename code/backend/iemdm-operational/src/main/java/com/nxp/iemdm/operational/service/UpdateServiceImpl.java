package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.shared.intf.operational.CachedResources;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class UpdateServiceImpl implements UpdateService {

  private final SimpMessagingTemplate template;
  private final CachedResources cachedResources;

  public UpdateServiceImpl(SimpMessagingTemplate template, CachedResources cachedResources) {
    super();
    this.template = template;
    this.cachedResources = cachedResources;
  }

  @Override
  public void update(Update update) {
    this.cachedResources.update(update);
    this.template.convertAndSend(Update.ADDRESS, update);
  }
}
