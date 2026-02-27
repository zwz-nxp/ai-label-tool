package com.nxp.iemdm.model.configuration;

import static org.hibernate.type.SqlTypes.TIMESTAMP;

import com.nxp.iemdm.validation.annotations.DayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "GLOBAL_NXP_PROD_YEAR")
@XmlAccessorType(XmlAccessType.FIELD)
public class NxpProductionYear {
  @Id
  @Column(name = "YEAR")
  @NotNull
  @Min(1)
  private Integer year;

  @Column(name = "START_DATE")
  @NotNull
  @DayOfWeek(dayOfWeek = java.time.DayOfWeek.MONDAY)
  @JdbcTypeCode(TIMESTAMP)
  private LocalDate startDate;

  @Column(name = "END_DATE")
  @DayOfWeek(dayOfWeek = java.time.DayOfWeek.SUNDAY, message = "Only Sundays are allowed")
  @JdbcTypeCode(TIMESTAMP)
  private LocalDate endDate;

  @Column(name = "LAST_UPDATE")
  @UpdateTimestamp
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
}
