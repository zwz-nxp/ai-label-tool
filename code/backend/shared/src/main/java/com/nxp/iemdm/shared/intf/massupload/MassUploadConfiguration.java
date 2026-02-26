package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.exception.MassUploadInvalidConfigurationException;
import com.nxp.iemdm.model.massupload.MassUploadType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

@Slf4j
@Getter
public class MassUploadConfiguration {

  /**
   * Use this constructor for when the configuration object is used for generating mass upload
   * templates.
   */
  public MassUploadConfiguration(MassUploadType massUploadType) {
    this.massUploadType = massUploadType;
    try {
      this.requiredColumns = this.requiredColumnsByMassUploadType.get(massUploadType.setOfColumns);
      this.optionalColumns = this.optionalColumnsByMassUploadType.get(massUploadType.setOfColumns);
      this.prefilledColumns =
          this.prefilledColumnsByMassUploadType.get(massUploadType.setOfColumns);
    } catch (NullPointerException nullPointerException) {
      log.error(
          String.format(
              "Mass upload columns for type '%s' are not configured correctly.",
              massUploadType.massUploadTypeName),
          nullPointerException);
      throw new MassUploadInvalidConfigurationException(
          "If you see this message contact the 1st line support", nullPointerException);
    }
    this.rowsToSkip = new ArrayList<>();
  }

  /** Use this constructor when using the configuration object for processing a mass upload. */
  public MassUploadConfiguration(MassUploadType massUploadType, String wbi) {
    this(massUploadType);
    this.wbi = wbi;
  }

  public final MassUploadType massUploadType;

  public final List<MassUploadColumn> requiredColumns;
  public final List<MassUploadColumn> optionalColumns;
  // These columns are only used, when a user does a download of a template, and it needs to be
  // filled with data that is currently shown in the UI. In the mass upload itself, these columns
  // are ignored.
  public final List<MassUploadColumn> prefilledColumns;

  // Rows that should be skipped are those that have the upload flag set to 'N', but also
  // we want to skip rows in subsequent phases (Verification, Extraction, Transformation, Loading)
  // if those rows already contain some form of error. For example, if row 5 contains an error,
  // produced by the verification phase, then row 5 should be ignored in all the subsequent phases.
  private final Collection<Integer> rowsToSkip;

  private String wbi;

  // Shallow copy
  public List<String> getRequiredColumns() {
    return this.requiredColumns.stream()
        .map(MassUploadColumn::getName)
        .collect(Collectors.toList());
  }

  public List<MassUploadColumn> getFilteredRequiredColumns() {
    return this.requiredColumns.stream()
        .filter(x -> x.getIndex() > -1)
        .collect(Collectors.toList());
  }

  public List<MassUploadColumn> getFilteredOptionalColumns() {
    return this.optionalColumns.stream()
        .filter(x -> x.getIndex() > -1)
        .collect(Collectors.toList());
  }

  public int getIndexOfUploadFlag() {
    return this.requiredColumns.stream()
        .filter(x -> x.getName().equals("UPLOAD_FLAG"))
        .findFirst()
        .map(MassUploadColumn::getIndex)
        .orElse(-1);
  }

  public int getIndexOfMassUploadAction() {
    return this.requiredColumns.stream()
        .filter(massUploadColumn -> massUploadColumn.getName().equals("UPLOAD_ACTION"))
        .findFirst()
        .map(MassUploadColumn::getIndex)
        .orElse(-1);
  }

  // Shallow copy
  public List<String> getOptionalColumns() {
    return this.optionalColumns.stream()
        .map(MassUploadColumn::getName)
        .collect(Collectors.toList());
  }

  public List<String> getPrefilledColumns() {
    return this.prefilledColumns.stream()
        .map(MassUploadColumn::getName)
        .collect(Collectors.toList());
  }

  public List<String> getRequiredAndOptionalColumns() {
    var allColumns = new ArrayList<String>();

    allColumns.addAll(this.getRequiredColumns());
    allColumns.addAll(this.getOptionalColumns());

    return allColumns;
  }

  public List<String> getAllColumns() {
    var allColumns = new ArrayList<String>();

    allColumns.addAll(this.getRequiredColumns());
    allColumns.addAll(this.getOptionalColumns());
    allColumns.addAll(this.getPrefilledColumns());

    return allColumns;
  }

  public void skipRowInFuture(int index) {
    if (!this.rowsToSkip.contains(index)) {
      this.rowsToSkip.add(index);
    }
  }

  public boolean rowShouldBeSkipped(int index) {
    return this.rowsToSkip.contains(index);
  }

