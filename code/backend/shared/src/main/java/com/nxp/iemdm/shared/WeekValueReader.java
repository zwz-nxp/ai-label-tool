package com.nxp.iemdm.shared;

import com.nxp.iemdm.model.planning.TimephasedWeekValues;

public class WeekValueReader {

  private static final int HOURS_PER_WEEK = 168; // 1 week in hours: 24 * 7

  private WeekValueReader() {}

  public static double getWeekValueFromTimePhasedWeekValues(
      TimephasedWeekValues timephasedWeekValues, int weekCount) {
    if (timephasedWeekValues != null) {
      return timephasedWeekValues.getWeekValue(weekCount);
    } else {
      return 0d;
    }
  }
}
