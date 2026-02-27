package com.nxp.iemdm.model.scheduling;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "SYS_JOB_OVERVIEW")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobOverview {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ID")
  @JsonIgnore
  private Integer id;

  @Column(name = "JOB_NAME")
  private String jobName;

  @Column(name = "JOB_DESCRIPTION")
  private String jobDescription;

  @Transient private String className;

  @Column(name = "TRIGGER_NAME")
  private String triggerName;

  @Column(name = "TRIGGER_DESCRIPTION")
  private String triggerDescription;

  @Column(name = "CRON_EXPRESSION")
  private String cronExpression;

  @Column(name = "TIMEZONE")
  private String timeZone;

  @Transient private String triggerState;

  @Column(name = "PREVIOUS_FIRE_TIME", columnDefinition = "TIMESTAMP")
  private LocalDateTime previousFireTime;

  @Column(name = "NEXT_FIRE_TIME", columnDefinition = "TIMESTAMP")
  private LocalDateTime nextFireTime;

  @Column(name = "ACTION")
  private String action;

  @UpdateTimestamp
  @Column(name = "LAST_UPDATED")
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JobOverview that)) return false;

    if (!jobName.equals(that.jobName)) return false;
    if (!Objects.equals(triggerName, that.triggerName)) return false;
    if (!Objects.equals(action, that.action)) return false;
    if (!Objects.equals(lastUpdated, that.lastUpdated)) return false;
    return Objects.equals(updatedBy, that.updatedBy);
  }

  @Override
  public int hashCode() {
    int result = jobName.hashCode();
    result = 31 * result + (triggerName != null ? triggerName.hashCode() : 0);
    result = 31 * result + (action != null ? action.hashCode() : 0);
    result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
    result = 31 * result + (updatedBy != null ? updatedBy.hashCode() : 0);
    return result;
  }
}
