package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.model.search.GenericBaseArg;
import com.nxp.iemdm.model.search.GenericSearchArg;
import com.nxp.iemdm.model.search.GenericSearchArguments;
import com.nxp.iemdm.model.search.GenericSearchType;
import com.nxp.iemdm.model.search.GenericSortArg;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.operational.CachedResources;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import com.nxp.iemdm.shared.repository.jpa.LocationRepository;
import com.nxp.iemdm.shared.repository.jpa.ManufacturerRepository;
import com.nxp.iemdm.shared.utility.SearchParameterHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.core.MediaType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/genericSearch")

/*
 * This file contains several classes which can be used to filter & sort any
 * Entity in a generic way. The idea is that the Angular code only deals with
 * enum's and that the Java then knows how to filter/sort the Entity which may
 * be deeply nested. In the Java backend the enum (GenericSearchSortField.java)
 * the complete is set to find the corresponding property. For example:
 * E_EC_RESOURCEGROUP("equipmentCode.resourceGroup.resourceGroup"),
 *
 * All backend rest call have the same signature, for example: public
 * genericSearch_ANY(searchValues: GenericSearchArguments, page: number, size:
 * number): Observable<PaginatedContent<XxxPart>>
 *
 * The GenericSearchArguments class has two lists - searchArguments:
 * <code>Example: Array<GenericSearchArg> = []; - sortArgs:
 * Array<GenericSortArg> = [];</code>
 *
 * Both GenericSearchArg and GenericSortArg have the enum GenericSearchSortField
 * (the enum that was mentioned earlier). A GenericSearchArg also has the
 * property: value:string, which contains the value to filter on. The
 * GenericSortArg has the boolean property: descending , indication the sort
 * direction.
 *
 * Depending on the type of the Entity field, the (string) search value is
 * either: - For string and enum values: a single value, that will end up as
 * sql: where dbsvalue.toLowerCase() like ('%' + value.toLowerCase() + '%')' -
 * For numeric and date values: a colon separated string where the first part is
 * the from-value and the second-part is the to-value.
 *
 * In addition, the enum mentioned above has a second optional constructor
 * argument to indicate a special search option. For example, the yield value in
 * parts_list is a string (because it is a generic value), but we would to
 * search as if it is a number. This can be achieved by :
 * P_COMM_YIELD("committedYield", GenericSearchType.FROM_TO), So here we
 * indicate that we use a from/to predicate instead of sql like predicate. In
 * Angular, we set this indication in the getSearchType() method in
 * GenericSearchUtils.
 *
 */

public class GenericSearchService {
  private static final Logger logger = Logger.getLogger(GenericSearchService.class.getName());

  private final LocationRepository locationRepository;
  private final ManufacturerRepository manufacturerRepository;
  private final PersonService personService;
  private final CachedResources cachedResources;

  public GenericSearchService(
      LocationRepository locationRepository,
      ManufacturerRepository manufacturerRepository,
      PersonService personService,
      CachedResources cachedResources) {
    this.locationRepository = locationRepository;
    this.manufacturerRepository = manufacturerRepository;
    this.personService = personService;
    this.cachedResources = cachedResources;
  }

