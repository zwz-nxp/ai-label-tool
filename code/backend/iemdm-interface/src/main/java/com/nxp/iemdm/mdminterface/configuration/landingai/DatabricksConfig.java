package com.nxp.iemdm.mdminterface.configuration.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:ie-mdm-interface.properties")
@PropertySource(
    value = "classpath:ie-mdm-interface.override.properties",
    ignoreResourceNotFound = true)
public class DatabricksConfig {

  /** Creates the Databricks workspace configuration bean */
  @Bean
  public WorkspaceConfig databricksWorkspaceConfig(
      @Value("${databricks.workspace.url:}") String url,
      @Value("${databricks.workspace.token:}") String token) {
    return new WorkspaceConfig(url, token);
  }

  /** Creates the Databricks training configuration bean */
  @Bean
  public TrainingConfig databricksTrainingConfig(
      @Value("${databricks.training.notebook-path:/Workspace/training/yolo_training}")
          String notebookPath,
      @Value("${databricks.training.cluster-id:}") String clusterId,
      @Value("${databricks.training.job-id:}") String jobId) {
    return new TrainingConfig(notebookPath, clusterId, jobId);
  }

  /** Creates the Databricks serving configuration bean */
  @Bean
  public ServingConfig databricksServingConfig(
      @Value("${databricks.serving.endpoint-name-pattern:{modelName}-endpoint}")
          String endpointNamePattern) {
    return new ServingConfig(endpointNamePattern);
  }

  /** Creates the Databricks timeout configuration bean */
  @Bean
  public TimeoutConfig databricksTimeoutConfig(
      @Value("${databricks.timeout.job-submission:5000}") int jobSubmission,
      @Value("${databricks.timeout.result-retrieval:3000}") int resultRetrieval,
      @Value("${databricks.timeout.model-test:10000}") int modelTest,
      @Value("${databricks.timeout.model-download:2000}") int modelDownload,
      @Value("${databricks.timeout.model-delete:2000}") int modelDelete) {
    return new TimeoutConfig(jobSubmission, resultRetrieval, modelTest, modelDownload, modelDelete);
  }

  /** Creates the Mock service configuration bean */
  @Bean
  public MockConfig databricksMockConfig(
      @Value("${databricks.mock.base-dir:./mock_training_data}") String baseDir,
      @Value("${databricks.mock.cleanup-temp:true}") boolean cleanupTemp) {
    return new MockConfig(baseDir, cleanupTemp);
  }

  /** Creates the Volume configuration bean for Unity Catalog volumes */
  @Bean
  public VolumeConfig databricksVolumeConfig(
      @Value("${databricks.catalog:}") String catalog,
      @Value("${databricks.schema:}") String schema,
      @Value("${databricks.volume:}") String volume,
      @Value("${databricks.volume.path:}") String volumePath) {
    return new VolumeConfig(catalog, schema, volume, volumePath);
  }

  /** Creates the Results configuration bean for training results download */
  @Bean
  public ResultsConfig databricksResultsConfig(
      @Value("${databricks.results.local-download-path:./training_results}")
          String localDownloadPath) {
    return new ResultsConfig(localDownloadPath);
  }

  /** Creates the main Databricks configuration bean */
  @Bean
  public DatabricksProperties databricksProperties(
      @Value("${databricks.mode:mock}") String mode,
      WorkspaceConfig workspaceConfig,
      TrainingConfig trainingConfig,
      ServingConfig servingConfig,
      TimeoutConfig timeoutConfig) {
    return new DatabricksProperties(
        mode, workspaceConfig, trainingConfig, servingConfig, timeoutConfig);
  }

  // Configuration POJOs

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkspaceConfig {
    private String url;
    private String token;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TrainingConfig {
    private String notebookPath;
    private String clusterId;
    private String jobId;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ServingConfig {
    private String endpointNamePattern;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimeoutConfig {
    private int jobSubmission;
    private int resultRetrieval;
    private int modelTest;
    private int modelDownload;
    private int modelDelete;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MockConfig {
    private String baseDir;
    private boolean cleanupTemp;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VolumeConfig {
    private String catalog;
    private String schema;
    private String volume;
    private String volumePath;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResultsConfig {
    private String localDownloadPath;
  }

  /** Main properties holder with convenience methods */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DatabricksProperties {
    private String mode;
    private WorkspaceConfig workspace;
    private TrainingConfig training;
    private ServingConfig serving;
    private TimeoutConfig timeout;

    public boolean isMockMode() {
      return "mock".equalsIgnoreCase(mode);
    }

    public boolean isRealMode() {
      return "real".equalsIgnoreCase(mode);
    }
  }
}
