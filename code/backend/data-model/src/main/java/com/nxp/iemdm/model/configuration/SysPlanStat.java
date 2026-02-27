package com.nxp.iemdm.model.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SYS_PLAN_STAT")
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class SysPlanStat {
  @Id
  @Column(name = "CONSTANT_NAME")
  @NotNull
  private String constantName;

  @Column(name = "sortOrder")
  @NotNull
  private Integer sortOrder;

  @Column(name = "capView")
  @NotNull
  private Integer capView;

  @Column(name = "eqView")
  @NotNull
  private Integer eqView;

  @Column(name = "status")
  @NotNull
  private String status;
}
