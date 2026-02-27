package com.nxp.iemdm.operational.service.cron;

import com.nxp.iemdm.operational.service.TemplateProcessorImpl;
import com.nxp.iemdm.operational.service.rest.PersonServiceImpl;
import com.nxp.iemdm.shared.intf.approval.DelegateService;
import com.nxp.iemdm.shared.intf.controller.ReminderService;
import com.nxp.iemdm.shared.intf.notification.NotificationModService;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
public class ReminderServiceImpl implements ReminderService {

  private final PersonServiceImpl personService;
  private final NotificationModService notificationService;
  private final TemplateProcessorImpl templateProcessor;
  private final DelegateService delegateService;

  @Autowired
  public ReminderServiceImpl(
      PersonServiceImpl personService,
      NotificationModService notificationService,
      TemplateProcessorImpl templateProcessor,
      DelegateService delegateService) {
    this.personService = personService;
    this.notificationService = notificationService;
    this.templateProcessor = templateProcessor;
    this.delegateService = delegateService;
  }

  @Transactional
  public void sendApprovalReminders() {}
}
