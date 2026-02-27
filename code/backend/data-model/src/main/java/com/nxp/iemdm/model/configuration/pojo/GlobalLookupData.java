package com.nxp.iemdm.model.configuration.pojo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/** POJO to be used in LookupTableController to pass data Angular to fill for example comboboxes */
@Data
public class GlobalLookupData implements Serializable {
  @Serial private static final long serialVersionUID = -2674852462123597759L;

  private Map<String, String> userNames;
  private Map<String, String>
      genericSearchMap; // map from GenericSearchSortField.name() -> fieldName
}
