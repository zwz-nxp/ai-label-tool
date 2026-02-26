package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.model.configuration.pojo.VersionInfo;
import java.util.Arrays;

/** helper class to build a VersionInfo object */
public class VersionInfoHelper {

  private final String name;
  private final String version;
  private final String buildDate;
  private final String commitHashShort;

  private VersionInfoHelper(String name, String version, String commitHashShort, String buildDate) {
    this.name = name;
    this.version = version;
    this.commitHashShort = commitHashShort;
    this.buildDate = buildDate;
  }

  public static VersionInfoHelper create(
      String name, String version, String commitHashShort, String buildDate) {
    return new VersionInfoHelper(name, version, commitHashShort, buildDate);
  }

  /**
   * returns information about application
   *
   * @return version info
   */
  public VersionInfo build() {
    VersionInfo result = new VersionInfo();
    result.setName(this.name);
    result.setBuildDate(formatValue(this.buildDate));
    result.setVersion(formatValue(this.version));
    result.setCommitHashShort(formatValue(this.commitHashShort));
    result.setNode(new NodeNameHelper().getNodeName());
    return result;
  }

  public static String getYear(String value) {
    return Arrays.stream(value.replace("\"", "").split("-"))
        .filter(result -> result.matches("^\\d{4}$"))
        .findFirst()
        .orElse("--");
  }

  /**
   * get rid of quotes and commna
   *
   * @param value to format
   * @return formatted value
   */
  private static String formatValue(String value) {
    String result = value.replace("\"", "");
    if (result.endsWith(",")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }
}
