package com.nxp.iemdm.model.location;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GLOBAL_SAP_CODE")
@Audited
@XmlAccessorType(XmlAccessType.FIELD)
public class SapCode {
  @Id
  @Column(name = "PLANT_CODE")
  @NotNull
  private String plantCode;

  @Column(name = "CNTR_ACRONYM")
  private String enoviaAcronym;

  @Column(name = "CNTR_CITY")
  private String city;

  @Column(name = "CNTR_COUNTRY")
  private String country;

  @Column(name = "CNTR_STATE")
  private String state;

  @ManyToOne
  @JoinColumn(name = "MANAGED_BY_SITE", referencedColumnName = "ID")
  private Location managedBy;

  @Column(name = "LAST_UPDATED")
  @NotNull
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  @NotNull
  private String updatedBy;
}
