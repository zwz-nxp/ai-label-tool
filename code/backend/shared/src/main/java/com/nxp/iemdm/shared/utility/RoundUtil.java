package com.nxp.iemdm.shared.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Small util for rounding numbers in IEMDM. Rounding is only for the UI, the database should
 * contain the full decimal values. Therefore, only use this method within the API for sending
 * pojo's/entities to the UI or to excel.
 */
public class RoundUtil {
  private RoundUtil() {}

  public static double round(double value, int decimalPlaces) {
    return roundPowerScale(value, decimalPlaces);
  }

  static double roundBigDecimal(double value, int decimalPlaces) {
    BigDecimal bd = BigDecimal.valueOf(value);
    BigDecimal result = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
    return result.doubleValue();
  }

  static double roundPowerScale(double value, int decimalPlaces) {
    int scale = (int) Math.pow(10, decimalPlaces);
    return (double) Math.round(value * scale) / scale;
  }
}
