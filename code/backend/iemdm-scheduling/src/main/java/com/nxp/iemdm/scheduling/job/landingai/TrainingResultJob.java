package com.nxp.iemdm.scheduling.job.landingai;

import com.nxp.iemdm.scheduling.job.IemdmQuartzJob;
import com.nxp.iemdm.shared.dto.landingai.ProcessingResult;
import com.nxp.iemdm.shared.intf.operational.landingai.TrainingResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for processing training results from Databricks API. Runs periodically to check for
 * training records with status WAITFORRESULT, fetches their results, and persists them to the
 * database.
 *
 * <p>This job is configured to disallow concurrent execution to prevent overlapping processing of
 * the same training records.
 */
@DisallowConcurrentExecution
@Component
@Slf4j
@RequiredArgsConstructor
public class TrainingResultJob implements IemdmQuartzJob {

  private final TrainingResultService trainingResultService;

  @Override
  public void execute(JobExecutionContext context) {
    log.info("TrainingResultJob started");

    try {
      ProcessingResult result = trainingResultService.processWaitingTrainingRecords();

      log.info(
          "TrainingResultJob completed. Total: {}, Success: {}, Failure: {}",
          result.getTotalRecords(),
          result.getSuccessCount(),
          result.getFailureCount());

      if (result.getFailureCount() > 0) {
        log.warn("TrainingResultJob had {} failures:", result.getFailureCount());
        for (String error : result.getErrors()) {
          log.warn("  - {}", error);
        }
      }

    } catch (Exception e) {
      log.error("TrainingResultJob failed with exception: {}", e.getMessage(), e);
    }
  }

  @Override
  public String getDescription() {
    return "Process training results from Databricks API and persist to database";
  }
}
