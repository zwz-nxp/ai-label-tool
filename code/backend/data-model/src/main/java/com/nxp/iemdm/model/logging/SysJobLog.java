package com.nxp.iemdm.model.logging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SYS_JOB_LOG")
@XmlAccessorType(XmlAccessType.FIELD)
public class SysJobLog {
  @Id
  @Column(name = "TIME")
  private Instant timestamp;

  @Column(name = "JOB_NAME")
  private String jobName;

  @Column(name = "INFO")
  private String logMessage;

  @Column(name = "TRACKING_ID")
  private String trackingId;
}
