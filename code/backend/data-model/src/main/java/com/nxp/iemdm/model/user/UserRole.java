package com.nxp.iemdm.model.user;

import com.nxp.iemdm.model.location.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "GLOBAL_USER_ROLE")
@IdClass(UserRole.UserRoleKey.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class UserRole {
  @Id
  @ManyToOne
  @JoinColumn(name = "USER_ID", referencedColumnName = "WBI", nullable = false)
  private Person user;

  @Id
  @ManyToOne
  @JoinColumn(name = "LOCATION", referencedColumnName = "ID")
  private Location location;

  @Id
  @ManyToOne
  @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID", nullable = false)
  private Role role;

  @Column(name = "LAST_UPDATED")
  @UpdateTimestamp
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserRoleKey implements Serializable {
    @Serial private static final long serialVersionUID = -5090952556027717366L;

    private String user;
    private Integer location;
    private String role;
  }
}
