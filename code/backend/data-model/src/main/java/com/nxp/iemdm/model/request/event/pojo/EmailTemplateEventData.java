package com.nxp.iemdm.model.request.event.pojo;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POJO that contains relevant Event data that can be used to generate email's */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTemplateEventData implements Serializable {

  @Serial private static final long serialVersionUID = 5506048866720946465L;

  private Location site;
  private String resourceGroup;
  private String resourceClass;
  private String startDate;
  private String endDate;
  private double amount;
  private String eventType;
  private String specification;
  private Person requester;

  public Location getLocation() {
    return this.getSite();
  }
}