  public void decorate(Sheet sheet) {
    this.decorateRequiredMassUploadColumns(sheet);
    this.decorateOptionalMassUploadColumns(sheet);
  }

  // Two MU's for yield can have the required columns be in different positions
  // All we do here is decorate the configuration object with information on what the exact
  // index is of each required column. This method is idempotent, if called with the same sheet.
  private void decorateRequiredMassUploadColumns(Sheet sheet) {
    ArrayList<String> requiredColumnsAsString = new ArrayList<>(this.getRequiredColumns());

    Row headerRow = sheet.getRow(0);
    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
      Cell cell = headerRow.getCell(i);
      if (cell != null
          && cell.getCellType() != CellType.FORMULA
          && cell.getCellType() != CellType.ERROR) {
        String cellContent = cell.getStringCellValue();
        if (requiredColumnsAsString.contains(cellContent)) {
          this.setIndexOfRequiredMassUploadColumn(cellContent, i);
        }
      }
    }
  }

  private void decorateOptionalMassUploadColumns(Sheet sheet) {
    ArrayList<String> optionalColumnsAsString = new ArrayList<>(this.getOptionalColumns());

    Row headerRow = sheet.getRow(0);
    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
      Cell cell = headerRow.getCell(i);
      if (cell != null
          && cell.getCellType() != CellType.FORMULA
          && cell.getCellType() != CellType.ERROR) {
        String cellContent = cell.getStringCellValue();
        if (optionalColumnsAsString.contains(cellContent)) {
          this.setIndexOfOptionalMassUploadColumn(cellContent, i);
        }
      }
    }
  }

  private void setIndexOfRequiredMassUploadColumn(String columnHeaderName, int index) {
    MassUploadColumn column =
        this.requiredColumns.stream()
            .filter(x -> x.getName().equals(columnHeaderName))
            .toList()
            .get(0);
    column.setIndex(index);
  }

  private final Map<Integer, List<MassUploadColumn>> requiredColumnsByMassUploadType =
      Map.ofEntries(
          Map.entry(
              MassUploadType.YIELD.setOfColumns,
              new ArrayList<>(
                  List.of(
                      new MassUploadColumn("UPLOAD_FLAG", String.class),
                      new MassUploadColumn("UPLOAD_ACTION", String.class),
                      new MassUploadColumn("PARAMETER_NAME", String.class),
                      new MassUploadColumn("PART_12NC", String.class),
                      new MassUploadColumn("SITE", String.class),
                      new MassUploadColumn("EFFECTIVE_DATE", LocalDate.class),
                      new MassUploadColumn(
                          "PARAMETER_VALUE",
                          String
                              .class))) // Should be Double.class, but due to technical debt this is
              // a String
              ));

  private void setIndexOfOptionalMassUploadColumn(String columnHeaderName, int index) {
    MassUploadColumn column =
        this.optionalColumns.stream()
            .filter(x -> x.getName().equals(columnHeaderName))
            .toList()
            .get(0);
    column.setIndex(index);
  }

  private final Map<Integer, List<MassUploadColumn>> optionalColumnsByMassUploadType =
      Map.ofEntries(
          Map.entry(
              MassUploadType.YIELD.setOfColumns,
              new ArrayList<>(List.of(new MassUploadColumn("DESCRIPTION", String.class, false)))));

  private final Map<Integer, ArrayList<MassUploadColumn>> prefilledColumnsByMassUploadType =
      new HashMap<>(
          Map.of(
              0, // YIELD
              new ArrayList<>(
                  List.of(
                      new MassUploadColumn("YIELD_GROUP_NAME", String.class, false),
                      new MassUploadColumn("YIELD_SOURCE", String.class, false),
                      new MassUploadColumn("COMMITTED_YIELD", String.class, false),
                      new MassUploadColumn("INITIAL_YIELD", String.class, false),
                      new MassUploadColumn("PLANT_CODE", String.class, false),
                      new MassUploadColumn("PART_TYPE", String.class, false),
                      new MassUploadColumn("PART_TYPE_GROUP", String.class, false),
                      new MassUploadColumn("PART_DESCRIPTION", String.class, false),
                      new MassUploadColumn("MFG_STAGE", String.class, false),
                      new MassUploadColumn("LCM", String.class, false),
                      new MassUploadColumn("PGDW", String.class, false),
                      new MassUploadColumn("PEP", String.class, false),
                      new MassUploadColumn("PEP_DESCRIPTION", String.class, false),
                      new MassUploadColumn("MAG", String.class, false),
                      new MassUploadColumn("EXCLUDE_DATE", LocalDate.class, false),
                      new MassUploadColumn("PHASE", String.class, false)))));
}
