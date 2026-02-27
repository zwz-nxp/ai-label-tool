package com.nxp.iemdm.service;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import java.util.List;

public interface GlossaryService {
  List<GlossaryItem> getAllGlossaryItems();

  GlossaryItem saveGlossaryItem(GlossaryItem glossaryItem);
}
