package com.nxp.iemdm.model.configuration.pojo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

/** Pojo that can be used in Freemarker email templates to pass common info. */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailMetaInfo {
  private String messageType = "";
  private String messageTitle = "";
  private String mailingList = "";
  private String appName = "";
  private String appVersion = "";
  private String appDate = "";
  private String appYear = "";
}
