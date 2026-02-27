package com.nxp.iemdm.services.spring.configuration;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * Configure ProxyDataSource based on the description on the ttddyy datasource-proxy-examples on
 * GitHub.
 *
 * @see <a
 *     href="https://github.com/ttddyy/datasource-proxy-examples/blob/master/springboot-autoconfig-example/src/main/java/net/ttddyy/dsproxy/example/DatasourceProxyBeanPostProcessor.java">...</a>
 */
@Component
@ConditionalOnExpression("${proxy.data.source.interceptor.enabled:false}")
public class ProxyDataSourceBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof DataSource dataSource && !(bean instanceof ProxyDataSource)) {
      final ProxyFactory factory = new ProxyFactory(dataSource);
      factory.setProxyTargetClass(true);
      factory.addAdvice(new ProxyDataSourceInterceptor(dataSource));
      return factory.getProxy();
    }
    return bean;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    return bean;
  }

  private static class ProxyDataSourceInterceptor implements MethodInterceptor {
    private final DataSource dataSource;

    public ProxyDataSourceInterceptor(final DataSource dataSource) {
      SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
      DefaultQueryLogEntryCreator logEntryCreator = new DefaultQueryLogEntryCreator();
      logEntryCreator.setMultiline(true);
      loggingListener.setQueryLogEntryCreator(logEntryCreator);
      loggingListener.setLogLevel(SLF4JLogLevel.INFO);
      loggingListener.setWriteConnectionId(true);
      loggingListener.setWriteIsolation(true);
      this.dataSource =
          ProxyDataSourceBuilder.create(dataSource)
              .name("ProxyDataSource")
              .logSlowQueryBySlf4j(10, TimeUnit.SECONDS)
              .countQuery()
              .listener(loggingListener)
              .build();
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
      final Method proxyMethod =
          ReflectionUtils.findMethod(this.dataSource.getClass(), invocation.getMethod().getName());
      if (proxyMethod != null) {
        return proxyMethod.invoke(this.dataSource, invocation.getArguments());
      }
      return invocation.proceed();
    }
  }
}
