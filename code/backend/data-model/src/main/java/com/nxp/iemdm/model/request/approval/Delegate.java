package com.nxp.iemdm.model.request.approval;

import static org.hibernate.type.SqlTypes.TIMESTAMP;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "GLOBAL_DELEGATE_V2")
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Delegate implements Serializable {

  @Serial private static final long serialVersionUID = -7189781409272987232L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ID")
  private Integer id;

  @Column(name = "APPROVER_WBI")
  @NotNull
  private String approverWbi;

  @Column(name = "DELEGATE_WBI")
  @NotNull
  private String delegateWbi;

  @Column(name = "START_DATE")
  @JdbcTypeCode(TIMESTAMP)
  private LocalDate startDate;

  @Column(name = "END_DATE")
  @JdbcTypeCode(TIMESTAMP)
  private LocalDate endDate;

  @Column(name = "LAST_UPDATED")
  @UpdateTimestamp
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
}
