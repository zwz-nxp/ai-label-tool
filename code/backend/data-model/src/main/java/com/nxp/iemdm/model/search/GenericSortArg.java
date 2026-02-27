package com.nxp.iemdm.model.search;

import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** POJO used in GenericSearch */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenericSortArg extends GenericBaseArg {

  @Serial private static final long serialVersionUID = 4748256056400354787L;

  private boolean descending;

  public GenericSortArg(GenericSearchSortField field, boolean descending) {
    this.descending = descending;
    this.setField(field);
  }

  public void setDescending(boolean descending) {
    this.descending = descending;
  }
}
