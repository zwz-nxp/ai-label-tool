package com.nxp.iemdm.utility;

import com.nxp.iemdm.model.configuration.NxpProductionYear;
import com.nxp.iemdm.service.NxpProductionYearService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NXPWeekCalculator {
  private final NxpProductionYearService nxpProductionYearService;

  public NXPWeekCalculator(NxpProductionYearService nxpProductionYearService) {
    this.nxpProductionYearService = nxpProductionYearService;
  }

  public Map<Integer, NxpProductionYear> getNxpProductionYearMap() {
    return this.nxpProductionYearService.getAllNxpProductionYears().stream()
        .collect(
            Collectors.toMap(
                NxpProductionYear::getYear, value -> value, (a, b) -> a, TreeMap::new));
  }

  @MethodLog
  public String transformLocalDateToNxpWeekNumber(
      LocalDate localDate, Map<Integer, NxpProductionYear> nxpProductionYearMap) {
    int year = localDate.getYear();

    NxpProductionYear nxpProductionYear = nxpProductionYearMap.get(year);

    if (isDateInPreviousNxpYear(localDate, nxpProductionYear)) {
      year--;
    } else if (isDateInNextNxpYear(localDate, nxpProductionYear)) {
      year++;
    }

    LocalDate nxpYearStart = nxpProductionYearMap.get(year).getStartDate();

    long weeks = nxpYearStart.until(localDate, ChronoUnit.WEEKS);

    long nxpProdWeekNumber = weeks + 1;

    return String.valueOf(year % 100 * 100 + nxpProdWeekNumber);
  }

  private boolean isDateInNextNxpYear(LocalDate localDate, NxpProductionYear nxpProductionYear) {
    LocalDate nxpYearEnd = nxpProductionYear.getEndDate();

    return localDate.isAfter(nxpYearEnd);
  }

  private boolean isDateInPreviousNxpYear(
      LocalDate localDate, NxpProductionYear nxpProductionYear) {
    LocalDate nxpYearStart = nxpProductionYear.getStartDate();

    return localDate.isBefore(nxpYearStart);
  }
}
