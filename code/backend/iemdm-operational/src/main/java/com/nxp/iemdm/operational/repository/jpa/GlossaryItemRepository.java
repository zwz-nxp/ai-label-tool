package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.configuration.GlossaryItem;
import org.springframework.data.repository.CrudRepository;

public interface GlossaryItemRepository extends CrudRepository<GlossaryItem, String> {}
