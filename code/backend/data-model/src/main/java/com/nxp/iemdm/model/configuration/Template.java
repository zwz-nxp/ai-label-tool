package com.nxp.iemdm.model.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "GLOBAL_TEMPLATE")
@XmlAccessorType(XmlAccessType.FIELD)
public class Template {
  @Column(name = "NAME")
  @Id
  @NotBlank
  private String name;

  @Column(name = "TEMPLATE", length = Integer.MAX_VALUE)
  @Lob
  private String template;

  @Column(name = "LAST_UPDATE")
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
}
