package com.nxp.iemdm.model.user;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.util.Set;
import lombok.Data;

/** POJO to pass to Angular a list of all Role's plus Site + set of SapCode's for a user */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleAllowed {
  private Role role;
  private boolean allSites = false;
  private String site;
  private Set<String> sapCodes;
}
