package com.nxp.iemdm.shared.utility;

import java.text.NumberFormat;
import java.util.regex.Pattern;

public class StringUtil {
  private static final String EMAIL_REGEX_PATTERN = "^(.+)@(\\S+)$";

  private StringUtil() {}

  public static boolean isNullOrWhiteSpace(String inString) {
    if (inString != null) {
      return inString.strip().isBlank();
    }
    return true;
  }

  public static boolean isValidEmail(String email) {
    if (isNullOrWhiteSpace(email)) {
      return false;
    } else {
      return matchesPattern(email, EMAIL_REGEX_PATTERN);
    }
  }

  public static boolean matchesPattern(String input, String regexPattern) {
    return Pattern.compile(regexPattern).matcher(input).matches();
  }

  public static boolean isEqualsIncludeNull(String a, String b) {
    a = convertNullToEmptyString(a);
    b = convertNullToEmptyString(b);
    return a.equals(b);
  }

  public static String convertToStringWithDigits(Double doubleValue, int digits) {
    if (doubleValue == null) {
      return null;
    }
    return convertToStringWithDigits(doubleValue.doubleValue(), digits);
  }

  public static String convertToStringWithDigits(double doubleValue, int digits) {
    NumberFormat numberFormat = NumberFormat.getInstance();
    numberFormat.setMaximumFractionDigits(digits);
    return numberFormat.format(doubleValue);
  }

  private static String convertNullToEmptyString(String inString) {
    if (inString == null) {
      return "";
    }
    return inString;
  }
}
