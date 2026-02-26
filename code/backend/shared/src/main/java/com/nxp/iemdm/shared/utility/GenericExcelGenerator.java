package com.nxp.iemdm.shared.utility;

import static com.nxp.iemdm.shared.utility.ConsParamsFormatter.parseValue;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.model.user.Role;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** helper class to generate Excel for DqmInput and DqmActual */
public class GenericExcelGenerator {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
  private static final int COLUMN_WIDTH = 15;
  private static final String TEST = "TEST";
  private static final String PACKING = "PACKING";

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericExcelGenerator.class);

  @Getter private XSSFWorkbook workbook;
  private CellStyle textStyle;
  private final Map<SheetType, String[]> rowHeaders;
  private int rowNum = 0;

  private static final String[] DQM_INPUT_HEADERS =
      new String[] {
        "PART_12NC",
        "SITE",
        "EFFECTIVE_DATE",
        "PARAMETER",
        "DQM_STATUS",
        "DQM_ADVICE",
        "PART_12NC_CHANGE",
        "PART_12NC_ACTUAL",
        "ACTUAL_MIN",
        "ACTUAL_MAX",
        "GROUP_NAME",
        "GROUP_ACTUAL",
        "PREVIOUS_COMMIT",
      };
  private static final String[] DQM_ACTUAL_HEADERS =
      new String[] {
        "PART_12NC",
        "SITE",
        "EFFECTIVE_DATE",
        "PARAMETER",
        "DQM_STATUS",
        "DQM_ADVICE",
        "PART_12NC_CHANGE",
        "PART_12NC_ACTUAL",
        "ACTUAL_MIN",
        "ACTUAL_MAX",
        "GROUP_NAME",
        "GROUP_ACTUAL",
        "PREVIOUS_COMMIT",
        "LAST_UPDATED",
        "DQM_INPUT_REPORT_ID",
      };
  private static final String[] CONSPARAM_HEADERS =
      new String[] {
        "PART_12NC",
        "SITE",
        "EFFECTIVE_DATE",
        "PARAMETER_NAME",
        "PARAMETER_TYPE",
        "PARAMETER_VALUE",
        "REASON",
        "LAST_UPDATED",
        "UPDATE_BY",
      };

  private static final String[] PLANNING_FLOW_MASS_UPLOAD_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "PART_12NC",
        "SITE",
        "PLANNING_FLOW_NAME",
        "SEQUENTIAL_STEP_NBR",
        "ARAS_FLOW_NAME",
        "ARAS_STEP_NAME",
        "ARAS_STAGE_NAME"
      };
  private static final String[] ISSUES_HEADERS =
      new String[] {
        "PART_12NC",
        "SITE",
        "EFFECTIVE_DATE",
        "PARAMETER",
        "PREVIOUS_COMMIT",
        "PART_12NC_ACTUAL",
        "ACTUAL_MIN",
        "ACTUAL_MAX",
        "PART_12NC_CHANGE",
        "DQM_STATUS",
        "DQM_ADVICE",
        "GROUP_NAME",
        "GROUP_ACTUAL",
        "COMMITTED_YIELD",
        "GLOBAL_DQM_ISSUE_TYPE",
        "GLOBAL_DQM_ISSUE_STATUS",
        "LAST_UPDATED",
        "UPDATED_BY"
      };
  private static final String[] CONSPARAMS_OUTPUT_HEADERS =
      new String[] {
        "ID",
        "PART_12NC",
        "SITE",
        "EFFECTIVE_DATE",
        "PARAMETER_NAME",
        "PARAMETER_TYPE",
        "PARAMETER_VALUE",
        "CONS_PARAMS_ID",
        "CONS_PARAMS_LAST_UPDATED",
        "CONS_PARAMS_UPDATED_BY",
        "LAST_UPDATED",
      };
  private static final String[] SEARCH_ARGS_HEADERS =
      new String[] {"Query argument name", "Query argument value"};
  private static final String[] EQUIPMENT_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "Site",
        "Tool Id",
        "Equipment code",
        "Resource Class",
        "Process Stage",
        "Dedication",
        "Comment",
        "Serial #",
        "Asset #",
        "ConversionKit",
        "Skeleton",
        "Extended Site"
      };
  private static final String[] EQUIPMENT_CODE_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "EQUIPMENT CODE",
        "EQUIPMENT CODE NAME",
        "EQUIPMENT CODE MATURITY STATE",
        "EQUIPMENT CODE LIFECYCLE STATE",
        "EQUIPMENT MODEL",
        "EQUIPMENT MODEL LIFECYCLE STATE",
        "EQUIPMENT PLATFORM",
        "EQUIPMENT VENDOR",
        "EQUIPMENT TYPE",
        "RESOURCE GROUP",
        "LAST UPDATED",
        "UPDATED BY"
      };
  private static final String[] RESOURCE_GROUP_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "RESOURCE GROUP",
        "RG DESCRIPTION",
        "TMDB NAME",
        "COMMUNICATION CLUSTER NAME",
      };
  private static final String[] USER_HEADERS =
      new String[] {"WBI", "EMAIL", "NAME", "PRIMARY_LOCATION", "LAST_LOGIN"};
  private static final String[] USER_ROLES_HEADERS =
      new String[] {"WBI", "EMAIL", "NAME", "LOCATION", "ROLES"};
  private static final String[] UR_FINANCIAL_FLAG_HEADERS =
      new String[] {
        "Plant",
        "Phase",
        "Process (Capacity Process)",
        "MultiPhase",
        "Phase Name",
        "Counter",
        "Base Quantity",
        "Nethourfactor",
        "SecResProcesstime 1",
        "Base Unit Of Measure",
        "Resource_Secondary",
        "Category",
        "SecResUsageRate_1",
      };

  private static final String[] INSTALLED_BASE_HEADERS =
      new String[] {
        "Global Identifier",
        "Site",
        "Extended Site",
        "Resource Class",
        "Process Stage",
        "Comment",
        "Tool Id",
        "Equipment Type",
        "Skeleton",
        "Dedication",
        "Serial number",
        "Asset number",
        "Status",
        "Equipment Code",
        "Equipment Code Name",
        "Resource Group",
        "Resource Group Name",
        "TMDB Name",
        "Equipment Code Maturity State",
        "Equipment Code Lifecycle State",
        "Equipment Model",
        "Equipment Model Lifecycle State",
        "Equipment Platform",
        "Equipment Vendor",
        "Communication Cluster Name",
        "Max Sites",
        "Temp",
        "Codified Model",
        "Conversion kit"
      };

  private static final String[] ACTIVATION_FLOW_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "SAP_LOCATION",
        "PART_12NC",
        "FLOW_TYPE",
        "ARAS_FLOW_ID",
        "ARAS_FLOW_NAME",
        "ACTIVATION_DATE",
        "PRIORITY_1",
        "PRIORITY_2",
        "PRIORITY_3",
        "PRIORITY_4",
        "PRIORITY_5",
        "PRIORITY_6",
        "PRIORITY_7",
        "PRIORITY_8",
      };

  private static final String[] SUBCON_CAPACITY_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "SITE",
        "RESOURCE_GROUP",
        "RESOURCE_CLASS",
        "CAPACITY",
        "EFFECTIVE_DATE",
        "DESCRIPTION"
      };

  private static final String[] UR_TECHNICAL_OVERVIEW_TEST_HEADERS =
      new String[] {
        "PLANT_CODE",
        "MANUFACTURER_CODE",
        "PART_12NC",
        "PART_TYPE",
        "PART_MATURITY",
        "MANUFACTURING_STAGE",
        "CROSS_MANUFACTURING_INDICATOR",
        "COMMUNICATION_CLUSTERNAME",
        "MAG",
        "PGDW",
        "ARAS_DEVICENAME",
        "ARAS_FLOW_ID",
        "ARAS_FLOW_NAME",
        "ARAS_TESTSTAGE_ID",
        "ARAS_TESTSTAGE_NAME",
        "ARAS_TESTSTAGE_MATURITY",
        "PLANNING", // planning flag
        "IEMDM_PLANNING_FLOW_NAME",
        "IEMDM_PLANNING_FLOW_ACTIVATION", // activation date
        "FLOW_PRIORITY", // activation flow priority
        "STEP_SEQUENCE",
        "STEP_TYPE",
        "TEMPERATURE_CLASS",
        "TEMPERATURE",
        "L1_EC",
        "L1_EC_NAME",
        "L1_RG",
        "L1_RG_NAME",
        "L2_EC",
        "L2_EC_NAME",
        "L2_RG",
        "L2_RG_NAME",
        "TDP_EC",
        "TDP_DESCRIPTION",
        "TDP_NUMBER",
        "L4_TDP_EC",
        "L4_DESCRIPTION",
        "TEST_PROGRAMNAME",
        "MULTISITE",
        "INITIAL_TEST_TIME",
        "TESTTIME",
        "AVAILABILITY",
        "LOADTIME",
        "INDEXTIME",
        "PRODUCT_PERFORMANCE",
        "USAGERATE",
        "USAGE_RATE_FINANCIAL",
        "USAGE_RATE_EFFECTIVE_DATE",
        "YIELD",
        "STEP_YIELD",
        "CUM_INPUT_YIELD",
        "CUM_OUTPUT_YIELD",
        "ADJUSTED_USAGERATE",
        "FINANCIAL_PLANNING_FLAG",
      };

  private static final String[] UR_TECHNICAL_OVERVIEW_BURN_IN_HEADERS =
      new String[] {
        "PART_12NC",
        "PART_DESCRIPTION",
        "PLANT_CODE",
        "MANUFACTURER_CODE",
        "PART_TYPE",
        "PART_MATURITY",
        "MANUFACTURING_STAGE",
        "MAG",
        "BURN_IN_MARKER",
        "ARAS_DEVICENAME",
        "ARAS_FLOW_ID",
        "ARAS_FLOW_NAME",
        "ARAS_FLOW_LCM",
        "ARAS_STEP_ID",
        "ARAS_STEP_NAME",
        "ARAS_STEP_LCM",
        "BURN_IN_STAGE_ID",
        "BURN_IN_STAGE_NAME",
        "BURN_IN_STAGE_LCM",
        "PLANNING",
        "BURN_IN_PLANNING_FLOW_NAME",
        "BURN_IN_PLANNING_FLOW_ACTIVATION_DATE",
        "FLOW_PRIORITY",
        "STEP_TYPE",
        "STEP_SEQUENCE",
        "LEVEL_1_EC",
        "LEVEL_1_EC_NAME",
        "LEVEL_1_RG",
        "LEVEL_1_RG_NAME",
        "LEVEL_1_RG_COMMUNICATION_CLUSTER",
        "LEVEL_2_EC",
        "LEVEL_2_EC_NAME",
        "LEVEL_2_RG",
        "LEVEL_2_RG_NAME",
        "LEVEL_3_EC",
        "LEVEL_3_DESCRIPTION",
        "LEVEL_3_NUMBER",
        "EFFECTIVE_DATE_USAGE_RATE",
        "EFFECTIVE_DATE_INDICATOR",
        "USAGE_RATE_BURN_IN_BOARD",
        "USAGE_RATE_BURN_IN_CHAMBER",
        "INITIAL_BURN_IN_DURATION",
        "BURN_IN_DURATION",
        "BURN_IN_CHAMBER_AVAILABILITY",
        "BURN_IN_CHAMBER_SLOTS",
        "BURN_IN_CHAMBER_ADJUSTMENT_FACTOR",
        "BURN_IN_BOARD_SOCKET_DENSITY",
        "BURN_IN_BOARD_TIME_BETWEEN_TURNS",
        "BURN_IN_BOARD_RE_BURN_IN_RATE",
        "BURN_IN_BOARD_SOCKET_UTILIZATION",
        "TURNS_PER_WEEK",
        "BURN_IN_BOARD_GOOD_SOCKET_COUNT",
        "INFO_TYPE",
      };

  private static final String[] UR_TECHNICAL_OVERVIEW_PACKING_HEADERS =
      new String[] {
        "PLANT_CODE",
        "PART_12NC",
        "PART_DESCRIPTION",
        "PART_TYPE",
        "PART_MATURITY",
        "MAG",
        "PACKAGE_TYPE_DESCRIPTION",
        "ASSEMBLY_CG",
        "PKG_OUTLINE_VERSION_DESC",
        "PKG_OUTLINE_VERSION_LEGACY_PART",
        "DRY_PACK",
        "PACKING_TYPE",
        "PACKAGE_BODY_WIDTH",
        "PACKAGE_BODY_LENGTH",
        "PACKAGE_BODY_HEIGHT",
        "PACKING_METHOD",
        "PACKING_METHOD_DESCRIPTION",
        "PACKING_QUANTITY",
        "MFG_STAGE",
        "PACKING_FLOW_ID",
        "PACKING_FLOW_NAME",
        "PACKING_FLOW_MATURITY",
        "PLANNING",
        "IEMDM_PLANNING_FLOW_NAME",
        "IEMDM_PLANNING_FLOW_ACTIVATION",
        "FLOW_PRIORITY",
        "STEP_SEQUENCE",
        "STEP_TYPE",
        "STEP_INDICATOR_CATEGORY",
        "PACKAGE_TYPE_FAMILY_NAME",
        "EQUIPMENT_TYPE",
        "L1_EC",
        "L1_EC_NAME",
        "L1_RG",
        "L1_RG_NAME",
        "LLC_RG",
        "EFFECTIVE_DATE_USAGE_RATE",
        "OPERATION_UTILIZATION",
        "POV_EFFICIENCY",
        "YIELD",
        "PK_UNIT_TIME",
        "IDEAL_UNIT_PER_HOUR",
        "USAGE_RATE",
        "UNIT_PER_DAY",
        "NET_UNIT_PER_HOUR",
        "USAGE_RATE_FINANCIAL",
        "FINANCIAL_PLANNING_FLAG",
        "LAST_UPDATED",
        "PRODUCED",
        "MAN_MACHINE_RATIO",
        "PCA_RESOURCE",
        "PACKING_STEP_ID",
        "PACKING_STEP_NAME",
        "PACKING_STEP_MATURITY",
        "PACKING_STAGE_ID",
        "PACKING_STAGE_NAME",
        "PACKING_STAGE_MATURITY",
        "MFG_WORKFLOW"
      };

  private static final String[] UR_NEW_STAGES_HEADERS =
      new String[] {
        "PART_12NC",
        "PLANT_CODE",
        "MANUFACTURER_CODE",
        "PART_TYPE",
        "PART_MATURITY",
        "MANUFACTURING_STAGE",
        "ARAS_FLOW_NAME",
        "ARAS_STEP_NAME",
        "ARAS_TESTSTAGE_NAME",
        "STEP_TYPE"
      };

  private static final String[] UR_CHANGED_STAGES_HEADERS = UR_NEW_STAGES_HEADERS;

  private static final String[] UR_DISCONNECTED_FLOWS_HEADERS =
      new String[] {
        "PLANT_CODE",
        "ACRONYM",
        "PART_12NC",
        "PART_DESCRIPTION",
        "MFG_STAGE",
        "ARAS_FLOW_ID",
        "ARAS_FLOW_NAME",
        "ARAS_DEVICE_NAME",
        "ARAS_FLOW_LCM",
        "PLANNING_FLOW_NAME",
        "PLANNING_FLOW_ID",
        "PRIORITY",
        "ACTIVATION_DATE",
        "TYPE",
        "STEP_LIST",
        "L1_EC_LIST",
        "L2_EC_LIST",
        "L3_EC_LIST",
      };

  private static final String[] UR_DISCONNECTED_STAGES_HEADERS =
      new String[] {
        "PLANT_CODE",
        "ACRONYM",
        "PART_12NC",
        "PART_DESCRIPTION",
        "MFG_STAGE",
        "ARAS_FLOW_ID",
        "ARAS_FLOW_NAME",
        "ARAS_STAGE_ID",
        "ARAS_STAGE_NAME",
        "ARAS_DEVICE_NAME",
        "ARAS_FLOW_LCM",
        "PLANNING_FLOW_NAME",
        "PLANNING_FLOW_ID",
        "PRIORITY",
        "ACTIVATION_DATE",
        "TYPE",
        "L1_EC",
        "L2_EC",
        "L3_EC",
      };

  private static final String[] POV_PARAMS_HEADERS =
      new String[] {
        "Site",
        "Pov",
        "Pack Type",
        "Step Type",
        "Step Sequence",
        "Equipment Code",
        "LLC RG",
        "PQ",
        "PKUT",
        "Priority",
        "PCA Resource",
        "LAST UPDATED",
        "UPDATED BY"
      };

  private static final String[] PK_PLANNING_FLOWS_HEADERS =
      new String[] {
        "UPLOAD_FLAG",
        "UPLOAD_ACTION",
        "PK_PLANNING_FLOW_NAME",
        "PK_FLOW_NAME",
        "PK_STEP_NAME",
        "PK_STAGE_NAME",
      };

  private static final String[] POV_EFFICIENCY_HEADERS =
      new String[] {
        "Location", "POV", "Produced", "Efficiency", "Last Updated", "Updated By",
      };

  private static final String[] EQUIPMENT_CODE_MAN_MACHINE_RATIO_HEADERS =
      new String[] {
        "Equipment Code",
        "Equipment Code Name",
        "Site",
        "Effective date",
        "Man Machine Ratio",
        "Last updated",
        "Updated by"
      };

  private static final String[] MANUAL_FLOW_HEADERS =
      new String[] {
        "Site", "Part 12NC", "Step Type", "Last Updated", "Updated By",
      };

  private static final String[] EQUIPMENT_CODE_OPERATION_UTILIZATION_HEADERS =
      new String[] {
        "Equipment Code",
        "Equipment Code Name",
        "Site",
        "Effective date",
        "Operation Utilization",
        "Last updated",
        "Updated by"
      };

  private static final String[] MISSING_LINK_HEADERS =
      new String[] {
        "PART 12NC",
        "SITE ID",
        "SITE NAME",
        "SITE ACRONYM",
        "PLM 12NC",
        "PLM LCM",
        "PLM POV",
        "PLM PACK TYPE",
        "PLM PQ",
        "PACK FLOW",
        "FLOW SOURCE",
        "POV PARAMS",
        "ACTIVATED PLAN FLOW",
        "STATUS",
        "LAST UPDATED"
      };

  public GenericExcelGenerator() {
    this.initializeWorkbook();
    this.rowHeaders = new EnumMap<>(SheetType.class);
    this.initHeadersMap();
    DataFormat dataFormat = this.workbook.createDataFormat();
  }

  public GenericExcelGenerator(String[] shownColumns, String flow) {
    this();
    // in case this constructor is called and a custom list of columns is provided from the Test
    // Flow Details page, the map record will be overwritten with this custom list
    switch (flow) {
      case TEST:
        this.rowHeaders.put(SheetType.UR_TECHNICAL_OVERVIEW_TEST, shownColumns);
        break;
      case PACKING:
        this.rowHeaders.put(SheetType.UR_TECHNICAL_OVERVIEW_PACKING, shownColumns);
        break;
      default:
        break;
    }
  }

  private void initializeWorkbook() {
    this.workbook = new XSSFWorkbook();
    this.textStyle = this.workbook.createCellStyle();
    DataFormat dataFormat = this.workbook.createDataFormat();
    this.textStyle.setDataFormat(dataFormat.getFormat("@"));
  }

  private void initHeadersMap() {
    this.rowHeaders.put(SheetType.DQM_INPUT, DQM_INPUT_HEADERS);
    this.rowHeaders.put(SheetType.GLOBAL_DQM_ACTUAL, DQM_ACTUAL_HEADERS);
    this.rowHeaders.put(SheetType.CONS_PARAM, CONSPARAM_HEADERS);
    this.rowHeaders.put(SheetType.PLANNING_FLOW_MASS_UPLOAD, PLANNING_FLOW_MASS_UPLOAD_HEADERS);
    this.rowHeaders.put(SheetType.GLOBAL_DQM_ISSUE, ISSUES_HEADERS);
    this.rowHeaders.put(SheetType.YIELD_OUTPUT, CONSPARAMS_OUTPUT_HEADERS);
    this.rowHeaders.put(SheetType.SEARCH_ARGS, SEARCH_ARGS_HEADERS);
    this.rowHeaders.put(SheetType.EQUIPMENT, EQUIPMENT_HEADERS);
    this.rowHeaders.put(SheetType.EQUIPMENT_CODE, EQUIPMENT_CODE_HEADERS);
    this.rowHeaders.put(SheetType.RESOURCE_GROUP, RESOURCE_GROUP_HEADERS);
    this.rowHeaders.put(SheetType.USERS, USER_HEADERS);
    this.rowHeaders.put(SheetType.USER_ROLES, USER_ROLES_HEADERS);
    this.rowHeaders.put(SheetType.UR_FINANCIAL_FLAG, UR_FINANCIAL_FLAG_HEADERS);
    this.rowHeaders.put(SheetType.INSTALLED_BASE, INSTALLED_BASE_HEADERS);
    this.rowHeaders.put(SheetType.ACTIVATION_FLOW_OVERVIEW, ACTIVATION_FLOW_HEADERS);
    this.rowHeaders.put(SheetType.SUBCON_CAPACITY, SUBCON_CAPACITY_HEADERS);
    this.rowHeaders.put(SheetType.UR_TECHNICAL_OVERVIEW_TEST, UR_TECHNICAL_OVERVIEW_TEST_HEADERS);
    this.rowHeaders.put(
        SheetType.UR_TECHNICAL_OVERVIEW_BURN_IN, UR_TECHNICAL_OVERVIEW_BURN_IN_HEADERS);
    this.rowHeaders.put(
        SheetType.UR_TECHNICAL_OVERVIEW_PACKING, UR_TECHNICAL_OVERVIEW_PACKING_HEADERS);
    this.rowHeaders.put(SheetType.ISSUES_ARAS_NEW, UR_NEW_STAGES_HEADERS);
    this.rowHeaders.put(SheetType.ISSUES_ARAS_CHANGED, UR_CHANGED_STAGES_HEADERS);
    this.rowHeaders.put(SheetType.DISCONNECTED_FLOWS, UR_DISCONNECTED_FLOWS_HEADERS);
    this.rowHeaders.put(SheetType.DISCONNECTED_STAGES, UR_DISCONNECTED_STAGES_HEADERS);
    this.rowHeaders.put(SheetType.PK_PLANNING_FLOW_MASS_UPLOAD, PK_PLANNING_FLOWS_HEADERS);
    this.rowHeaders.put(SheetType.POV_PARAMS, POV_PARAMS_HEADERS);
    this.rowHeaders.put(SheetType.POV_EFFICIENCY, POV_EFFICIENCY_HEADERS);
    this.rowHeaders.put(
        SheetType.EQUIPMENT_CODE_MAN_MACHINE_RATIO, EQUIPMENT_CODE_MAN_MACHINE_RATIO_HEADERS);
    this.rowHeaders.put(SheetType.MANUAL_FLOW, MANUAL_FLOW_HEADERS);
    this.rowHeaders.put(
        SheetType.EQUIPMENT_CODE_OPERATION_UTILIZATION,
        EQUIPMENT_CODE_OPERATION_UTILIZATION_HEADERS);
    this.rowHeaders.put(SheetType.MISSING_LINK, MISSING_LINK_HEADERS);
  }

  public void generateUsersSheet(List<Person> users, String sheetName) {
    XSSFSheet sheet = this.createSheet(sheetName, SheetType.USERS);
    for (Person person : users) {
      this.addRowCells(sheet, person);
    }
  }

  public void generateUserRolesSheet(
      List<Person> users, List<Location> locations, String sheetName) {

    Map<Integer, Location> locationsById =
        locations.stream().collect(Collectors.toMap(Location::getId, Function.identity()));
    XSSFSheet sheet = this.createSheet(sheetName, SheetType.USER_ROLES);
    for (Person person : users) {
      boolean isReadonlyUser = true; // assume
      Map<Integer, Set<Role>> rolesMap = person.getRoles();
      for (Integer locationId : rolesMap.keySet()) {
        for (Role role : rolesMap.get(locationId)) {
          isReadonlyUser = false;
          this.addRowCells(sheet, person, locationsById.get(locationId), role.getRoleName());
        }
      }

      if (isReadonlyUser) {
        this.addRowCells(sheet, person, "read-only");
      }
    }
  }

  // ---------------- private ------------------------------

  private XSSFSheet createSheet(String sheetName, SheetType sheetType) {
    if (this.workbook == null) {
      this.initializeWorkbook();
    }
    XSSFSheet sheet = this.workbook.createSheet(sheetName);
    sheet.setDefaultColumnWidth(COLUMN_WIDTH);

    this.addRowHeaders(sheet, sheetType);
    return sheet;
  }

  private void createReadMeSheet(String templatePath) {
    List<String> lines = this.readReadMeLines(templatePath);
    if (lines.isEmpty()) {
      return;
    }

    XSSFSheet sheet = this.workbook.createSheet("ReadMe");
    sheet.setDefaultColumnWidth(COLUMN_WIDTH);

    int rownr = 0;
    for (String line : lines) {
      XSSFRow row = sheet.createRow(rownr++);
      Cell cell = row.createCell(0);
      cell.setCellValue(line);
      // todo: deprecated
      cell.setCellType(CellType.STRING);
    }
  }

  private List<String> readReadMeLines(String templatePath) {
    Path path;
    try {

      path =
          Paths.get(
              Objects.requireNonNull(this.getClass().getClassLoader().getResource(templatePath))
                  .toURI());
      return Files.readAllLines(path);
    } catch (URISyntaxException | IOException | NullPointerException e) {
      LOGGER.error("Unable to read template text for: {}", templatePath, e);
      return List.of();
    }
  }

  private void createTextCell(Row row, int column, String text) {
    Cell cell = row.createCell(column);
    cell.setCellStyle(this.textStyle);
    cell.setCellValue(parseValue(text));
  }

  private void createTextCell(Row row, int column, LocalDate localDate) {
    this.createTextCell(row, column, parseValue(localDate));
  }

  private void addRowHeaders(XSSFSheet sheet, SheetType sheetType) {
    XSSFRow row = sheet.createRow(0);
    this.rowNum = 1;
    String[] rowHeaders = this.rowHeaders.get(sheetType);
    for (int i = 0; i < rowHeaders.length; i++) {
      sheet.setDefaultColumnStyle(i, this.textStyle);
      this.createTextCell(row, i, rowHeaders[i]);
    }
  }

  // ---- start of addRowCells
  private void addRowCells(XSSFSheet sheet, Person person) {
    XSSFRow row = sheet.createRow(this.rowNum++);
    int colNum = 0;
    row.createCell(colNum++).setCellValue(person.getWbi());
    row.createCell(colNum++).setCellValue(person.getEmail());
    row.createCell(colNum++).setCellValue(person.getName());
    row.createCell(colNum++).setCellValue(person.getPrimaryLocation().getAcronym());
    row.createCell(colNum).setCellValue(parseValue(person.getLastLogin()));
  }

  private void addRowCells(XSSFSheet sheet, Person person, String role) {
    this.addRowCells(sheet, person, null, role);
  }

  private void addRowCells(XSSFSheet sheet, Person person, Location location, String role) {
    XSSFRow row = sheet.createRow(this.rowNum++);
    int colNum = 0;
    row.createCell(colNum++).setCellValue(person.getWbi());
    row.createCell(colNum++).setCellValue(person.getEmail());
    row.createCell(colNum++).setCellValue(person.getName());
    row.createCell(colNum++)
        .setCellValue(
            location == null ? person.getPrimaryLocation().getAcronym() : location.getAcronym());
    row.createCell(colNum).setCellValue(role);
  }

  // ---- end-of addRowCells

  private void addSearchArgIfNeeded(XSSFSheet sheet, String argName, String value) {
    if (value != null && !value.isEmpty()) {
      XSSFRow row = sheet.createRow(this.rowNum++);
      row.createCell(0).setCellValue(String.format("%s = %s", argName, value));
    }
  }

  private String translateSkeletonBoolean(Boolean value) {
    if (value == null) return "";
    return value ? "TRUE" : "FALSE";
  }

  // -----------------------------
  enum SheetType {
    DQM_INPUT,
    GLOBAL_DQM_ACTUAL,
    GLOBAL_DQM_ISSUE,
    CONS_PARAM,
    PLANNING_FLOW_MASS_UPLOAD,
    YIELD_OUTPUT,
    SEARCH_ARGS,
    EQUIPMENT,
    EQUIPMENT_CODE,
    RESOURCE_GROUP,
    USERS,
    USER_ROLES,
    UR_TECHNICAL_OVERVIEW_TEST,
    UR_TECHNICAL_OVERVIEW_BURN_IN,
    UR_TECHNICAL_OVERVIEW_PACKING,
    UR_FINANCIAL_FLAG,
    INSTALLED_BASE,
    ACTIVATION_FLOW_OVERVIEW,
    SUBCON_CAPACITY,
    ISSUES_ARAS_NEW,
    ISSUES_ARAS_CHANGED,
    DISCONNECTED_FLOWS,
    DISCONNECTED_STAGES,
    PK_PLANNING_FLOW_MASS_UPLOAD,
    POV_PARAMS,
    POV_EFFICIENCY,
    EQUIPMENT_CODE_MAN_MACHINE_RATIO,
    MANUAL_FLOW,
    EQUIPMENT_CODE_OPERATION_UTILIZATION,
    MISSING_LINK
  }
}
