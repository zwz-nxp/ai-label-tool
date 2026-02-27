package com.nxp.iemdm.spring.configuration;

import com.nxp.iemdm.controller.UserController;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthenticateIemdmUserOnDevAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log =
      LoggerFactory.getLogger(AuthenticateIemdmUserOnDevAuthenticationFilter.class);
  private static final String DEFAULT_DEV_WBI = "nxf45365";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String wbi = DEFAULT_DEV_WBI;
    String requestUri = request.getRequestURI();

    // Log every request to see if filter is being called
    log.debug(
        "Filter processing request: {} (Session exists: {})",
        requestUri,
        request.getSession(false) != null);

    HttpSession session = request.getSession(false);
    if (session != null) {
      log.debug(
          "Session ID: {}, Attributes: {}",
          session.getId(),
          java.util.Collections.list(session.getAttributeNames()));

      String switchedWbi = (String) session.getAttribute(UserController.SWITCH_USER_SESSION_KEY);
      if (switchedWbi != null && !switchedWbi.isBlank()) {
        wbi = switchedWbi;
        log.info("Using switched user WBI: {} for request: {}", wbi, requestUri);
      } else {
        log.debug(
            "No switched user in session, using default: {} for request: {}", wbi, requestUri);
      }
    } else {
      log.debug("No session found for request: {}, using default WBI: {}", requestUri, wbi);
    }

    IEMDMPrincipal iemdmPrincipal =
        new IEMDMPrincipal(
            new User(wbi, "<abc123>", true, true, true, true, new ArrayList<>()), new HashMap<>());

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                iemdmPrincipal, iemdmPrincipal.getPassword(), iemdmPrincipal.getAuthorities()));

    filterChain.doFilter(request, response);
  }
}
