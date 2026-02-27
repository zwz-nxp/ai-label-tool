package com.nxp.iemdm;

import com.nxp.iemdm.enums.configuration.UpdateType;
import com.nxp.iemdm.model.configuration.pojo.Update;
import com.nxp.iemdm.shared.intf.operational.UpdateService;
import org.springframework.stereotype.Component;

@Component
public class ProgressReportUtility {
  private final UpdateService updateService;

  public ProgressReportUtility(UpdateService updateService) {
    this.updateService = updateService;
  }

  public static final int PROGRESS_REPORT_INSTANCES = 16;
  public static final int EXTRACT_OFFSET = 0;
  public static final double TRANSFORM_OFFSET = 33.333;
  public static final double LOAD_OFFSET = 66.666;

  public void reportProgress(int currentProgress, int total, double offset, String wbi) {
    double progressInPercentage = (double) (currentProgress * 100 / total) / 3 + offset;
    Update update = new Update(UpdateType.MASS_UPLOAD_PROGRESS, 0, progressInPercentage, wbi);
    this.updateService.update(update);
  }

  public void reportProgressCompletedOperation(String wbi) {
    Update update = new Update(UpdateType.MASS_UPLOAD_PROGRESS, 0, 100, wbi);
    this.updateService.update(update);
  }
}
