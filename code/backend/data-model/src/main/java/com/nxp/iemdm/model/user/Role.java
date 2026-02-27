package com.nxp.iemdm.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GLOBAL_ROLE")
@XmlAccessorType(XmlAccessType.FIELD)
public class Role implements Serializable {
  @Serial private static final long serialVersionUID = -106293440801022703L;

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "ROLE_NAME")
  private String roleName;

  @Column(name = "DESCRIPTION")
  private String description;

  public String getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Role role) {
      return Objects.equals(id, role.id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 1179619963;
  }
}
