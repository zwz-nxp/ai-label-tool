package com.nxp.iemdm.model.search;

import java.io.Serial;
import java.io.Serializable;

/** POJO used in GenericSearch */
public abstract class GenericBaseArg implements Serializable {

  @Serial private static final long serialVersionUID = 4748256056400354784L;

  private GenericSearchSortField field;

  public GenericSearchSortField getField() {
    return field;
  }

  public void setField(GenericSearchSortField sortField) {
    this.field = sortField;
  }
}
