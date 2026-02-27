package com.nxp.iemdm.service.transformers;

import com.nxp.iemdm.ProgressReportUtility;
import com.nxp.iemdm.model.massupload.MassUploadAction;
import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.shared.intf.massupload.MassUploadConfiguration;
import com.nxp.iemdm.shared.intf.massupload.Transformer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.validation.BeanPropertyBindingResult;

public abstract class TransformerBase implements Transformer {
  protected static final int DECIMAL_PLACES = 2;
  // A date that will be instantiated at the beginning of the transformation phase. Why is this
  // necessary? Because processing might begin close to the end of a day, meaning that during
  // processing system time might change to another day, but date validation should still occur at
  // the original day that the MU was uploaded.
  protected final LocalDate dateOfStartOfProcessing;

  public TransformerBase() {
    this.dateOfStartOfProcessing = LocalDate.now();
  }

  public Collection<MassUploadExtract> transform(
      Collection<MassUploadExtract> entities,
      MassUploadResponse massUploadResponse,
      MassUploadConfiguration configuration) {
    return entities.stream()
        .filter(x -> x.getTransformedEntity() != null)
        .collect(Collectors.toList());
  }

  protected void combineErrorsAndWarningsIntoMessage(
      MassUploadExtract entity,
      BeanPropertyBindingResult errorCollector,
      BeanPropertyBindingResult warningCollector,
      MassUploadResponse massUploadResponse,
      MassUploadConfiguration configuration) {
    for (var message : warningCollector.getAllErrors()) {
      String warningMessage =
          String.format("%d ", entity.getRowNumber() + 1) + message.getDefaultMessage();
      massUploadResponse.appendErrorMessage(warningMessage, entity.getRowNumber());
    }

    for (var message : errorCollector.getAllErrors()) {
      String errorMessage =
          String.format("%d ", entity.getRowNumber() + 1) + message.getDefaultMessage();
      massUploadResponse.appendErrorMessage(errorMessage, entity.getRowNumber());
    }

    configuration.skipRowInFuture(entity.getRowNumber());
  }

  protected void combineErrorsIntoMessage(
      MassUploadExtract entity,
      BeanPropertyBindingResult errorCollector,
      MassUploadResponse massUploadResponse,
      MassUploadConfiguration configuration) {
    for (var message : errorCollector.getAllErrors()) {
      String errorMessage =
          String.format("%d ", entity.getRowNumber() + 1) + message.getDefaultMessage();
      massUploadResponse.appendErrorMessage(errorMessage, entity.getRowNumber());
    }

    configuration.skipRowInFuture(entity.getRowNumber());
  }

  protected void combineErrorsIntoMessage(
      MassUploadExtract entity,
      Collection<String> errors,
      MassUploadResponse massUploadResponse,
      MassUploadConfiguration configuration) {
    for (var message : errors) {
      String errorMessage = String.format("%d ", entity.getRowNumber() + 1) + message;
      massUploadResponse.appendErrorMessage(errorMessage, entity.getRowNumber());
    }

    configuration.skipRowInFuture(entity.getRowNumber());
  }

  protected void combineWarningsIntoMessage(
      MassUploadExtract entity,
      BeanPropertyBindingResult warningCollector,
      MassUploadResponse massUploadResponse) {
    StringBuilder warningMessage = new StringBuilder();
    warningMessage.append(String.format("%d contains a warning.", entity.getRowNumber() + 1));

    for (var message : warningCollector.getAllErrors()) {
      warningMessage.append(". ");
      warningMessage.append(message.getDefaultMessage());
    }
    massUploadResponse.appendWarningMessage(warningMessage.toString(), entity.getRowNumber());
    // Even though an entity has a warning, we still process it.
  }

  protected void reportProgress(
      int counter,
      int total,
      ProgressReportUtility progressReportUtility,
      MassUploadConfiguration configuration) {
    int progressReportInterval =
        Math.max(total / ProgressReportUtility.PROGRESS_REPORT_INSTANCES, 1);
    if (counter % progressReportInterval == 0 || counter == total) {
      progressReportUtility.reportProgress(
          counter, total, ProgressReportUtility.TRANSFORM_OFFSET, configuration.getWbi());
    }
  }

  protected String getUpdateInfo(MassUploadConfiguration configuration) {
    return String.format(Locale.US, "MU_VERSION_%2.2f", configuration.massUploadType.version);
  }

  protected void validateDate(
      BeanPropertyBindingResult errorCollector, LocalDate date, MassUploadAction action) {
    if ((action == MassUploadAction.ADD_ONLY
            || action == MassUploadAction.ADD_OR_CHANGE
            || action == MassUploadAction.CHANGE_ONLY)
        && date.isBefore(this.dateOfStartOfProcessing)) {
      errorCollector.reject("EFFECTIVE_DATE", "Rejected: Date/Time cannot be in the past");
    }

    if (action == MassUploadAction.DELETE && !date.isAfter(this.dateOfStartOfProcessing)) {
      errorCollector.reject(
          "EFFECTIVE_DATE", "Rejected: delete current or past Date/Time is not allowed");
    }
  }

  protected String getString(Object obj) {
    String result;

    if (obj == null) {
      result = "";
    } else {
      try {
        result = String.valueOf(obj);
      } catch (Exception exception) {
        result = "";
      }
    }

    result = result.strip();

    return result;
  }

  protected LocalDate getDate(Object obj) {
    return (LocalDate) obj;
  }

  protected double getDouble(Object obj) {
    return (double) obj;
  }

  protected int getInteger(Object obj) {
    if (obj == null) {
      return -1;
    }
    return ((Number) obj).intValue();
  }

  protected BigDecimal getBigDecimal(Object obj) {
    return (BigDecimal) obj;
  }
}
