package com.nxp.iemdm.model.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nxp.iemdm.model.user.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@NoArgsConstructor
@AllArgsConstructor
@Audited
@Entity
@Getter
@Setter
@Table(name = "GLOBAL_MANUFACTURER")
public class Manufacturer implements Serializable {

  @Serial private static final long serialVersionUID = -5744903085348174773L;

  @Id
  @Column(name = "MANUFACTURER_CODE")
  @XmlTransient
  private String manufacturerCode;

  @Column(name = "LAST_UPDATE")
  @UpdateTimestamp
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @ManyToOne
  @JoinColumn(
      name = "UPDATED_BY",
      referencedColumnName = "WBI",
      insertable = false,
      updatable = false)
  @XmlTransient
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @JsonIgnore
  private Person updater;

  @ManyToOne
  @JoinColumn(
      name = "LOCATION_ID",
      referencedColumnName = "ID",
      insertable = false,
      updatable = false)
  @JsonBackReference
  private Location location;

  @Column(name = "LOCATION_ID", nullable = false)
  private Integer locationId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Manufacturer manufacturer = (Manufacturer) o;
    return manufacturerCode.equals(manufacturer.manufacturerCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(manufacturerCode);
  }
}