  @MethodLog
  @PostMapping(
      path = "/person",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Person> searchPerson(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArguments) {
    Pageable pageable = this.buildPageableWithSort(searchArguments, page, size);
    Specification<Person> specification = this.buildSpecification(searchArguments, Person.class);
    return this.personService.findAll(specification, pageable);
  }

  @MethodLog
  @PostMapping(
      path = "/location",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Location> searchLocation(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArguments) {

    Pageable pageable = this.buildPageableWithSort(searchArguments, page, size);
    Specification<Location> specification =
        this.buildSpecification(searchArguments, Location.class);
    Page<Location> locationPage = this.locationRepository.findAll(specification, pageable);
    return locationPage;
  }

  @MethodLog
  @PostMapping(
      path = "/manufacturer",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Page<Manufacturer> searchManufacturerCodes(
      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
      @RequestParam(name = "size", required = false, defaultValue = "25") int size,
      @RequestBody GenericSearchArguments searchArguments) {

    Pageable pageable = this.buildPageableWithSort(searchArguments, page, size);
    Specification<Manufacturer> specification =
        this.buildSpecification(searchArguments, Manufacturer.class);
    return this.manufacturerRepository.findAll(specification, pageable);
  }

  private Pageable buildPageableWithSort(
      GenericSearchArguments searchArguments, int page, int size) {
    Optional<Sort> sortOpt = this.buildOptionalSort(searchArguments);
    return sortOpt
        .map(sort -> PageRequest.of(page, size, sort))
        .orElseGet(() -> PageRequest.of(page, size));
  }

  private Optional<Sort> buildOptionalSort(GenericSearchArguments searchArguments) {
    if (searchArguments.getSortArgs() != null && !searchArguments.getSortArgs().isEmpty()) {
      List<Order> orderList = new ArrayList<>();
      for (GenericSortArg sortArg : searchArguments.getSortArgs()) {
        Sort.Direction sortDir = sortArg.isDescending() ? Direction.DESC : Direction.ASC;
        orderList.add(new Order(sortDir, sortArg.getField().getFieldName()));
      }
      return Optional.of(Sort.by(orderList));
    } else {
      return Optional.empty();
    }
  }

  private <T> Specification<T> buildSpecification(
      GenericSearchArguments searchArguments, Class<T> rootClazz) {

    return new Specification<>() {
      @Serial private static final long serialVersionUID = 7336549707778012632L;

      @Override
      public Predicate toPredicate(
          Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> searchCriteria = new ArrayList<>();
        searchArguments.getSearchArgs().stream()
            .filter(searchArgument -> StringUtils.hasText(searchArgument.getValue()))
            .forEach(
                searchArgument -> {
                  Type fieldType =
                      GenericSearchService.this.getFieldTypeFromSearchArgument(
                          searchArgument, rootClazz);
                  GenericSearchService.this.handleSearchCriteria(
                      root, criteriaBuilder, searchCriteria, searchArgument, fieldType);
                });
        return criteriaBuilder.and(searchCriteria.toArray(new Predicate[0]));
      }
    };
  }

  private void handleSearchCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg,
      Type fieldType) {
    try {
      if (genericSearchArg.getField().getType() == GenericSearchType.FROM_TO) {
        this.handleFromToCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (genericSearchArg.getField().getType() == GenericSearchType.EXISTS) {
        this.handleExistsCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (genericSearchArg.getField().getType() == GenericSearchType.BY_ACRONYM) {
        this.handleByAcronymCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (genericSearchArg.getField().getType() == GenericSearchType.BOOLEAN) {
        this.handleBooleanCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (genericSearchArg.getField().getType() == GenericSearchType.BY_PRIORITY) {
        this.handleByPriorityCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (genericSearchArg.getField().getType() == GenericSearchType.EXACT_MATCH) {
        this.handleExactCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (fieldType instanceof Class && ((Class<?>) fieldType).isEnum()) {
        this.handleEnumCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg, fieldType);
      } else if (fieldType.equals(Integer.class)) {
        this.handleFromToIntegerCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (fieldType.equals(Instant.class)) {
        this.handleFromToInstantCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (fieldType.equals(LocalDate.class)) {
        this.handleFromToLocalDateCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else if (fieldType.equals(Date.class)) {
        this.handleFromToDateCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      } else {
        this.handleStringCriteria(root, criteriaBuilder, searchCriteria, genericSearchArg);
      }
    } catch (Exception e) {
      logger.severe("Error building predicate: " + e);
    }
  }

  private void handleStringCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);

    Expression<String> path = this.getExpressionFromFieldNames(fieldNames, root, String.class);

    /*
     * Empty string should be filtered out adding predicates. The issue is that the
     * values are used
     * in like constraints with wild cards, and "something like '%%'" matches all
     * values but null,
     * which is not what we would want. Leaving the predicate out should lead to
     * records with null
     * values for the particular field being accepted into the search results.
     */ if (!StringUtils.hasText(genericSearchArg.getValue())) {
      return;
    }
    if (genericSearchArg.getValue().contains(";")) {
      String[] hasValues =
          Arrays.stream((genericSearchArg.getValue() + ';').split(";"))
              .filter(StringUtils::hasText)
              .toArray(String[]::new);
      Predicate[] predicatesForSearchField = new Predicate[hasValues.length];
      for (int i = 0; i < hasValues.length; i++) {
        predicatesForSearchField[i] =
            criteriaBuilder.like(
                criteriaBuilder.upper(path), this.toSqlWildCard(hasValues[i].toUpperCase()));
      }
      searchCriteria.add(criteriaBuilder.or(predicatesForSearchField));
    } else {
      searchCriteria.add(
          criteriaBuilder.like(
              criteriaBuilder.upper(path),
              this.toSqlWildCard(genericSearchArg.getValue().toUpperCase())));
    }
  }

  private <T extends Enum<T>> void handleEnumCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg gsa,
      Type fieldType) {
    List<String> matchedValues = this.getMatchingEnumValues(gsa.getValue(), fieldType);
    String[] fieldNames = this.getFieldNames(gsa);
    Expression<T> path;
    List<T> enumValues;
    if (fieldNames.length > 2) {
      path = root.get(fieldNames[0]).get(fieldNames[1]).get(fieldNames[2]);
      enumValues =
          this.castToEnum(
              root.get(fieldNames[0]).get(fieldNames[1]).get(fieldNames[2]).getJavaType(),
              matchedValues);
    } else if (fieldNames.length > 1) {
      path = root.get(fieldNames[0]).get(fieldNames[1]);
      enumValues =
          this.castToEnum(root.get(fieldNames[0]).get(fieldNames[1]).getJavaType(), matchedValues);
    } else {
      path = root.get(fieldNames[0]);
      enumValues = this.castToEnum(root.get(fieldNames[0]).getJavaType(), matchedValues);
    }
    In<T> in = criteriaBuilder.in(path);
    enumValues.forEach(in::value);
    searchCriteria.add(in);
  }

  /**
   * Compares a String as a ';' separated list of String with an In where clause for a particular
   * field in the GenericSearchArg
   *
   * @param root root
   * @param criteriaBuilder criteriaBuilder
   * @param predicates list of criteria
   * @param genericSearchArg argument to evaluate
   */
  private void handleExactCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> predicates,
      GenericSearchArg genericSearchArg) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    Expression<String> path = this.getExpressionFromFieldNames(fieldNames, root, String.class);

    String[] splitValues = genericSearchArg.getValue().split(";");
    In<String> inClause = criteriaBuilder.in(path);
    for (String value : splitValues) {
      inClause.value(value);
    }
    predicates.add(inClause);
  }

  private void handleFromToIntegerCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    Pair<Integer, Integer> fromTo = this.getFromToIntegers(genericSearchArg.getValue());
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    Expression<?> expression = this.getExpressionFromFieldNames(fieldNames, root);
    searchCriteria.add(
        criteriaBuilder.between(
            expression.as(Integer.class), fromTo.getFirst(), fromTo.getSecond()));
  }

  private void handleFromToInstantCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    Pair<Instant, Instant> fromTo = this.getFromToInstants(genericSearchArg.getValue());
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    Expression<?> expression = this.getExpressionFromFieldNames(fieldNames, root);
    searchCriteria.add(
        criteriaBuilder.between(
            expression.as(Instant.class), fromTo.getFirst(), fromTo.getSecond()));
  }

  private void handleFromToLocalDateCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    Pair<LocalDate, LocalDate> fromTo = this.getFromToLocalDates(genericSearchArg.getValue());
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    Expression<?> expression = this.getExpressionFromFieldNames(fieldNames, root);
    searchCriteria.add(
        criteriaBuilder.between(
            expression.as(LocalDate.class), fromTo.getFirst(), fromTo.getSecond()));
  }

  private void handleFromToDateCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    Pair<Date, Date> fromTo = this.getFromToDates(genericSearchArg.getValue());
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    Expression<?> expression = this.getExpressionFromFieldNames(fieldNames, root);
    searchCriteria.add(
        criteriaBuilder.between(expression.as(Date.class), fromTo.getFirst(), fromTo.getSecond()));
  }

  private void handleFromToCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    String[] fromToValues = genericSearchArg.getValue().split(";");
    double fromValue = this.getDoubleFromString(fromToValues[0], true);
    double toValue = this.getDoubleFromString(fromToValues[1], false);
    Expression<Double> expression =
        this.getExpressionFromFieldNames(fieldNames, root, Double.class);
    Predicate between = criteriaBuilder.between(expression, fromValue, toValue);
    searchCriteria.add(between);
  }

