package com.nxp.iemdm.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GLOBAL_USER_SETTING")
@IdClass(UserSetting.UserSettingKey.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class UserSetting {
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "USER_ID", referencedColumnName = "WBI")
  @Id
  private Person user;

  @Column(name = "KEY")
  @Id
  private String key;

  @Column(name = "VALUE")
  @Lob
  private String value;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserSettingKey implements Serializable {
    @Serial private static final long serialVersionUID = -5090952556027717367L;
    private String user;
    private String key;
  }
}
