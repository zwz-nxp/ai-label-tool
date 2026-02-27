package com.nxp.iemdm.builder;

import java.util.HashMap;
import java.util.Map;

public class BuilderConstants {
  private BuilderConstants() {}

  public static final String SITE_ATKH = "ATKH";
  public static final String SITE_ATBK = "ATBK";
  public static final String RC_WT = "WT";
  public static final String RC_FT = "FT";
  public static final String RG_A1 = "A1";
  public static final String RG_J1 = "J1";

  public static final Map<Integer, String> SITE_ID_ACRONYM_MAP = new HashMap<>();

  static {
    SITE_ID_ACRONYM_MAP.put(1, SITE_ATKH);
    SITE_ID_ACRONYM_MAP.put(2, SITE_ATBK);
  }
}
