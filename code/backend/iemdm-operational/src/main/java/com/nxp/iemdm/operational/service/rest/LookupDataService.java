package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.model.configuration.LocalLookupData;
import com.nxp.iemdm.model.configuration.pojo.GlobalLookupData;
import com.nxp.iemdm.model.search.GenericSearchSortField;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lookupdata")
public class LookupDataService {

  private final EntityManager entityManager;

  private static final String QUERY_RC =
      "select distinct(resource_class) from v_planning_flag_set where site = 1001 order by resource_class";
  private static final String QUERY_USER = "select wbi,name from global_user";

  @Autowired
  public LookupDataService(EntityManager entityManager) {
    super();
    this.entityManager = entityManager;
  }

  @MethodLog
  @GetMapping(path = "/global", produces = MediaType.APPLICATION_JSON)
  public GlobalLookupData getGlobalLookupData() {

    try {
      GlobalLookupData lookupData = new GlobalLookupData();

      lookupData.setUserNames(this.getLookupUsernames());
      lookupData.setGenericSearchMap(this.getGenericSearchNames());

      return lookupData;
    } catch (Exception ex) {
      ex.printStackTrace();
      return new GlobalLookupData();
    }
  }

  @MethodLog
  @GetMapping(path = "/local/{locationId}", produces = MediaType.APPLICATION_JSON)
  public LocalLookupData getLocalLookupData(@PathVariable("locationId") Integer locationId) {
    LocalLookupData lookupData = new LocalLookupData();
    lookupData.setPlannableResourceClasses(this.fillPlannableResourceClasses(locationId));
    return lookupData;
  }

  // ------ private -------------

  @SuppressWarnings("unchecked")
  private Map<String, String> getLookupUsernames() {
    Map<String, String> result = new HashMap<>();

    Query query = entityManager.createNativeQuery(QUERY_USER);
    List<Object> rows = query.getResultList();
    for (Object row : rows) {
      Object[] values = (Object[]) row;
      result.put(values[0].toString(), values[1].toString());
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private List<String> fillPlannableResourceClasses(Integer siteId) {
    String sql = String.format(QUERY_RC, siteId);
    Query query = entityManager.createNativeQuery(sql);
    List<Object> rows = query.getResultList();
    return rows.stream().map(Object::toString).collect(Collectors.toList());
  }

  private Map<String, String> getGenericSearchNames() {
    Map<String, String> result = new HashMap<>();

    for (GenericSearchSortField gssf : GenericSearchSortField.values()) {
      result.put(gssf.name(), gssf.getFieldName());
    }

    return result;
  }
}
