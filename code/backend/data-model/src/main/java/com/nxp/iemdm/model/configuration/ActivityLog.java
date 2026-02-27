package com.nxp.iemdm.model.configuration;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LOCAL_ACTIVITY_LOG")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ActivityLog.ActivityLogKey.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityLog {
  @Id
  @ManyToOne
  @JoinColumn(name = "USER_ID", referencedColumnName = "WBI")
  private Person user;

  @Id
  @ManyToOne
  @JoinColumn(name = "LOCATION", referencedColumnName = "ID")
  private Location location;

  @Id
  @Column(name = "ACTIVITY_DATE")
  private Instant timestamp;

  @Column(name = "ACTIVITY")
  private String action;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ActivityLogKey implements Serializable {
    @Serial private static final long serialVersionUID = -5090952556027717357L;

    private String user;
    private Integer location;
    private Instant timestamp;
  }
}
