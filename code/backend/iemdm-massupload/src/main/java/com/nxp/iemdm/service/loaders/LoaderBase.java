package com.nxp.iemdm.service.loaders;

import com.nxp.iemdm.ProgressReportUtility;
import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.shared.intf.massupload.Loader;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;

public abstract class LoaderBase implements Loader {
  protected final DateTimeFormatter formatter;
  protected final ProgressReportUtility progressReportUtility;

  protected LoaderBase(ProgressReportUtility progressReportUtility) {
    this.formatter = DateTimeFormatter.ISO_DATE;
    this.progressReportUtility = progressReportUtility;
  }

  @Override
  public boolean load(
      Collection<MassUploadExtract> entities, MassUploadResponse massUploadResponse, String wbi) {
    Collection<MassUploadExtract> entitiesWithoutDuplicates =
        this.removeAndReportDuplicates(entities, massUploadResponse);
    int total = entitiesWithoutDuplicates.size();
    int progressReportInterval =
        Math.max(total / ProgressReportUtility.PROGRESS_REPORT_INSTANCES, 1);
    int counter = 1;

    if (entitiesWithoutDuplicates.isEmpty()) {
      this.progressReportUtility.reportProgressCompletedOperation(wbi);
    }

    for (MassUploadExtract entity : entitiesWithoutDuplicates) {
      switch (entity.getMassUploadAction()) {
        case ADD_OR_CHANGE -> this.addOrChange(entity, massUploadResponse);
        case ADD_ONLY -> this.add(entity, massUploadResponse);
        case CHANGE_ONLY -> this.change(entity, massUploadResponse);
        case DELETE -> this.delete(entity, massUploadResponse);
        default -> {
          // ignore
        }
      }

      if (counter % progressReportInterval == 0 || counter == total) {
        this.progressReportUtility.reportProgress(
            counter, total, ProgressReportUtility.LOAD_OFFSET, wbi);
      }

      counter++;
    }

    return true;
  }

  protected abstract void add(MassUploadExtract entity, MassUploadResponse massUploadResponse);

  protected abstract void change(MassUploadExtract entity, MassUploadResponse massUploadResponse);

  protected abstract void addOrChange(
      MassUploadExtract entity, MassUploadResponse massUploadResponse);

  protected abstract void delete(MassUploadExtract entity, MassUploadResponse massUploadResponse);

  protected Collection<MassUploadExtract> removeAndReportDuplicates(
      Collection<MassUploadExtract> entities, MassUploadResponse massUploadResponse) {
    if (entities.isEmpty()) {
      return entities;
    }

    HashSet<MassUploadExtract> entitiesWithoutDuplicates = new HashSet<>();
    HashSet<Object> seenEntities = new HashSet<>();
    HashMap<Object, Integer> rowObjectMapping =
        new HashMap<>(); // Because we want to report the row
    // information in the error message of the first occurrence of the entity

    for (MassUploadExtract entity : entities) {
      boolean success = seenEntities.add(entity.getTransformedEntity());
      if (success) {
        rowObjectMapping.put(entity.getTransformedEntity(), entity.getRowNumber());
        entitiesWithoutDuplicates.add(entity);
      } else {
        int indexOfFirstOccurrence = rowObjectMapping.get(entity.getTransformedEntity());
        // Works because this element is a duplicate of an element that has already been added
        massUploadResponse.appendErrorMessage(
            String.format(
                "Rejected: entity in row '%d' is a duplicate of entity defined in row '%d' and will therefore not be loaded.",
                entity.getRowNumber() + 1, indexOfFirstOccurrence + 1),
            entity.getRowNumber());
        massUploadResponse.incrementDuplicateCount();
      }
    }

    return entitiesWithoutDuplicates;
  }

  protected void handleUniqueConstraintViolation(
      MassUploadExtract entity, MassUploadResponse massUploadResponse) {

    massUploadResponse.appendErrorMessage(
        String.format(
            "Could not insert record in row '%d', because it already exists.",
            entity.getRowNumber() + 1),
        entity.getRowNumber());
  }

  protected void handleExceptionOnInsert(
      MassUploadExtract entity, MassUploadResponse massUploadResponse, Exception exception) {

    massUploadResponse.incrementIgnoreCount();

    if (this.isCausedByUniqueConstraintViolation(exception)) {
      this.handleUniqueConstraintViolation(entity, massUploadResponse);
      return;
    }

    massUploadResponse.actionError(
        entity.getMassUploadAction(), entity.getRowNumber(), exception.getMessage());
  }

  boolean isCausedByUniqueConstraintViolation(Exception exception) {
    return exception instanceof DuplicateKeyException
        || (exception instanceof DataIntegrityViolationException
            && this.hasOracleErrorCodeForUniqueConstraintViolation(exception));
  }

  private boolean hasOracleErrorCodeForUniqueConstraintViolation(Exception exception) {
    Throwable checkNext = exception;
    while (checkNext != null) {
      String message = checkNext.getMessage();
      if (StringUtils.hasText(message) && message.contains("ORA-00001")) {
        return true;
      }
      checkNext = checkNext.getCause();
    }
    return false;
  }
}
