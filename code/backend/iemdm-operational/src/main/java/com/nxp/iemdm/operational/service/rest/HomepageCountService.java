package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.notification.HomePageCount;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.repository.jpa.NotificationRepository;
import jakarta.ws.rs.core.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/homepagecount")
public class HomepageCountService {
  private final NotificationRepository notificationRepository;

  public HomepageCountService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{wbi}/{maxNotifications}", produces = MediaType.APPLICATION_JSON)
  public HomePageCount getNotificationCount(
      @PathVariable("wbi") String wbi, @PathVariable("maxNotifications") String maxNotifications)
      throws NotFoundException {

    HomePageCount result = new HomePageCount();
    int maxNrNotifications = Integer.parseInt(maxNotifications);

    int countNotifications = this.notificationRepository.countByRecipient_WbiAndReadFalse(wbi);
    int countCapacityStatementRecords = 0;

    result.setUnreadNotificationCount(countNotifications + countCapacityStatementRecords);
    result.setMaxNotifications(maxNrNotifications);

    result.setApprovalCount(0); // in case you have a delegate set

    return result;
  }
}
