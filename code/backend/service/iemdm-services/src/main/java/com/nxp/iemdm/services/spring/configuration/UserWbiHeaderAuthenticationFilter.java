package com.nxp.iemdm.services.spring.configuration;

import com.nxp.iemdm.shared.IemdmConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class UserWbiHeaderAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String userWbi = request.getHeader(IemdmConstants.USER_WBI_HEADER);
    if (StringUtils.hasText(userWbi)) {
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(
                  userWbi, "<abc123>", Collections.emptyList()));
    }

    filterChain.doFilter(request, response);
  }
}
