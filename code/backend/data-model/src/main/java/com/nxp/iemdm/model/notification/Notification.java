package com.nxp.iemdm.model.notification;

import com.nxp.iemdm.enums.notification.NotificationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "GLOBAL_NOTIFICATION")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Notification extends NotificationParent {
  @Column(name = "TITLE")
  private String title;

  @Column(name = "SEVERITY")
  @Enumerated(EnumType.STRING)
  private NotificationLevel severityLevel;

  @Override
  public String getType() {
    return "notification";
  }
}
