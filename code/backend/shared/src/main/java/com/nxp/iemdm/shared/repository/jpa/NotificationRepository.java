package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.notification.Notification;
import com.nxp.iemdm.model.user.Person;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
  @Transactional
  Iterable<Notification> findAllByRecipientAndRead(Person user, boolean read);

  @Transactional
  Iterable<Notification> findAllByRecipient(Person user);

  @Transactional
  int countByRecipient_WbiAndReadFalse(String wbi);

  List<Notification> findByReadAndTimestampBefore(boolean read, Instant timestamp);
}
