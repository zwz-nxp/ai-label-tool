package com.nxp.databricks;

import static org.junit.jupiter.api.Assertions.*;

import com.databricks.sdk.WorkspaceClient;
import com.databricks.sdk.core.DatabricksConfig;
import com.databricks.sdk.service.jobs.Run;
import com.databricks.sdk.service.jobs.RunNow;
import com.databricks.sdk.service.jobs.RunNowResponse;
import com.databricks.sdk.support.Wait;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * TEMPORARY playground test for Databricks connectivity. This is just for testing and playing
 * around - will be deleted later.
 *
 * <p>To run: 1. Set environment variables: set
 * DATABRICKS_HOST=https://your-workspace.cloud.databricks.com set
 * DATABRICKS_TOKEN=your-access-token 2. Remove @Disabled annotation 3. Run: mvn test
 * -Dtest=DatabricksPlaygroundTest
 */
public class DatabricksPlaygroundTest {

  private static final String WORKSPACE_URL = System.getenv("DATABRICKS_HOST");
  private static final String ACCESS_TOKEN = System.getenv("DATABRICKS_TOKEN");

  /** Disable SSL verification for corporate proxy/VPN environments */
  @BeforeAll
  static void disableSslVerification() throws Exception {
    TrustManager[] trustAll =
        new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          }
        };
    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(null, trustAll, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
  }

  @Test
  @Disabled("Remove this annotation and set environment variables to run")
  public void testConnection() {
    String separator = "============================================================";
    System.out.println("\n" + separator);
    System.out.println("Testing Databricks Connection");
    System.out.println(separator);

    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST environment variable");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN environment variable");

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      // Get current user
      var user = client.currentUser().me();
      System.out.println("✓ Connected successfully!");
      System.out.println("  User: " + user.getUserName());
      System.out.println("  User ID: " + user.getId());

      assertNotNull(user.getUserName());

    } catch (Exception e) {
      System.err.println("✗ Connection failed: " + e.getMessage());
      e.printStackTrace();
      fail("Connection failed: " + e.getMessage());
    }
  }

  @Test
  @Disabled("Remove this annotation and set environment variables to run")
  public void listClusters() {
    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN");

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      String separator = "============================================================";
      System.out.println("\n" + separator);
      System.out.println("Available Clusters:");
      System.out.println(separator);

      try {
        var clusters = client.clusters().list(null);
        clusters.forEach(
            cluster -> {
              System.out.println("  • " + cluster.getClusterName());
              System.out.println("    ID: " + cluster.getClusterId());
              System.out.println("    State: " + cluster.getState());
              System.out.println();
            });
      } catch (Exception clusterException) {
        System.out.println("⚠ Could not list clusters: " + clusterException.getMessage());
      }

    } catch (Exception e) {
      fail("Failed to list clusters: " + e.getMessage());
    }
  }

  @Test
  @Disabled("Remove this annotation and set environment variables to run")
  public void listJobs() {
    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN");

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      String separator = "============================================================";
      System.out.println("\n" + separator);
      System.out.println("Available Jobs:");
      System.out.println(separator);

      try {
        var jobs = client.jobs().list(null);
        jobs.forEach(
            job -> {
              System.out.println("  • " + job.getSettings().getName());
              System.out.println("    ID: " + job.getJobId());
              System.out.println();
            });
      } catch (Exception jobException) {
        System.out.println("⚠ Could not list jobs: " + jobException.getMessage());
      }

    } catch (Exception e) {
      fail("Failed to list jobs: " + e.getMessage());
    }
  }

  @Test
  @Disabled("Remove this annotation and set environment variables to run")
  public void exploreWorkspace() {
    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN");

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      String separator = "============================================================";
      System.out.println("\n" + separator);
      System.out.println("Workspace Root Contents:");
      System.out.println(separator);

      try {
        var objects = client.workspace().list("/");
        objects.forEach(
            obj -> {
              System.out.println("  • " + obj.getPath());
              System.out.println("    Type: " + obj.getObjectType());
              System.out.println();
            });
      } catch (Exception workspaceException) {
        System.out.println("⚠ Could not list workspace: " + workspaceException.getMessage());
      }

    } catch (Exception e) {
      System.out.println("⚠ Could not list workspace: " + e.getMessage());
    }
  }

  @Test
  //  @Disabled("Remove this annotation and set environment variables to run")
  public void triggerTrainingJob() {
    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN");

    long jobId = 761619706046349L;
    String trId = "DatabricksADC-PJ2286-SN15389-TR15390";

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      String separator = "============================================================";
      System.out.println("\n" + separator);
      System.out.println("Triggering Training Job");
      System.out.println("  Job ID: " + jobId);
      System.out.println("  TR_ID: " + trId);
      System.out.println(separator);

      Map<String, String> params = new HashMap<>();
      params.put("TR_ID", trId);

      RunNow runNow = new RunNow().setJobId(jobId).setJobParameters(params);

      Wait<Run, RunNowResponse> runWait = client.jobs().runNow(runNow);
      Long runId = runWait.getResponse().getRunId();

      System.out.println("✓ Job triggered successfully!");
      System.out.println("  Run ID: " + runId);

      // Check initial status
      Run run = client.jobs().getRun(runId);
      System.out.println("  Status: " + run.getState().getLifeCycleState());

      assertNotNull(runId);

    } catch (Exception e) {
      System.err.println("✗ Job trigger failed: " + e.getMessage());
      e.printStackTrace();
      fail("Job trigger failed: " + e.getMessage());
    }
  }

  @Test
  @Disabled("Set RUN_ID below and remove @Disabled to run")
  public void checkJobStatusAndGetResults() {
    assertNotNull(WORKSPACE_URL, "Set DATABRICKS_HOST");
    assertNotNull(ACCESS_TOKEN, "Set DATABRICKS_TOKEN");

    // *** SET YOUR RUN ID HERE ***
    long runId = 136982035581366L;
    String trackId = "DatabricksADC-PJ2286-SN15389-TR15390";

    try {
      DatabricksConfig config =
          new DatabricksConfig().setHost(WORKSPACE_URL).setToken(ACCESS_TOKEN);

      WorkspaceClient client = new WorkspaceClient(config);

      String separator = "============================================================";
      System.out.println("\n" + separator);
      System.out.println("Checking Job Status");
      System.out.println("  Run ID: " + runId);
      System.out.println("  Track ID: " + trackId);
      System.out.println(separator);

      // Step 1: Check job status
      Run run = client.jobs().getRun(runId);
      var lifeCycleState = run.getState().getLifeCycleState();
      var resultState = run.getState().getResultState();
      var stateMessage = run.getState().getStateMessage();

      System.out.println("  LifeCycle State: " + lifeCycleState);
      System.out.println("  Result State: " + resultState);
      System.out.println("  State Message: " + stateMessage);

      assertNotNull(lifeCycleState);

      // Step 2: If terminated and successful, try listing result files from volume
      if ("TERMINATED".equals(lifeCycleState.toString())
          && "SUCCESS".equals(String.valueOf(resultState))) {
        System.out.println("\n✓ Job completed successfully! Listing result files...");

        // Adjust this path to match your volume structure
        String volumePath =
            "/Volumes/adc_ie_mdm_dev/adc_ie_mdm_schema/adc_ie_mdm_volume/" + trackId;
        System.out.println("  Volume path: " + volumePath);

        try {
          var files = client.files().listDirectoryContents(volumePath);
          System.out.println("  Files found:");
          files.forEach(
              file -> {
                System.out.println(
                    "    • " + file.getName() + " (isDir: " + file.getIsDirectory() + ")");
              });
        } catch (Exception fileEx) {
          System.out.println("  ⚠ Could not list files: " + fileEx.getMessage());
          System.out.println("  (Volume path may need adjustment)");
        }
      } else {
        System.out.println(
            "\n⏳ Job not yet complete. Current state: " + lifeCycleState + " / " + resultState);
        System.out.println("  Run this test again later to check status.");
      }

    } catch (Exception e) {
      System.err.println("✗ Check failed: " + e.getMessage());
      e.printStackTrace();
      fail("Check failed: " + e.getMessage());
    }
  }
}
