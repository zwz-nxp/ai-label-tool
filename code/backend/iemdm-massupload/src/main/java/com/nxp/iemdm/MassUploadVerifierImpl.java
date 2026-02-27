package com.nxp.iemdm;

import com.nxp.iemdm.exception.MassUploadInvalidExcelSheetException;
import com.nxp.iemdm.model.massupload.MassUploadAction;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.shared.intf.massupload.MassUploadColumn;
import com.nxp.iemdm.shared.intf.massupload.MassUploadConfiguration;
import com.nxp.iemdm.shared.intf.massupload.MassUploadVerifier;
import com.nxp.iemdm.shared.utility.ExcelUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class MassUploadVerifierImpl implements MassUploadVerifier {

  private final MassUploadConfiguration massUploadConfiguration;
  private final MassUploadResponse massUploadResponse;

  public MassUploadVerifierImpl(
      MassUploadConfiguration massUploadConfiguration, MassUploadResponse massUploadResponse) {
    this.massUploadConfiguration = massUploadConfiguration;
    this.massUploadResponse = massUploadResponse;
  }

  @Override
  public boolean verify(XSSFSheet sheet) throws MassUploadInvalidExcelSheetException {
    boolean result = true;
    if (sheet.getPhysicalNumberOfRows() < 1) {
      throw new MassUploadInvalidExcelSheetException(
          String.format(
              "Sheet '%s' must at least contain a header row in order to be valid.",
              this.massUploadConfiguration.massUploadType.getSheetNameIncludingVersion()));
    }

    Row headerRow = sheet.getRow(0);

    if (headerRow.getLastCellNum() < 1) {
      throw new MassUploadInvalidExcelSheetException(
          "Header row must contain required headers as specified by the 'readme' sheet.");
    }

    ArrayList<String> columnHeaders = extractColumnHeaders(headerRow);
    Collection<String> duplicateRequiredHeaders =
        findDuplicateRequiredAndOptionalHeaders(columnHeaders);

    if (!duplicateRequiredHeaders.isEmpty()) {
      result = false;
      duplicateRequiredHeaders.forEach(
          headerName ->
              this.massUploadResponse.appendErrorMessage(
                  String.format("The following header may only be used once: '%s'", headerName),
                  0));
    }

    ArrayList<String> missingRequiredHeaders = findMissingRequiredHeaders(columnHeaders);
    if (!missingRequiredHeaders.isEmpty()) {
      result = false;
      missingRequiredHeaders.forEach(
          headerName ->
              this.massUploadResponse.appendErrorMessage(
                  String.format("The following required header column is missing: %s", headerName),
                  0));
    }

    if (this.massUploadResponse.getErrorCount() > 0) {
      this.massUploadResponse.setCriticalErrors();
    }

    // Before proceeding, we need to know the index of the required columns, which will be added to
    // the requiredColumns, which are contained within the MassUploadConfiguration
    this.massUploadConfiguration.decorate(sheet);

    this.verifyCellContent(sheet);

    return result;
  }

  private Collection<String> findDuplicateRequiredAndOptionalHeaders(
      ArrayList<String> columnHeaders) {
    HashSet<String> setOfHeaderColumns = new HashSet<>(columnHeaders);
    if (setOfHeaderColumns.size() == columnHeaders.size()) {
      return new HashSet<>();
    }

    ArrayList<String> headersWithoutUserColumns = ((ArrayList<String>) columnHeaders.clone());
    // Filter out user defined headers, because we are not interested in those
    headersWithoutUserColumns.retainAll(
        Stream.concat(
                massUploadConfiguration.getRequiredColumns().stream(),
                massUploadConfiguration.getOptionalColumns().stream())
            .collect(Collectors.toList()));

    HashSet<String> seenHeaders = new HashSet<>();
    ArrayList<String> duplicates = new ArrayList<>();

    for (String header : headersWithoutUserColumns) {
      boolean success = seenHeaders.add(header);
      if (!success) {
        duplicates.add(header);
      }
    }

    // Remove double entries of duplicates, because we do not want to create an error message for
    // each occurrence of a given duplicate
    return new HashSet<>(duplicates);
  }

  private ArrayList<String> findMissingRequiredHeaders(ArrayList<String> headerColumns) {
    ArrayList<String> missingRequiredHeaderColumns =
        new ArrayList<>(massUploadConfiguration.getRequiredColumns());
    for (String headerColumn : headerColumns) {
      missingRequiredHeaderColumns.remove(headerColumn);
    }

    return missingRequiredHeaderColumns;
  }

  private ArrayList<String> extractColumnHeaders(Row row) {
    ArrayList<String> headerColumns = new ArrayList<>();
    for (int i = 0; i < row.getLastCellNum(); i++) {
      Cell cell = row.getCell(i);
      if (cell != null && cell.getCellType() == CellType.STRING) {
        String cellContent = cell.getStringCellValue();
        headerColumns.add(cellContent);
      }
    }

    return headerColumns;
  }

  private void verifyCellContent(XSSFSheet sheet) {
    for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
      XSSFRow row = sheet.getRow(i);
      if (row == null || row.getLastCellNum() == -1 || ExcelUtility.isRowEmpty(row)) {
        this.massUploadConfiguration.skipRowInFuture(i);
        this.massUploadResponse.incrementIgnoreCount();
        continue;
      }

      ArrayList<String> cellErrorsPerRow = new ArrayList<>();
      for (MassUploadColumn massUploadColumn :
          this.massUploadConfiguration.getFilteredRequiredColumns()) {
        XSSFCell cell =
            row.getCell(massUploadColumn.getIndex(), MissingCellPolicy.CREATE_NULL_AS_BLANK);

        String error = verifyCell(cell);
        if (error != null) {
          cellErrorsPerRow.add(error);
          this.massUploadConfiguration.skipRowInFuture(i);
        } else if (massUploadColumn.getIndex()
            == this.massUploadConfiguration.getIndexOfUploadFlag()) {
          // Current cell is upload_flag, then we have to look if we should even bother with
          // checking
          String uploadFlag = cell.getStringCellValue();

          if (uploadFlag.equals("N")) {
            this.massUploadConfiguration.skipRowInFuture(i);
            this.massUploadResponse.incrementIgnoreCount();
            cellErrorsPerRow.clear();
            break;
            // No use in reporting the already found errors for this row, because the
            // row should be ignored
          } else {
            this.massUploadResponse.incrementRecordsToBeLoaded();
          }
        }
      }
      this.massUploadResponse.appendErrorMessages(cellErrorsPerRow, i);
    }
  }

  private String verifyCell(XSSFCell cell) {
    String error = null;
    CellType cellType = cell.getCellType();

    if (this.isCellRequired(cell) && cellType == CellType.BLANK) {
      error =
          String.format(
              "Cell at position %s must be filled in as it is required.",
              cell.getAddress().formatAsString());
    } else if (cellType == CellType.ERROR) {
      error =
          String.format(
              "Cell at position %s contains the following error: '%s'.",
              cell.getAddress().formatAsString(), cell.getErrorCellString());
    } else if (cellType == CellType.FORMULA) {
      // If we end up in this case, we will try and see if there is a formula error. If there is,
      // we return it.
      try {
        error =
            String.format(
                "Cell at position %s contains a formula error: '%s'.",
                cell.getAddress().formatAsString(), cell.getErrorCellString());
      } catch (IllegalStateException ignored) {
        // No error so we continue.
      }
    }

    return error;
  }

  private boolean isCellRequired(Cell cell) {
    return MassUploadColumn.getIndexesAsList(this.massUploadConfiguration.requiredColumns)
        .contains(cell.getColumnIndex());
  }

  private MassUploadAction getMassUploadAction(XSSFRow row, int actionIndex) {
    // Special case, if UPLOAD_ACTION column is not required
    if (actionIndex == -1) {
      return MassUploadAction.ADD_ONLY;
    }

    MassUploadAction massUploadAction;
    String action = row.getCell(actionIndex).getStringCellValue().toUpperCase();

    massUploadAction =
        switch (action) {
          case "ADD_OR_CHANGE" -> MassUploadAction.ADD_OR_CHANGE;
          case "ADD_ONLY" -> MassUploadAction.ADD_ONLY;
          case "CHANGE_ONLY" -> MassUploadAction.CHANGE_ONLY;
          case "DELETE" -> MassUploadAction.DELETE;
          default -> {
            this.massUploadResponse.appendErrorMessage(
                String.format(
                    "Could not determine which action should be performed for row '%d'.",
                    row.getCell(actionIndex).getRowIndex()),
                row.getRowNum());
            yield null;
          }
        };
    return massUploadAction;
  }
}
