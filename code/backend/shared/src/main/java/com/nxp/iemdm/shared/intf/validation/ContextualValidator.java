package com.nxp.iemdm.shared.intf.validation;

import org.springframework.validation.Errors;

public interface ContextualValidator {
  void validate(Object object, Errors errors, Errors warnings, String wbi);
}
