export class PageColumnsConfiguration {
  public configuration = new ColumnsConfiguration();
  public page = Page["USAGE_RATE_TEST_FLOW_DETAILS_COLUMNS_CONF"];
  public user = "";

  public setConfiguration(value: ColumnsConfiguration): void {
    this.configuration = value;
  }

  public setPage(value: Page): void {
    this.page = value;
  }

  public setUser(value: string): void {
    this.user = value;
  }

  /**
   * This function converts the pageColumnsConfiguration object to an Object.
   * The reason is that the configuration property does not get the values as expected in the REST
   * because it is a Map object.
   * @param pageColumnConfiguration
   */
  public convertToJsonFormat(
    pageColumnConfiguration: PageColumnsConfiguration
  ): PageColConfObject {
    return {
      user: pageColumnConfiguration.user,
      page: pageColumnConfiguration.page,
      configuration: {
        defaultConfigurationName:
          pageColumnConfiguration.configuration.defaultConfigurationName,
        configurationsByName: Object.fromEntries(
          pageColumnConfiguration.configuration.configurationsByName
        ),
      },
    };
  }
}

export class ColumnsConfiguration {
  public defaultConfigurationName = "";
  public configurationsByName: Map<string, Array<string>> = new Map();
}

export enum Page {
  USAGE_RATE_TEST_FLOW_DETAILS_COLUMNS_CONF = "USAGE_RATE_TEST_FLOW_DETAILS_COLUMNS_CONF",
  USAGE_RATE_PACKING_FLOW_DETAILS_COLUMNS_CONF = "USAGE_RATE_PACKING_FLOW_DETAILS_COLUMNS_CONF",
}

export type PageColConfObject = {
  user: string;
  page: string;
  configuration: any;
};
