package com.nxp.iemdm.model.massupload;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** POJO to return formatter response after call a massUpload service */
@Getter
@EqualsAndHashCode
@XmlAccessorType(XmlAccessType.FIELD)
public class MassUploadResponse {
  @Setter private int successCount = 0;
  private int insertCount = 0;
  private int updateCount = 0;
  private int ignoreCount = 0;
  private int errorCount = 0;
  private int warningCount = 0;
  private int deleteCount = 0;
  private int duplicateCount = 0;
  private int recordsToBeLoaded = 0;
  private final List<String> errorMessages = new ArrayList<>();
  private final List<String> warningMessages = new ArrayList<>();
  private final List<String> excludedFromUploadList = new ArrayList<>();
  private final List<Integer> rowNumberOfErrorMessages = new ArrayList<>();
  private final List<Integer> rowNumberOfWarningMessages = new ArrayList<>();
  private boolean criticalErrors = false;

  public void appendErrorMessage(String msg) {
    this.errorMessages.add(msg);
    this.errorCount++;
  }

  public void appendErrorMessage(String msg, int rowNumber) {
    this.errorMessages.add(msg);
    this.errorCount++;
    this.rowNumberOfErrorMessages.add(rowNumber);
  }

  public void appendWarningMessage(String message, int rowNumber) {
    this.warningMessages.add(message);
    this.warningCount++;
    this.rowNumberOfWarningMessages.add(rowNumber);
  }

  public void appendErrorMessages(Iterable<String> errorMessages, int rowNumber) {
    errorMessages.forEach(message -> this.appendErrorMessage(message, rowNumber));
  }

  public void incrementInsertCount(int value) {
    this.insertCount += value;
    this.successCount += value;
  }

  public void incrementInsertCount() {
    this.insertCount++;
    this.successCount++;
  }

  public void incrementUpdateCount(int value) {
    this.updateCount += value;
    this.successCount += value;
  }

  public void incrementUpdateCount() {
    this.updateCount++;
    this.successCount++;
  }

  public void incrementDuplicateCount(int value) {
    this.duplicateCount += value;
  }

  public void incrementDuplicateCount() {
    this.incrementDuplicateCount(1);
  }

  public void incrementIgnoreCount(int value) {
    this.ignoreCount += value;
  }

  public void incrementIgnoreCount() {
    this.ignoreCount++;
  }

  public void incrementDeleteCount(int value) {
    this.deleteCount += value;
    this.successCount += value;
  }

  public void incrementDeleteCount() {
    this.deleteCount++;
    this.successCount++;
  }

  public void setCriticalErrors() {
    this.criticalErrors = true;
  }

  public void incrementRecordsToBeLoaded() {
    this.recordsToBeLoaded++;
  }

  // actionError and notFoundError are only used with Mass Upload Loaders.
  public void actionError(MassUploadAction action, int rowNumber, String error) {
    var message =
        String.format(
            "Action '%s' could not be executed on row '%d'. The following error occurred '%s'.",
            action, rowNumber + 1, error);
    this.appendErrorMessage(message, rowNumber);
  }

  public void notFoundError(MassUploadAction action, int rowNumber) {
    var message =
        String.format(
            "Entity in row '%d' could not be %s, because it could not be found.",
            rowNumber + 1, action);
    this.appendErrorMessage(message, rowNumber);
  }
}
