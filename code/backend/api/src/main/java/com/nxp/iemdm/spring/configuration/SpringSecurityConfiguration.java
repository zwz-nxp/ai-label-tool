package com.nxp.iemdm.spring.configuration;

import com.nxp.iemdm.spring.security.authorization.UserRolePermissionEvaluator;
import com.nxp.iemdm.spring.security.authorization.UserRoleSecurityExpressionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SpringSecurityConfiguration {

  @Bean
  static RoleHierarchy getRoleHierarchy() {
    return new RoleHierarchyImpl();
  }

  @Bean
  static MethodSecurityExpressionHandler createExpressionHandler(RoleHierarchy roleHierarchy) {
    UserRoleSecurityExpressionHandler expressionHandler = new UserRoleSecurityExpressionHandler();
    expressionHandler.setRoleHierarchy(roleHierarchy);
    expressionHandler.setPermissionEvaluator(new UserRolePermissionEvaluator());
    return expressionHandler;
  }
}
