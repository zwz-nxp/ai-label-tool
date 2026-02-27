package com.nxp.iemdm.model.consumption;

public interface LookupTable {
  /**
   * The name of the lookup table
   *
   * @return
   */
  String getTableName();

  /**
   * the PK of the lookup table
   *
   * @return
   */
  String getKey();

  void setKey(String value);

  String getDisplayName();

  void setDisplayName(String value);
}
