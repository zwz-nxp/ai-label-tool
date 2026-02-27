package com.nxp.iemdm.spring.resolver;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;

import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import com.nxp.iemdm.spring.stereotype.CurrentUser;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

  private static final Logger log =
      LoggerFactory.getLogger(CurrentUserHandlerMethodArgumentResolver.class);

  private final String environment;

  public CurrentUserHandlerMethodArgumentResolver(
      @Value("${security.environment}") String environment) {
    this.environment = environment;
  }

  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterAnnotation(CurrentUser.class) != null
        && methodParameter.getParameterType().equals(UserDetails.class);
  }

  public Object resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    if (this.supportsParameter(methodParameter)) {
      return switch (environment) {
        case LOCAL_DEVELOPMENT_ENVIRONMENT ->
            new IEMDMPrincipal(
                new User("nxf45365", "<abc123>", true, true, true, true, new ArrayList<>()),
                new HashMap<>());
        default -> {
          Principal principal = webRequest.getUserPrincipal();
          yield ((Authentication) Objects.requireNonNull(principal)).getPrincipal();
        }
      };
    } else {
      return WebArgumentResolver.UNRESOLVED;
    }
  }
}
