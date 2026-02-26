package com.nxp.iemdm.shared.utility;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StacktraceHelper {

  private StacktraceHelper() {}

  /**
   * Only display (given) maxlines from the stacktrace
   *
   * @param e
   * @param maxLines
   * @return
   */
  public static String limit(Exception e, int maxLines, String searchFor) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    String[] lines = writer.toString().split("\n");
    StringBuilder sb = new StringBuilder();
    // first the top error messages
    for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
      sb.append(lines[i]).append("\n");
    }
    // and then the messages which contain given searchfor
    for (String line : lines) {
      if (line.contains(searchFor)) {
        sb.append(line).append("\n");
      }
    }

    return sb.toString();
  }
}
