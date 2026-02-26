package com.nxp.iemdm.shared.utility;

/** utility helper class to for example format search filters. */
public class SearchParameterHelper {

  private SearchParameterHelper() {}

  public static String formatSqlLikeWildcard(final String value) {
    if (value == null || value.isEmpty()) {
      return "%%";
    } else {
      return "%" + value.toUpperCase() + "%";
    }
  }
}
