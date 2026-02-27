package com.nxp.iemdm.model.configuration;

import com.nxp.iemdm.model.configuration.pojo.ColumnsConfiguration;

public class PageColumnsConfiguration {

  private String user;

  private String page;

  private ColumnsConfiguration configuration;

  public PageColumnsConfiguration(String user, String page, ColumnsConfiguration configuration) {
    this.user = user;
    this.page = page;
    this.configuration = configuration;
  }

  public PageColumnsConfiguration() {}

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public ColumnsConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(ColumnsConfiguration configuration) {
    this.configuration = configuration;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
