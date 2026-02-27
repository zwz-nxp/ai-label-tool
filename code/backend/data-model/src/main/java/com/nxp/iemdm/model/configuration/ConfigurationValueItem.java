package com.nxp.iemdm.model.configuration;

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

@Entity
@Table(name = "SYS_CONFIG")
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationValueItem {
  @Id
  @Column(name = "CONFIG_ITEM")
  private String configurationKey;

  @Column(name = "CONFIG_VALUE")
  private String configurationValue;

  @Column(name = "LAST_UPDATED")
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
}
