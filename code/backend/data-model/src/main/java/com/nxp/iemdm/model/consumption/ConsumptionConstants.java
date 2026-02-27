package com.nxp.iemdm.model.consumption;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** These constants used by various 'Consumption' services and tests. */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsumptionConstants {
  private ConsumptionConstants() {}

  public static final String IEMDM_USER = "nxf45365";
  public static final String DMQ_CHANGE = "CHG";
  public static final DecimalFormat DECIMAL_PCT_FORMAT = new DecimalFormat("###0.0000");
  public static final String PCT_FORMAT = "%.2f";
  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public static final String ALL_PARTS = "ALL_PARTS";
  public static final String EMPTY_VALUE = "--";

  public static final String PROCESS_DQM_INPUT_JOB = "Process DQM input";

  public static final String DQM_YIELD = "YIELD";
  public static final String DQM_ALERT = "ALERT";
  public static final String DQM_ERROR = "ERROR";
  public static final String DQM_WARNING = "WARNING";
  public static final String DQM_CHANGE = "CHG";

  // counters
  public static final String CONSPARAM_INSERT_COUNT = "CpIns";
  public static final String CONSPARAM_UPDATE_COUNT = "CpUpd";
  public static final String DQM_INSERT_COUNT = "DqmIns";
  public static final String DQM_UPDATE_COUNT = "DqmUpd";
  public static final String DQM_ACTUAL_COUNT = "DqmActual";
  public static final String DQM_INPUT_COUNT = "DqmInput";
  public static final String ERROR_COUNT = "Err";
  public static final String PROCESSING_TIME = "Took";
}
