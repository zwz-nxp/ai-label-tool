package com.nxp.iemdm.shared;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class IemdmConstants {
  private IemdmConstants() {}

  public static final String IEMDM_USER = "nxf45365";
  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public static final String FULL_INIT_JOBNAME = "FULL_INIT_PLANNING_W";

  // war files
  public static final String IEMDM_SERVICES = "iemdm-services";
  public static final String IEMDM_INTERFACE = "iemdm-interface";
  public static final String IEMDM_CAP_STMT = "capacity-statement";
  public static final String IEMDM_API = "api";
  public static final String USER_WBI_HEADER = "USER-WBI";
}
