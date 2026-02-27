package com.nxp.iemdm.model.configuration.pojo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class VersionInfo implements Serializable {
  @Serial private static final long serialVersionUID = 2033018224159979951L;

  private String name;
  private String version;
  private String buildDate;
  private String commitHashShort;
  private String node;
  private Instant createdAt;
  private String databaseName;
  private String databaseUrl;

  public VersionInfo() {
    this.createdAt = Instant.now();
  }
}
