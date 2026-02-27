package com.nxp.iemdm.spring.security.authorization;

import java.util.Objects;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class UserRoleSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

  @Value("${security.environment}")
  private String securityEnvironment;

  private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  @Override
  public EvaluationContext createEvaluationContext(
      Supplier<Authentication> authentication, MethodInvocation mi) {
    StandardEvaluationContext context =
        (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
    MethodSecurityExpressionOperations delegate =
        (MethodSecurityExpressionOperations) context.getRootObject().getValue();
    Objects.requireNonNull(delegate, "MethodSecurityExpressionOperations can not be null");
    UserRoleSecurityExpressionRoot root =
        new UserRoleSecurityExpressionRoot(delegate.getAuthentication(), securityEnvironment);
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    context.setRootObject(root);
    return context;
  }
}
