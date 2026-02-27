package com.nxp.iemdm.model.search;

public enum GenericSearchType {
  DEFAULT, // the type of the value will be used how to setup the predicates
  FROM_TO, // this can be used to search as if the string value is numeric
  EXISTS, // this can be used to check if the fields is null (value = false) or is not
  // null (value
  // = true)
  BOOLEAN, // to search with a boolean value, possibilities are true, false and not used to
  // see both
  BY_ACRONYM, // gets a location by acronym instead of id
  BY_PRIORITY, // for the pivoted activation flow overview, searches for each priority name,
  // regardless of level of priority
  EXACT_MATCH // can handle a list of String on exact match case (uses in where clause)
}
