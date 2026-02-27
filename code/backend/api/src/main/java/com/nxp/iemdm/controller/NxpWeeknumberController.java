package com.nxp.iemdm.controller;

import com.nxp.iemdm.model.configuration.NxpProductionYear;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.utility.NXPWeekCalculator;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productionweeknumber")
public class NxpWeeknumberController {

  private final NXPWeekCalculator nxpWeekCalculator;

  public NxpWeeknumberController(NXPWeekCalculator nxpWeekCalculator) {
    this.nxpWeekCalculator = nxpWeekCalculator;
  }

  @MethodLog
  @GetMapping(path = "/{year}", produces = MediaType.APPLICATION_JSON)
  public String[][] getNxpWeeknumbersForYear(@PathVariable("year") Integer year) {
    String[][] nxpWeekNumbers = new String[12][6];

    for (int month = 1; month <= 12; month++) {
      YearMonth yearMonth = YearMonth.of(year, month);

      int amountOfWeeksInMonth = yearMonth.atEndOfMonth().get(WeekFields.ISO.weekOfMonth());

      LocalDate startOfMonth = yearMonth.atDay(1);

      if (startOfMonth.get(WeekFields.ISO.weekOfMonth()) == 0) {
        amountOfWeeksInMonth++;
      }

      Map<Integer, NxpProductionYear> nxpProductionYearMap =
          this.nxpWeekCalculator.getNxpProductionYearMap();

      for (int week = 0; week < amountOfWeeksInMonth; week++) {
        LocalDate date = startOfMonth.plusWeeks(week);

        nxpWeekNumbers[month - 1][week] =
            nxpWeekCalculator.transformLocalDateToNxpWeekNumber(date, nxpProductionYearMap);
      }
    }

    return nxpWeekNumbers;
  }
}
