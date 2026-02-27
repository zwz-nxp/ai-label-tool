package com.nxp.iemdm.spring.security.cache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class CustomRequestCache extends HttpSessionRequestCache {
  @Override
  public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
    if (!request.getRequestURI().contains("/socket")
        && !request.getRequestURI().contains("/api/")) {
      super.saveRequest(request, response);
    }
  }
}
