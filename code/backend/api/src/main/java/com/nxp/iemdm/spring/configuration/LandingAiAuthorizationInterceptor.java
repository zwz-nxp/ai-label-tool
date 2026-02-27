package com.nxp.iemdm.spring.configuration;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.model.user.Role;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that enforces role-based access control for all Landing AI API endpoints.
 *
 * <p>Access is granted if the user has:
 *
 * <ol>
 *   <li>The global {@code Administrator_System} role, OR
 *   <li>The {@code ADC_Engineer} role for the requested location (via {@code locationId} param)
 * </ol>
 *
 * Otherwise a 403 Forbidden response is returned.
 */
@Slf4j
public class LandingAiAuthorizationInterceptor implements HandlerInterceptor {

  private static final String ROLE_ADMINISTRATOR_SYSTEM = "Administrator_System";
  private static final String ROLE_ADC_ENGINEER = "ADC_Engineer";

  private final String securityEnvironment;
  private final ObjectMapper objectMapper;

  public LandingAiAuthorizationInterceptor(String securityEnvironment, ObjectMapper objectMapper) {
    this.securityEnvironment = securityEnvironment;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    // Skip check in local dev environment
    if (LOCAL_DEVELOPMENT_ENVIRONMENT.equals(securityEnvironment)) {
      return true;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof IEMDMPrincipal principal)) {
      writeForbidden(response, "User is not authenticated.");
      return false;
    }

    Map<Integer, Set<Role>> userRoles = principal.getUserRoles();

    // Step 1: Check Administrator_System global role (location key 0)
    Set<Role> globalRoles = userRoles.getOrDefault(0, new HashSet<>());
    boolean isSystemAdmin =
        globalRoles.stream().anyMatch(role -> ROLE_ADMINISTRATOR_SYSTEM.equals(role.getId()));

    if (isSystemAdmin) {
      return true;
    }

    // Step 2: Check ADC_Engineer for the specific location
    String locationIdParam = request.getParameter("locationId");
    if (locationIdParam != null) {
      try {
        int locationId = Integer.parseInt(locationIdParam);
        Set<Role> locationRoles = userRoles.getOrDefault(locationId, new HashSet<>());
        // Also include global roles (key 0) per existing convention
        Set<Role> combined = new HashSet<>(globalRoles);
        combined.addAll(locationRoles);
        boolean isAdcEngineer =
            combined.stream().anyMatch(role -> ROLE_ADC_ENGINEER.equals(role.getId()));
        if (isAdcEngineer) {
          return true;
        }
      } catch (NumberFormatException e) {
        log.warn("Invalid locationId parameter: {}", locationIdParam);
      }
    }

    // Step 3: Deny access
    log.warn(
        "Access denied for user '{}' to LandingAI endpoint: {}",
        principal.getUsername(),
        request.getRequestURI());
    writeForbidden(
        response,
        "You don't have the privilege to access this service. "
            + "Required role: Administrator_System or ADC_Engineer for the selected location.");
    return false;
  }

  private void writeForbidden(HttpServletResponse response, String message) throws Exception {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", HttpStatus.FORBIDDEN.value());
    body.put("error", "Forbidden");
    body.put("message", message);

    objectMapper.writeValue(response.getWriter(), body);
  }
}
