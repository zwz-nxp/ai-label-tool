package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.shared.intf.operational.CachedResources;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Helper class to filter and sort some entities dynamically. TODO now some error's are logged when
 * all use-cases are thoroughly tested we can throw runtime errors instead
 */
public class FilterAndSortHelper {

  private final CachedResources cachedResources;

  public FilterAndSortHelper(CachedResources cachedResources) {
    this.cachedResources = cachedResources;
  }

  private static final Logger logger = Logger.getLogger(FilterAndSortHelper.class.getName());

  // this is map between the column name coming from Angular with the
  // corresponding getter
  // for the entity. Hence, Angular must give the correct column name!
  private static final Map<String, Method> GETTER_MAP = new HashMap<>();

  private static final Map<String, Class<?>> GETTER_CLASS = new HashMap<>();

  static {
    GETTER_CLASS.put("siteId", Location.class);
  }

  // --------- private ------

  private boolean doFilter(String filter, String value) {
    return (filter == null || filter.isEmpty())
        || (value != null
            && Pattern.compile(
                    Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(value)
                .find());
  }

  private Method getGetter(String name, Class<?> clazz) throws IntrospectionException {
    String key = this.buildKey(name, clazz);
    if (GETTER_MAP.containsKey(key)) {
      return GETTER_MAP.get(key);
    } else {
      PropertyDescriptor getDescriptor = new PropertyDescriptor(name, clazz);
      Method getter = getDescriptor.getReadMethod();
      GETTER_MAP.put(key, getter);
      return getter;
    }
  }

  public int compare(Object o1, Object o2, Method method, boolean descending) {
    try {
      Comparable comp1 = (Comparable) method.invoke(o1, new Object[] {});
      Comparable comp2 = (Comparable) method.invoke(o2, new Object[] {});
      return comp1.compareTo(comp2) * (descending ? 1 : -1);
    } catch (Exception e) {
      logger.severe("Errror while sorting list," + e);
      return 0;
    }
  }

  // ----------------- private --------------------

  private String buildKey(String fieldName, Class<?> clazz) {
    return String.format("%s.%s", clazz.getSimpleName(), fieldName);
  }

  private boolean isBlank(String str) {
    return str == null || str.isEmpty();
  }
}
