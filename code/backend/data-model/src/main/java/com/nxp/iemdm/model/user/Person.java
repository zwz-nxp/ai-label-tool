package com.nxp.iemdm.model.user;

import com.nxp.iemdm.adapter.InstantXmlAdapter;
import com.nxp.iemdm.model.location.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "GLOBAL_USER")
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Person implements Serializable {
  @Serial private static final long serialVersionUID = -3388432137204085945L;

  @Id
  @Column(name = "WBI")
  @NotBlank
  private String wbi;

  @Column(name = "NAME")
  @NotNull
  private String name;

  @Transient private String pictureURL;

  @Column(name = "EMAIL")
  @NotNull
  private String email;

  @ManyToOne
  @JoinColumn(name = "PRIMARY_LOCATION", referencedColumnName = "ID", nullable = false)
  @NotNull
  private Location primaryLocation;

  @Column(name = "LOGIN_ALLOWED")
  private Boolean loginAllowed;

  @Transient @XmlTransient private Map<Integer, Set<Role>> roles = new HashMap<>();

  @Column(name = "LAST_LOGIN")
  @XmlJavaTypeAdapter(InstantXmlAdapter.class)
  @XmlSchemaType(name = "dateTime")
  private Instant lastLogin;

  @Column(name = "LAST_UPDATED")
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  public Set<Role> getRolesForLocationId(int locationId) {
    return this.roles.getOrDefault(locationId, new HashSet<>());
  }

  public boolean hasRoleForLocationOrGlobal(Location location, String roleId) {
    int globalLocationId = 0;
    return this.getRolesForLocationId(location.getId()).stream()
            .anyMatch(role -> role.getId().equals(roleId))
        || this.getRolesForLocationId(globalLocationId).stream()
            .anyMatch(role -> role.getId().equals(roleId));
  }

  public boolean hasRoleForLocationId(String roleId, int locationId) {
    return this.getRolesForLocationId(locationId).stream()
        .anyMatch(role -> role.getId().equals(roleId));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Person person)) return false;
    return Objects.equals(wbi, person.wbi);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(wbi);
  }
}
