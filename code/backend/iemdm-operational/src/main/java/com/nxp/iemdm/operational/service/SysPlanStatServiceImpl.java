package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.model.configuration.SysPlanStat;
import com.nxp.iemdm.shared.intf.operational.SysPlanStatService;
import com.nxp.iemdm.shared.repository.jpa.SysPlanStatRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SysPlanStatServiceImpl implements SysPlanStatService {

  private final SysPlanStatRepository sysPlanStatRepository;

  public SysPlanStatServiceImpl(SysPlanStatRepository sysPlanStatRepository) {
    this.sysPlanStatRepository = sysPlanStatRepository;
  }

  @Transactional
  @Override
  public void clearSysPlanStat() {
    this.sysPlanStatRepository.deleteAll();
  }

  @Transactional
  @Override
  public void populateSysPlanStat() {
    List<SysPlanStat> sysPlanStats = new ArrayList<>();
    this.sysPlanStatRepository.saveAll(sysPlanStats);
  }

  private static int convertBooleanToInt(boolean theBoolean) {
    return theBoolean ? 1 : 0;
  }
}
