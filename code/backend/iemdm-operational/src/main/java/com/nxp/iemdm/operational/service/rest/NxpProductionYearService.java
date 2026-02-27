package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.configuration.NxpProductionYear;
import com.nxp.iemdm.operational.repository.jpa.NxpProductionYearRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/productionyear")
public class NxpProductionYearService {
  private final NxpProductionYearRepository nxpProductionYearRepository;

  @Autowired
  public NxpProductionYearService(NxpProductionYearRepository nxpProductionYearRepository) {
    this.nxpProductionYearRepository = nxpProductionYearRepository;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/{year}", produces = MediaType.APPLICATION_JSON)
  public Optional<NxpProductionYear> getNxpProductionYearForYear(@PathVariable("year") Integer year)
      throws NotFoundException {
    return this.nxpProductionYearRepository.findById(year);
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON)
  public Iterable<NxpProductionYear> getAllNxpProductionYears() {
    return this.nxpProductionYearRepository.findAll();
  }

  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON)
  public NxpProductionYear saveNxpProductionYear(@RequestBody NxpProductionYear nxpProductionYear) {
    Optional<NxpProductionYear> optionalPreviousYear =
        this.nxpProductionYearRepository.findById(nxpProductionYear.getYear() - 1);
    Optional<NxpProductionYear> optionalNextYear =
        this.nxpProductionYearRepository.findById(nxpProductionYear.getYear() + 1);

    optionalPreviousYear.ifPresent(
        previousYear -> {
          previousYear.setEndDate(nxpProductionYear.getStartDate().minusDays(1));
          this.nxpProductionYearRepository.save(previousYear);
        });

    optionalNextYear.ifPresentOrElse(
        nextYear -> nxpProductionYear.setEndDate(nextYear.getStartDate().minusDays(1)),
        () -> {
          long weeksInYear =
              IsoFields.WEEK_OF_WEEK_BASED_YEAR
                  .rangeRefinedBy(nxpProductionYear.getStartDate())
                  .getMaximum();

          long weekNumberOfStartDate =
              IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(nxpProductionYear.getStartDate());

          long diff = weeksInYear - weekNumberOfStartDate;

          LocalDate endDate =
              nxpProductionYear.getStartDate().plusWeeks(diff).with(ChronoField.DAY_OF_WEEK, 7);

          nxpProductionYear.setEndDate(endDate);
        });

    return this.nxpProductionYearRepository.save(nxpProductionYear);
  }
}
