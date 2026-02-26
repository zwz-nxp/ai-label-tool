package com.nxp.iemdm.shared.utility;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CollectionUtility {

  public <E> Set<E> mergeSets(Set<E> source, Set<E> target) {
    target.addAll(source);
    return target;
  }
}