  private void handleExistsCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);

    Expression<?> path = this.getExpressionFromFieldNames(fieldNames, root);

    boolean exists = !"FALSE".contentEquals(genericSearchArg.getValue().toUpperCase());
    if (exists) {
      searchCriteria.add(criteriaBuilder.isNotNull(path));
    } else {
      searchCriteria.add(criteriaBuilder.isNull(path));
    }
  }

  private void handleBooleanCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);

    Expression<?> path = this.getExpressionFromFieldNames(fieldNames, root);

    boolean isTrue = "true".equals(genericSearchArg.getValue());
    boolean isFalse = "false".equals(genericSearchArg.getValue());
    if (isTrue) {
      searchCriteria.add(criteriaBuilder.equal(path, true));
    } else if (isFalse) {
      searchCriteria.add(criteriaBuilder.equal(path, false));
    }
  }

  private void handleByAcronymCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    List<Location> locations = this.matchingLocationList(genericSearchArg.getValue());
    String[] fld = this.getFieldNames(genericSearchArg);
    searchCriteria.add(criteriaBuilder.in(root.get(fld[0])).value(locations));
  }

  private void handleByPriorityCriteria(
      Root<?> root,
      CriteriaBuilder criteriaBuilder,
      List<Predicate> searchCriteria,
      GenericSearchArg genericSearchArg) {
    List<String[]> priorityFields = this.getPriorityFields(genericSearchArg);
    Predicate[] expressions = new Predicate[8];

    for (int i = 0; i < 8; i++) {
      String[] field = priorityFields.get(i);
      Expression<String> path = this.getExpressionFromFieldNames(field, root, String.class);
      String value = genericSearchArg.getValue().toUpperCase();
      expressions[i] = criteriaBuilder.like(criteriaBuilder.upper(path), this.toSqlWildCard(value));
    }
    searchCriteria.add(criteriaBuilder.or(expressions));
  }

  private Type getFieldTypeFromSearchArgument(
      GenericSearchArg genericSearchArg, Class<?> rootClazz) {
    String[] fieldNames = this.getFieldNames(genericSearchArg);
    return this.getFieldTypeFromFields(fieldNames, rootClazz);
  }

  private Type getFieldTypeFromFields(String[] fieldNames, Class<?> rootClazz) {
    Class<?> resultType = null;
    Method method;
    try {
      for (String field : fieldNames) {
        Class<?> returnType = resultType == null ? rootClazz : resultType;
        method = this.getterMethodFromField(field, returnType);
        resultType = method.getReturnType();
      }
    } catch (IntrospectionException introspectionException) {
      logger.info(
          String.format(
              "Error getting field type for %s : %s",
              fieldNames[0], introspectionException.getMessage()));
      return null;
    }
    return resultType;
  }

  private String toSqlWildCard(String value) {
    return SearchParameterHelper.formatSqlLikeWildcard(value);
  }

  private String[] getFieldNames(GenericBaseArg genericBaseArg) {
    String fldName = genericBaseArg.getField().getFieldName().replace(".", ";");
    return fldName.split(";");
  }

  private List<String[]> getPriorityFields(GenericSearchArg genericSearchArg) {
    String fldName = genericSearchArg.getField().getFieldName().replace(".", ";");
    List<String[]> result = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      String[] field = new String[1];
      field[0] = String.format("%s%d", fldName, i);
      result.add(field);
    }
    return result;
  }

  private Method getterMethodFromField(String fieldName, Class<?> clazz)
      throws IntrospectionException {
    // "new PropertyDescriptor(fieldName, clazz);"
    // you should get a property descriptor like above, but that requires every
    // model to have a
    // setter for each property that is not the case if a model is based on a view
    // and thus readonly
    PropertyDescriptor propertyDescriptor;
    try {
      propertyDescriptor = this.getPropertyDescriptor(fieldName, clazz, "get");
    } catch (IntrospectionException introspectionException) {
      propertyDescriptor = this.getPropertyDescriptor(fieldName, clazz, "is");
    }
    return propertyDescriptor.getReadMethod();
  }

  PropertyDescriptor getPropertyDescriptor(String fieldName, Class<?> clazz, String prefix)
      throws IntrospectionException {
    return new PropertyDescriptor(
        fieldName,
        clazz,
        String.format(
            "%s%s%s", prefix, Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1)),
        null);
  }

  // unchecked class cast, should only be used when type is enum
  @SuppressWarnings("unchecked")
  private <T extends Enum<T>> List<T> castToEnum(Type fieldType, List<String> enumValuesAsString) {
    if (fieldType instanceof Class && ((Class<?>) fieldType).isEnum()) {
      try {
        Class<T> enumClazz = (Class<T>) fieldType;
        return enumValuesAsString.stream().map(string -> Enum.valueOf(enumClazz, string)).toList();
      } catch (ClassCastException classCastException) {
        logger.log(Level.SEVERE, "Invalid cast", classCastException);
        return List.of();
      }
    }
    throw new AssertionError("Provided type is not an Enum");
  }

  // unchecked class cast, should only be used when type is enum
  @SuppressWarnings("unchecked")
  private <T extends Enum<T>> List<String> getMatchingEnumValues(
      String enumValuesAsString, Type fieldType) {
    if (fieldType instanceof Class && ((Class<?>) fieldType).isEnum()) {
      String[] split = enumValuesAsString.split(";");
      try {
        Class<T> enumClazz = (Class<T>) fieldType;
        return Arrays.stream(enumClazz.getEnumConstants())
            .map(Enum::name)
            .filter(
                enumValue ->
                    Arrays.stream(split)
                        .anyMatch(stringValue -> stringValue.equalsIgnoreCase(enumValue)))
            .toList();
      } catch (ClassCastException classCastException) {
        logger.log(Level.SEVERE, "Not an Enum", classCastException);
        return List.of();
      }
    }
    throw new AssertionError("Provided type is not an Enum");
  }

  private Pair<Date, Date> getFromToDates(String value) {
    Date first = new GregorianCalendar(1990, Calendar.JANUARY, 1).getTime();
    Date second = new GregorianCalendar(9999, Calendar.SEPTEMBER, 9).getTime();
    try {
      String[] stringDates = value.split(";");
      first = this.convertToLocalDate(stringDates[0], first);
      second = this.convertToLocalDate(stringDates[1], second);
    } catch (ParseException exception) {
      logger.log(Level.SEVERE, "Error parsing dates", exception);
    }
    return Pair.of(first, second);
  }

  private Pair<LocalDate, LocalDate> getFromToLocalDates(String value) {
    LocalDate first = LocalDate.of(1990, 1, 1);
    LocalDate second = LocalDate.of(9999, 9, 9);
    String[] stringDates = value.split(";");
    first = this.convertToLocalDate(stringDates[0], first);
    second = this.convertToLocalDate(stringDates[1], second);
    return Pair.of(first, second);
  }

  private Pair<Integer, Integer> getFromToIntegers(String value) {
    String[] numberStrings = value.split(";");
    int first = Integer.MIN_VALUE;
    int second = Integer.MAX_VALUE;
    try {
      first = Integer.parseInt(numberStrings[0]);
    } catch (NumberFormatException ignore) {
      logger.log(Level.WARNING, "Failed to parse first integer");
    }
    try {
      if (numberStrings.length > 1) {
        second = Integer.parseInt(numberStrings[1]);
      }
    } catch (NumberFormatException ignore) {
      logger.log(Level.WARNING, "Failed to parse second integer");
    }
    return Pair.of(first, second);
  }

  private Pair<Instant, Instant> getFromToInstants(String value) {
    Pair<Date, Date> dates = this.getFromToDates(value);
    return Pair.of(dates.getFirst().toInstant(), dates.getSecond().toInstant());
  }

  private Date convertToLocalDate(String value, Date defaultDate) throws ParseException {
    if (value.isEmpty()) {
      return defaultDate;
    } else {
      return IemdmConstants.SIMPLE_DATE_FORMAT.parse(value);
    }
  }

  private LocalDate convertToLocalDate(String value, LocalDate defaultDate)
      throws DateTimeParseException {
    if (value.isEmpty()) {
      return defaultDate;
    } else {
      try {
        return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
      } catch (DateTimeParseException dateTimeParseException) {
        return defaultDate;
      }
    }
  }

  private double getDoubleFromString(String toBeParsed, boolean isMinimumValue) {
    try {
      return Double.parseDouble(toBeParsed);
    } catch (NumberFormatException ignored) {
      // although these numbers are ridicules for most use cases
      // the use of float is to compensate for the database to use
      // of floating point numbers, which has fewer bits than
      // java singed double
      if (isMinimumValue) {
        return Float.MIN_VALUE;
      } else {
        return Float.MAX_VALUE;
      }
    }
  }

  private <T> Expression<T> getExpressionFromFieldNames(
      String[] fieldNames, Root<?> root, Class<T> clazz) {
    if (fieldNames.length > 2) {
      return root.get(fieldNames[0]).get(fieldNames[1]).get(fieldNames[2]).as(clazz);
    } else if (fieldNames.length > 1) {
      return root.get(fieldNames[0]).get(fieldNames[1]).as(clazz);
    } else {
      return root.get(fieldNames[0]).as(clazz);
    }
  }

  private Expression<?> getExpressionFromFieldNames(String[] fields, Root<?> root) {
    if (fields.length > 3) {
      return root.get(fields[0]).get(fields[1]).get(fields[2]).get(fields[3]);
    } else if (fields.length > 2) {
      return root.get(fields[0]).get(fields[1]).get(fields[2]);
    } else if (fields.length > 1) {
      return root.get(fields[0]).get(fields[1]);
    } else {
      return root.get(fields[0]);
    }
  }

  private List<Location> matchingLocationList(String acronymSearchString) {
    Collection<Location> locations = this.cachedResources.getLocations();
    String acronymSearchStringUpperCase = acronymSearchString.toUpperCase();
    return locations.stream()
        .filter(
            location -> location.getAcronym().toUpperCase().contains(acronymSearchStringUpperCase))
        .toList();
  }
}
