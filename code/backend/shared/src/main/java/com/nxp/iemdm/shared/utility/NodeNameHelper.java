package com.nxp.iemdm.shared.utility;

import java.util.Map;

public class NodeNameHelper {

  public static final String COMPUTERNAME = "COMPUTERNAME";
  public static final String HOSTNAME = "HOSTNAME";
  private final Map<String, String> env = System.getenv();

  /**
   * returns the current nodename, based on the system properties.
   *
   * @return String
   */
  public String getNodeName() {
    if (env.containsKey(COMPUTERNAME)) return env.get(COMPUTERNAME);
    else if (env.containsKey(HOSTNAME)) return env.get(HOSTNAME);
    else return "Unknown Computer";
  }
}
