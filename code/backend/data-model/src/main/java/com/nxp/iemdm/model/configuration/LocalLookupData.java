package com.nxp.iemdm.model.configuration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/** POJO to be used in LookupTableController to pass data Angular to fill for example comboboxes */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalLookupData implements Serializable {
  @Serial private static final long serialVersionUID = -2674852462123597759L;

  private List<String> plannableResourceClasses;
}
